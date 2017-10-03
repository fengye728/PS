import psycopg2 as db

SELECT_ALL_COMPANIES_SQL = 'SELECT * \
    FROM company com'

SELECT_COMPANY_WITH_STATISTICS_SQL = 'SELECT com.id, com.symbol, com.name, com.sector, com.last_quote_dt, st.insider_own_perc, st.inst_own_perc, st.shs_outstand, st.shs_float \
    FROM company com JOIN company_statistics st ON com.id = st.company_id'

def connect_db():
    return db.connect(database = "ps", user="postgres", password="123456", host="localhost", port = "5432")

def close_db(conn):
    conn.close()
    

def get_companies(cursor):
    cursor.execute(SELECT_ALL_COMPANIES_SQL)
    return cursor.fetchall()
    
    





cursor = conn.cursor()

cursor.execute(SELECT_COMPANY_WITH_STATISTICS_SQL)

rows = cursor.fetchall()

for row in rows:
    print(row)



# close db
db.close()
