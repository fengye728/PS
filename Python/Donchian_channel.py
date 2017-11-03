import math
import numpy as np
import matplotlib.pyplot as plt
from db_operation import DBService

def get_dao():
    return DBService('ps', 'postgres', '123456', 'localhost','5432')

def is_breakout(quotes, index, entry_period):
    SMA200 = np.mean(quotes[index - 200 : index, 2])
    SMA50 = np.mean(quotes[index - 50 : index, 2])

    if SMA50 < SMA200:
        return 0
    
    max_price = np.max(quotes[index - entry_period : index, 3])
    min_price = np.min(quotes[index - entry_period : index, 4])
    if quotes[index, 4] < min_price:
        return -1
    elif quotes[index, 3] > max_price:
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
    start_index = 200

    # trade records: a list of trades
    records = []
    # trade: [pos , entry_price, exit_price, roic, days]
    trade = [0, 0, 0, 0.0, 0]
    # now position: 0: null; -1: short; 1 : long
    pos = 0
    for i in range(start_index, len(quotes)):
        if pos == 0:
            # ready to entry
            # state in i  for entry
            state = is_breakout(quotes, i, entry_period)
            # entry
            if state != 0:
                # open postion
                pos = state
                trade[0] = pos
                trade[1] = key_price(quotes, i, state, entry_period)
                trade[4] = i

        else:
            # ready to exit
            state = is_breakout(quotes, i, exit_period)
            # exit
            if pos + state == 0:
                trade[2] = key_price(quotes, i, state, exit_period)
                if trade[0] == 1:
                    # long
                    trade[3] = float(trade[2]) / trade[1] - 1
                else:
                    # short
                    trade[3] = float(trade[1]) / trade[2] - 1                
                # close postion
                trade[4] = i - trade[4]
                pos = 0
                records.append(trade.copy())

    return records

def evaluate(quotes, records):
    records = np.array(records)
    print('Count of records:', len(records), '\tMean ROIC:', np.mean(records[:, 3]), '\t', np.mean(records[:, 4]))

    long_records = records[ records[:, 0] > 0]
    short_records = records[ records[:, 0] < 0]

    print('Count of long:', len(long_records), '\tMean ROIC:', np.mean(long_records[:, 3]))
    print('Count of short:', len(short_records), '\tMean ROIC:', np.mean(short_records[:, 3]))


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
    

dao = get_dao()
dao.connect()

quotes = dao.get_quotes_by_symbol('TEVA').values

ATR_list(quotes)
records = donchian_channel(quotes, 50, 22)

evaluate(quotes, records)


