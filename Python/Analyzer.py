import numpy as np
import matplotlib.pyplot as plt

from db_operation import DBService
import ta_lib 

def JL_Trend(quotes):

    ATR_PERIOD = 20
    RATE_TREND = 0.06
    # 0: consolidation, 1: up, 2: down
    trend = 0

    point = []

    rateAcc = 0
    for i in range(ATR_PERIOD, len(quotes)):
        #RATE_TREND = ta_lib.ATR(quotes, i, ATR_PERIOD) / quotes[i][2]
        rate = quotes[i][2] / quotes[i - 1][2] - 1
        
        if trend == 1:
            if rate > 0:
                #point.append([i, quotes[i][2], 1])
                continue
        elif trend == 2:
            if rate < 0:
                #point.append([i, quotes[i][2], 2])
                continue
        
        if rateAcc == 0:
            prev_trend = trend
            reverse_index = i - 1
            
        rateAcc += rate
        if rateAcc >= RATE_TREND:
            # up
            if prev_trend == 2:
                point.append([reverse_index, quotes[reverse_index][2], 1])
                point.append([i, quotes[i][2], 3])
            trend = 1
            rateAcc = 0
            
        elif rateAcc <= -RATE_TREND:
            # down
            if prev_trend == 1:
                point.append([reverse_index, quotes[reverse_index][2], 2])
                point.append([i, quotes[i][2], 3])
            trend = 2
            rateAcc = 0
        else:
            trend = 0

    plt.plot(quotes[:, 2])
    
    point = np.array(point)
    if len(point) == 0:
        return 
    support = point[ point[:, 2] == 2]
    restistance = point[ point[:, 2] == 1]
    signal = point[ point[:, 2] == 3]
    
    plt.plot(support[:, 0], support[:, 1], "go")
    plt.plot(restistance[:, 0], restistance[:, 1], "ro")
    plt.plot(signal[:, 0], signal[:, 1], "b*")
    plt.show()


dao = DBService('ps', 'postgres', '123456', 'localhost', '5432')

dao.connect()

companies = dao.get_companies()

for symbol in companies.values[:, 0]:
    plt.title(symbol)
    quotes = dao.get_quotes_by_symbol(symbol)
    JL_Trend(quotes.values[-300 : ])
