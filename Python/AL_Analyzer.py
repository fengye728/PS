from db_operation import DBService

DATE_FORMAT = '%Y%m%d'

SELECT_SQL = "SELECT strike, call_put, event_day, event_time, expiration, price, size, direction \
FROM \
( \
	SELECT * \
	FROM option_trade_173\
	UNION \
	SELECT* \
	FROM option_trade_174 \
) ot \
WHERE stock_symbol = '{0}' \
	AND event_day BETWEEN {1} AND {2} \
	AND expiration >= {3} \
LIMIT 10"

def get_db():
    dao = DBService('aolang', 'postgres', 'AL', '54.210.133.145', '6432')
    return dao

def conbine_sql(symbol, start_day, end_day, expiration):
    return SELECT_SQL.format(symbol, start_day, end_day, expiration)

dao = get_db()
dao.connect()

sql = conbine_sql('YIN',170901, 171025, 171025)

r = dao.execute_sql(sql)
print(r)



