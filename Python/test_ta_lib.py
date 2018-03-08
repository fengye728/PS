import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from db_operation import DBService
import ta_lib

def SMA_LIST(data_list, period):
    sma = [0] * period
    for i in range(period, len(data_list)):
        sma.append(np.mean(data_list[i - period + 1 : i + 1]))

    return sma

# find reverse bulge
def reverse_bulge_list(quotes):
    ''' Get reverse_bulge day in period [ index - period + 1, index ] '''
    # 0: in; 1: out
    status = 1
    result_list = []

    # date filter just test
    START_DT = -100
    END_DT = -50
    
    for i in range(max(0, len(quotes) + START_DT), len(quotes) + END_DT):
        mi = ta_lib.MI(quotes, i)
        if mi is None:
            continue
        if mi >= 27:
            status = 0
        else:
            if status == 0 and mi <= 26.5:
                status = 1
                result_list.append(i)
    return result_list


def exist_consolidation(quotes, index):
    OB_PERIOD = 100
    ATR_PERIOD = 14
    
    TIMES = 1.5

    
    for i in range(max(0, index - OB_PERIOD), index):
        try:
            
            pre_vola = ta_lib.VOLATILITY_AN(quotes, i - ATR_PERIOD, ATR_PERIOD)
            cur_vola = ta_lib.VOLATILITY_AN(quotes, i, ATR_PERIOD)
                    
            if pre_vola > cur_vola * TIMES:
                return True
        except:
            pass

    return False
    
    
def draw(company, quotes):
    
    EXPAND_SIZE = 100
    SMA_PRICE_SIZE = 50
    EXAM_SIZE = 100
    mi_list = [0] * EXPAND_SIZE
    mfi_list = [0] * EXPAND_SIZE

    symbol = company.symbol
    print('-------- ', symbol, 'of', company.sector)
    for i in range(EXPAND_SIZE, len(quotes)):
        index = i
        dt = quotes.iloc[index].quote_date
        mfi = ta_lib.MFI(quotes, index)
        mi = ta_lib.MI(quotes, index)

        mi_list.append(mi)
        mfi_list.append(mfi)

        #print(i, dt, mfi, mi, sep = '\t')
        
    mfi_list = np.array(mfi_list)
    long_period = 16
    short_period = 8

    long_ema_mfi = ta_lib.SMA_LIST(mfi_list, long_period)
    short_ema_mfi = ta_lib.SMA_LIST(mfi_list, short_period)

    # ma list
    ma_price_list = ta_lib.SMA_LIST(quotes.close, SMA_PRICE_SIZE)
    ma_price_list_2 = ta_lib.EMA_LIST(quotes.close, 10)
        
    rb_list = reverse_bulge_list(quotes)
    for index in rb_list:

        if index < 50:
            continue

        # filter consolidation
        if not exist_consolidation(quotes, index):
            continue
        '''
        # filter mfi
        if mfi_list[index] > 80:
            continue

        if index - 30 >= 0 and np.max(mfi_list[index - 30: index]) < 80:
            continue
        
        if np.sum([ 1 if mfi < 40 else 0 for mfi in mfi_list[index - 50: index]]) > 20:
            continue
        # trend
        '''
        # print info
        high = np.max(quotes.iloc[index : index + EXAM_SIZE].high)
        low = np.min(quotes.iloc[index : index + EXAM_SIZE].low)
        now = quotes.iloc[index].close
        
        print( int(quotes.iloc[index].quote_date), '%.2f' % now, '%.2f' % (high / now - 1), '%.2f' % (low / now - 1), sep = '\t')

        # show drawing
        plt.title(symbol + ' ' + str(index))

        # display mi
        plt.subplot(311)
        plt.plot(mi_list[index - EXPAND_SIZE : index + EXPAND_SIZE])
        plt.plot([27] * EXPAND_SIZE * 2, color = 'k')
        plt.plot([26.5] * EXPAND_SIZE * 2, 'k:')
        plt.plot([EXPAND_SIZE], [26.5], 'r*')
        plt.ylabel('MI')
        plt.xlim(0, EXPAND_SIZE)

        # display stock
        plt.subplot(312)
        
        view_quotes = list(quotes[index - EXPAND_SIZE : index + EXPAND_SIZE].close)
        plt.plot(view_quotes)
        plt.plot([EXPAND_SIZE], [view_quotes[EXPAND_SIZE]] if len(view_quotes) > EXPAND_SIZE else [0], 'r*')

        # plot ma
        plt.plot(ma_price_list[index - EXPAND_SIZE : index + EXPAND_SIZE], 'g')
        plt.plot(ma_price_list_2[index - EXPAND_SIZE : index + EXPAND_SIZE], 'r')
        
        plt.ylabel('stock')
        plt.xlim(0, EXPAND_SIZE)

        # display mfi
        plt.subplot(313)
        
        plt.plot(mfi_list[index - EXPAND_SIZE : index + EXPAND_SIZE])
        plt.plot(long_ema_mfi[index - EXPAND_SIZE : index + EXPAND_SIZE], color = 'r')
        plt.plot(short_ema_mfi[index - EXPAND_SIZE : index + EXPAND_SIZE], color = 'g')
        
        plt.plot([80] * EXPAND_SIZE * 2, color = 'k')
        plt.plot([20] * EXPAND_SIZE * 2, color = 'k')
        plt.plot([60] * EXPAND_SIZE * 2, 'y:')
        plt.plot([40] * EXPAND_SIZE * 2, 'y:')
        plt.plot([50] * EXPAND_SIZE * 2, 'y:')
        
        plt.plot([EXPAND_SIZE, EXPAND_SIZE], [0, 100], 'k:')
        plt.plot([EXPAND_SIZE + 20, EXPAND_SIZE + 20], [0, 100], 'k:')
        
        plt.ylabel('MFI')
        plt.ylim(0, 100)
        plt.xlim(0, EXPAND_SIZE)

        plt.show()

db = DBService()
db.connect()

'''
# test draw
symbol = 'SDRL'
quotes = db.get_quotes_by_symbol(symbol)
draw(symbol, quotes)
'''

#all_quotes = db.get_all_quotes()

# find reverse_bulge list

companies = db.get_companies()


for i in companies.index:
    if i < 337:
        continue

    if companies.iloc[i].sector == 'Health Care':
        continue
    
    print(i)
    quotes = db.get_quotes_by_symbol(companies.iloc[i].symbol)
    draw(companies.iloc[i], quotes)
