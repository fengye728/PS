CREATE TABLE IF NOT EXISTS earning_date
(
	id bigserial,
	symbol text,
	report_date integer,
	"time" text,	-- amc or bmo
	
	CONSTRAINT earning_date_pk PRIMARY KEY(id),
	CONSTRAINT earning_date_unique UNIQUE(symbol, quarter)
);

CREATE TABLE IF NOT EXISTS dividend_date
(
	id bigserial,
	symbol text,
	report_date integer,
	
	CONSTRAINT dividend_date_pk PRIMARY KEY(id),
	CONSTRAINT dividend_datee_unique UNIQUE(symbol, quarter)
);