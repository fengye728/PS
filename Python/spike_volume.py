from db_operation import DBService

db = DBService()

db.connect()

companies = db.get_companies()

