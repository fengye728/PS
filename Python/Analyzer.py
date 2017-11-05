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
            if rate + rateAcc > 0:
                #point.append([i, quotes[i][2], 1])
                rateAcc = 0
                continue
        elif trend == 2:
            if rate + rateAcc < 0:
                #point.append([i, quotes[i][2], 2])
                rateAcc = 0
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

    point = np.array(point)
    if len(point) <= 1:
        return 0
    support = point[ point[:, 2] == 2]
    restistance = point[ point[:, 2] == 1]
    signal = point[ point[:, 2] == 3]
    
    roic_list = []
    # pos, roic
    trade = [0, 0.0]  
    
    if point[0][2] == 2:
        # first is support and long position
        position = True
    else:
        position = False
    
    # calc roic
    for i in range(1, len(signal)):
        if position:        
            # sell to close the previous trade
            trade[0] = 1    # long trade
            trade[1] = float(signal[i][1] / signal[i - 1][1]) - 1
        else:
            # buy to close the previous short trade
            trade[0] = -1   # short trade
            trade[1] = float(signal[i - 1][1] / signal[i][1]) - 1
            
        position = not position
        roic_list.append(trade.copy())
    
    # printing
    roic_list = np.array(roic_list)
    
    roic = 0.0
    if len(roic_list) != 0:
        roic = np.mean(roic_list[:, 1])
        if abs(roic) > 0.2:
            print('Mean roic:', roic)
        
        
    return roic
    # draw
    plt.plot(quotes[:, 2])
    plt.plot(support[:, 0], support[:, 1], "go")
    plt.plot(restistance[:, 0], restistance[:, 1], "ro")
    plt.plot(signal[:, 0], signal[:, 1], "b*")
    plt.show()


dao = DBService('ps', 'postgres', '123456', 'localhost', '5432')

dao.connect()

companies = dao.get_companies()

total_roic = []
count = 0
for symbol in companies[:, 0]:
    plt.title(symbol)
    quotes = dao.get_quotes_by_symbol(symbol)
    print(symbol)
    total_roic.append(JL_Trend(quotes))

total_roic = np.array(total_roic)
total_roic = total_roic[total_roic != 0]
print('Total roic:', np.sum(total_roic), "Count of", len(total_roic), "Mean:", np.mean(total_roic))
print('Max roic:', np.max(total_roic), 'min roic:', np.min(total_roic))
