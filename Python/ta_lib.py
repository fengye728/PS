import numpy as np

def TR(quotes, index):
    pre_close = quotes[index - 1][2]
    quote = quotes[index]
    return max( quote[3] - quote[4], abs(quote[4] - pre_close), abs(quote[3] - pre_close))

def ATR(quotes, index, period):
    tr_list = [ TR(quotes, i) for i in range(index - period + 1, index + 1)]
    return np.mean(tr_list)