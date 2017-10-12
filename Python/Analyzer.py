from db_operation import DBService
from datetime import datetime
import matplotlib.pyplot as plt
import matplotlib.dates as mdates

DATE_FORMAT = '%Y%m%d'

dao = DBService('ps', 'postgres', '123456', 'localhost', '5432')

dao.connect()


lines = dao.get_quotes_by_symbol('SNAP')

plt.gca().xaxis.set_major_formatter(mdates.DateFormatter('%m/%d'))
plt.gca().xaxis.set_major_locator(mdates.DayLocator())

def JL_Trend(quotes):

    RATE_TREND = 0.06
    # 0: consolidation, 1: up, 2: down
    trend = 0

    closeSeries = quotes['close']

    dates = []
    prices = []

    rateAcc = 0
    for i in range(1, closeSeries.size):
        rate = closeSeries[i] / closeSeries[i - 1] - 1
        
        if trend == 1:
            if rate > 0:
                rateAcc = 0
                date = datetime.strptime(str(lines['quote_date'][i]), DATE_FORMAT).date()
                dates.append(date)
                prices.append(2)
                continue
        elif trend == 2:
            if rate < 0:
                rateAcc = 0
                date = datetime.strptime(str(lines['quote_date'][i]), DATE_FORMAT).date()
                dates.append(date)
                prices.append(1)
                continue

        rateAcc += rate
        
        if rateAcc >= RATE_TREND:
            trend = 1
            date = datetime.strptime(str(lines['quote_date'][i]), DATE_FORMAT).date()
            dates.append(date)
            prices.append(2)
        elif rateAcc <= -RATE_TREND:
            trend = 2
            date = datetime.strptime(str(lines['quote_date'][i]), DATE_FORMAT).date()
            dates.append(date)
            prices.append(1)            

    print(dates)
    plt.plot(dates, prices)
    plt.show()    
    
JL_Trend(lines[:30])
