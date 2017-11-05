import numpy as np
import matplotlib.pyplot as plt
from db_operation import DBService
import random

def get_dao():
    return DBService('ps', 'postgres', '123456', 'localhost','5432')

def TR(quotes, index):
    pre_close = quotes[index - 1][2]
    quote = quotes[index]
    return max( quote[3] - quote[4], abs(quote[4] - pre_close), abs(quote[3] - pre_close))

def ATR(quotes, index, period):
    tr_list = [ TR(quotes, i) for i in range(index - period + 1, index + 1)]
    return np.mean(tr_list)

def ATR_list(quotes):
    start_index = 100
    atr_x = range(start_index, len(quotes))
    atr_y = []
    for i in atr_x:
        #atr_y.append(ATR(quotes, i, 20))
        atr_y.append(TR(quotes, i))

    plt.subplot(2, 1, 1)
    plt.plot(atr_x ,quotes[start_index : , 2], color = 'r')
    plt.subplot(2, 1, 2)
    plt.plot(atr_x, atr_y, 'b')
    plt.show()

# MPE and MAE
def MPAE(quotes, index, period):
    # ATR
    atr = ATR(quotes, index, int(period / 2))

    # MPE
    max_price = np.max(quotes[ index + 1 : index + period + 1, 3])
    mpe = max_price / quotes[index][2] / atr

    # MAE
    min_price = np.min(quotes[ index + 1 : index + period + 1, 4])
    mae = min_price / quotes[index][2] / atr

    return mpe, mae

def is_breakout(quotes, index, entry_period):
    SMA300 = np.mean(quotes[index - 300 : index, 2])
    SMA50 = np.mean(quotes[index - 50 : index, 2])
    
    max_price = np.max(quotes[index - entry_period : index, 3])
    min_price = np.min(quotes[index - entry_period : index, 4])

     
    if quotes[index, 2] < min_price and SMA50 < SMA300:
        return -1
    elif quotes[index, 2] > max_price and SMA50 > SMA300:
        return 1
    else:
        return 0

def key_price(quotes, index, pos, period):
    max_price = np.max(quotes[index - period : index, 3])
    min_price = np.min(quotes[index - period : index, 4])
    if pos == 1:
        # long
        if quotes[index, 1] > max_price:
            return quotes[index, 1]
        else:
            return max_price
    elif pos == -1:
        # short
        if quotes[index, 1] < min_price:
            return quotes[index, 1]
        else:
            return min_price

def donchian_channel(quotes, entry_period, exit_period):
    start_index = 300

    # trade records: a list of trades
    records = []
    # trade: [pos , mpe, mae]
    # position: 0: null; -1: short; 1 : long
    trade = [0, 0.0, 0.0]

    i = start_index
    while i < len(quotes) - exit_period:
        # entry
        pos = is_breakout(quotes, i, entry_period)

        if pos != 0:
            trade[0] = pos
            trade[1], trade[2] = MPAE(quotes, i, exit_period)
            records.append(trade.copy())
            i += exit_period
        else:
            i += 1

    return records

def random_channel(quotes, entry_period, exit_period):

    records = []
    trade = [0, 0.0, 0.0]
    for i in range(100):
        index = random.randint(300, len(quotes) - exit_period)
        trade[0] = 1
        trade[1], trade[2] = MPAE(quotes, index, exit_period)
        records.append(trade.copy())

    return records

def evaluate(quotes, records):
    records = np.array(records)
    #print('Count of records:', len(records), '\tMean E:', np.mean(records[:, 3]), '\t', np.mean(records[:, 4]))

    long_records = records[ records[:, 0] > 0]
    short_records = records[ records[:, 0] < 0]

    print('Count of long:', len(long_records), '\tMean E:', np.sum(long_records[:, 1]) / np.sum(long_records[:, 2]))
    print('Count of short:', len(short_records), '\tMean E:', np.sum(short_records[:, 1]) / np.sum(short_records[:, 2]))



dao = get_dao()
dao.connect()

quotes = dao.get_quotes_by_symbol('SDRL').values

#ATR_list(quotes)
records = donchian_channel(quotes, 20, 50)

evaluate(quotes, records)

evaluate(quotes, random_channel(quotes, 20, 20))


