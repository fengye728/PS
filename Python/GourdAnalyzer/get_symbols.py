import psycopg2 as db
import numpy as np
import pandas as pd

import time

# SQL select statement
SQL_GET_TRADING_SYMBOLS = 'SELECT DISTINCT stock_symbol from option_trade_{0}'

def execute_sql(cursor, sql):
    cursor.execute(sql)
    return list(cursor.fetchall())

def to_quarter(ym):
    y = int(ym / 100)
    m = int(ym % 100)
    m = int((m - 1) / 3 + 1)
    return y * 10 + m

# ------------- main process --------------
# construct sql
cur_quarter = to_quarter(int(time.strftime('%y%m')))
select_symbols_sql = SQL_GET_TRADING_SYMBOLS.format(cur_quarter)

try:
    # connect db
    server_conn = db.connect(database = 'aolang', user = 'postgres', password = 'AL', host = '54.210.133.145', port = '6432')
    server_cursor = server_conn.cursor()

    # get symbols
    symbols = execute_sql(server_cursor, select_symbols_sql)
    print(len(symbols))
    for symbol in symbols:
        print(symbol[0].strip())
except:
    pass
