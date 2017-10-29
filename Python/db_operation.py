import psycopg2 as db
import pandas as pd

# Columns Name #
COMPANY_COLUMN_LIST = ['symbol', 'name', 'ipo_year', 'sector', 'industry']

QUOTE_COLUMN_LIST = ['quote_date', 'open', 'close', 'high', 'low', 'volume']


# Select SQL #
SELECT_ALL_COMPANIES_SQL = 'SELECT com.symbol, com.name, com.ipo_year, com.sector, com.industry \
    FROM company com'

SELECT_COMPANY_WITH_STATISTICS_SQL = 'SELECT com.id, com.symbol, com.name, com.sector, com.last_quote_dt, st.insider_own_perc, st.inst_own_perc, st.shs_outstand, st.shs_float \
    FROM company com JOIN company_statistics st ON com.id = st.company_id'

SELECT_QUOTES_BY_SYMBOL_SQL = 'SELECT quo.quote_date, quo.open, quo.close, quo.high, quo.low, quo.volume \
    FROM stock_quote quo JOIN company com ON com.id = quo.company_id \
    WHERE com.symbol LIKE \'%s\' \
    ORDER BY quo.quote_date ASC'


class DBService:

    def __init__(self, database, user, password, host, port):
        self.database = database
        self.user = user
        self.password = password
        self.host = host
        self.port = port
    
    def connect(self):
        self.conn = db.connect(database = self.database, user = self.user, password = self.password, host = self.host, port = self.port)
        self.cursor = self.conn.cursor()

    def close_db(self):
        self.conn.close()
    
    def get_companies(self):
        '''Return DataFrame of companies'''
        self.cursor.execute(SELECT_ALL_COMPANIES_SQL)
        return pd.DataFrame(data = self.cursor.fetchall(), columns = COMPANY_COLUMN_LIST)

    def get_quotes_by_symbol(self, symbol):
        self.cursor.execute(SELECT_QUOTES_BY_SYMBOL_SQL % symbol)
        return pd.DataFrame(data = self.cursor.fetchall(), columns = QUOTE_COLUMN_LIST)
    
    def execute_sql(self, sql):
        self.cursor.execute(sql)
        return self.cursor.fetchall()
    
