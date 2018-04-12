import psycopg2 as db
import numpy as np
import pandas as pd

import time

# constant
GOURD_COLUMN_LIST = ['event_day', 'symbol', 'strike', 'expiration', 'price', 'size', 'size_count', 'total_size', 'millionD']
GOURD_SQL = " \
SELECT event_day, stock_symbol, strike, expiration, ROUND(AVG(price), 2) as price, size, COUNT(size) as size_count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millD \
FROM option_trade_{0} \
WHERE \
	(price >= 10 OR (price / strike > 0.2 AND price >= 2)) \
    AND call_put = 'P' \
    AND direction LIKE 'Sell%' \
    AND expiration - event_day > 300 \
	AND stock_symbol NOT IN ('SPY', 'SPXW', 'SVXY', 'QQQ', 'VXX', 'UVXY', 'DUST', 'JDST', 'RUT', 'RUTW') \
GROUP BY event_day, stock_symbol, strike, expiration, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
ORDER BY stock_symbol, event_day, strike DESC, total_size DESC \
"

GOURD_FILENAME = r'.\gourd_%s.csv'

LOCAL_QUARTER_THRESHOLD = 173


def execute_sql(cursor, sql):
    cursor.execute(sql)
    return list(cursor.fetchall())

def conbine_gourd_sql(quarter):
    return GOURD_SQL.format(quarter)

def to_quarter(ym):
    y = int(ym / 100)
    m = int(ym % 100)
    m = int((m - 1) / 3 + 1)
    return y * 10 + m

def get_quarter_list():
    ''' Get quarter list for searching sql'''
    start_quarter = 163
    #end_quarter = to_quarter(int(time.strftime('%y%m')))
    end_quarter = 173
    
    quarter_list = []
    cur_quarter = start_quarter
    while cur_quarter <= end_quarter:
        quarter_list.append(cur_quarter)
        if cur_quarter % 10 == 4:
            cur_quarter = int(cur_quarter / 10 + 1) * 10 + 1
        else:
            cur_quarter += 1
    
    return quarter_list
    

# connect db
# '54.210.133.145'
print('Connectting server...')
server_conn = db.connect(database = 'aolang', user = 'postgres', password = 'AL', host = '54.210.133.145', port = '6432')
local_conn = db.connect(database = 'aolang', user = 'postgres', password = 'AL', host = '192.168.0.249', port = '5432')
server_cursor = server_conn.cursor()
local_cursor = local_conn.cursor()

quarter_list = get_quarter_list()

records = []
for quarter in quarter_list:
    print('Searching', quarter)

    sql = conbine_gourd_sql(quarter)
    if quarter <= LOCAL_QUARTER_THRESHOLD:
        cursor = local_cursor
    else:
        cursor = server_cursor
		
    result = execute_sql(cursor, sql)
    records.extend(result)


records = pd.DataFrame(data = records, columns = GOURD_COLUMN_LIST)

records = records.sort_values(by = ['event_day', 'millionD'], ascending = False)


records.to_csv(GOURD_FILENAME % time.strftime('%y%m'), index = False)

print('Search completed!')
# close db
server_conn.close()
local_conn.close()
