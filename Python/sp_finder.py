import numpy as np

import matplotlib.pyplot as plt
from db_operation import DBService
import ta_lib

# get dao
dao = DBService('ps', 'postgres', '123456', 'localhost', '5432')

dao.connect()


def basic_filter(quotes, index, period):
    if index - period < 0:
        return False
    
    target_quo = quotes.iloc[index - period, index]

    if np.min(target_quo.close) < 5 or np.mean(target_quo.volume) < 100000:
        return False

    return True

def angular(quotes, index, direc):
    '''direc : 0 - resist angular, 1 - support angular'''
    HALF_OB_PERIOD = 5

    try:
        ob_quos = quotes.iloc[index - HALF_OB_PERIOD : index + HALF_OB_PERIOD]

        if direc == 0:
            if np.max(ob_quos.high) <= quotes.iloc[index].high:
                return True
            else:
                return False
        elif direc == 1:
            if np.min(ob_quos.low) >= quotes.iloc[index].low:
                return True
            else:
                return False
    except:
        pass

    return False



def sp_plot(quotes, index, period):

    try:
        
        #if basic_filter(quotes, index, period):
        #   return False
        
        # for plot
        res_index = []
        res_price = []

        sup_index = []
        sup_price = []
    
        # find support and resist point
        base_i = index - period
        i = index - period
        while i <= index:
            
            if angular(quotes, i, 0):
                res_index.append(i - base_i)
                res_price.append(quotes.iloc[i].high)
            if angular(quotes, i, 1):
                sup_index.append(i - base_i)
                sup_price.append(quotes.iloc[i].low)
            
            i += 1
        
        plt.plot(list(range(period)), quotes.iloc[index - period : index].close)
        plt.plot(list(range(period)), quotes.iloc[index - period : index].high, 'y:')
        plt.plot(list(range(period)), quotes.iloc[index - period : index].low, 'y:')
        plt.plot(res_index, res_price, "ro")
        plt.plot(sup_index, sup_price, "go")
        plt.show()
    except:
        pass
    
    return False

# main code

quotes = dao.get_quotes_by_symbol('C')

sp_plot(quotes, len(quotes) - 100, 300)

