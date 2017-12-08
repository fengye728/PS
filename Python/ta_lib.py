import numpy as np
import pandas as pd

def TR(quotes, index):
    pre_close = quotes.iloc[index - 1].close
    quote = quotes.iloc[index]
    return max( quote.high - quote.low, abs(quote.high - pre_close), abs(quote.low - pre_close))

def ATR(quotes, index, period = 14):
    tr_list = [ TR(quotes, i) for i in range(index - period + 1, index + 1)]
    return np.mean(tr_list)

def NP(quotes, index):
    quote = quotes.iloc[index]
    return (quote.high + quote.low + quote.close) / 3

def SMA(data_list, index, period = 14):
    if index < period - 1:
        return None
    return np.mean(data_list[index - period + 1 : index + 1])

def SMA_LIST(data_list, period):
    sma = [0] * period
    for i in range(period, len(data_list)):
        sma.append(np.mean(data_list[i - period + 1 : i + 1]))

    return sma
    
def EMA(data_list, index, period = 14):
    if index < period:
        return None
    
    data_list = np.array(data_list)
    multiplier = 2 / (period + 1)
    prev_ema = np.mean(data_list[0 : period])
    cur_ema = 0
    for i in range(period, len(data_list)):
        cur_ema = (data_list[i] - prev_ema) * multuplier + prev_ema
        prev_ema = cur_ema
    return cur_ema

def EMA_LIST(data_list, period = 14):
    ''' Return a list of EMA '''
    if len(data_list) < period:
        return None

    data_list = np.array(data_list)
    multiplier = 2 / (period + 1)

    # init ema list
    ema_list = [0] * len(data_list)
    ema_list[period - 1] = np.mean(data_list[0 : period])
    
    for i in range(period, len(data_list)):
        ema_list[i] = (data_list[i] - ema_list[i - 1]) * multiplier + ema_list[i - 1]

    return np.array(ema_list)
    
def MFI(quotes, index, period = 14):
    ''' Money Flow Index'''
    if index < period:
        return None
    
    pos_mf = 0.0
    neg_mf = 0.0

    prev_np = NP(quotes, index - period)
    #print('NP:', prev_np, index)
    for i in range(index - period + 1, index + 1):
        cur_np = NP(quotes, i)
        if prev_np < cur_np:
            # Positive MF
            pos_mf += quotes.iloc[i].volume * cur_np
        elif prev_np > cur_np:
            # Negetive MF
            neg_mf += quotes.iloc[i].volume * cur_np

        prev_np = cur_np
    
    if neg_mf == 0:
        return 100
    else:
        return 100 - 100 / (1 + pos_mf / neg_mf)

def MI(quotes, index, period = 25):
    ''' Mass Index '''
    EMA_PERIOD = 9

    if index < period + EMA_PERIOD * 2:
        return None
    
    high_low_diff = quotes.high - quotes.low
    high_low_diff = high_low_diff[0: index + 1]
    # single_ema list including (period + EMA_PERIOD) elements
    
    single_ema_list = EMA_LIST(high_low_diff, EMA_PERIOD)[EMA_PERIOD : ]
    double_ema_list = EMA_LIST(single_ema_list, EMA_PERIOD)

    return np.sum(single_ema_list[-period : ] / double_ema_list[-period : ])
    
    
    
