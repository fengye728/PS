/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.constants;

public class CommonConstants {
	
	
	public static final int BUFFER_SIZE_OF_READER = 524288;	// 512 * 1024
	
	public final static String DATE_FORMAT_OUT = "yyyyMMdd";
	public final static String DATE_FORMAT_NASDAQ_IN = "yyyy/MM/dd";
	
	
	public final static String CSV_SEPRATOR_BETWEEN_FIELD = "\",\"";
	public final static String CSV_SURROUNDER_OF_FIELD = "\"";
	public final static String CSV_NEWLINE = "\n";
	public final static String CSV_NEWLINE_REG = "[\r]?\n";
	
	
	// ---------------- Load and Persist Option ----------------------------------
	public final static int PERSIST_OPTION_DISK = 1;
	
	public final static int PERSIST_OPTION_DATABASE = 2;
	
	public final static int LOAD_OPTION_DISK = 1;
	
	public final static int LOAD_OPTION_DATABASE = 2;
	
	// --------------- NASDAQ CONSTANTS -------------------------- 
	public final static String NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD = ",[\r]?\n";
	
	public final static int NASDAQ_COMPANY_LIST_RECORD_FIELDS_NUMBER = 9;
	
	public final static String URL_GET_COMPANY_LIST_NASDAQ = "http://www.nasdaq.com/screening/companies-by-industry.aspx?render=download";
	
	public final static String URL_GET_STOCK_QUOTES_NASDAQ_PREFIX = "http://www.nasdaq.com/symbol/";
	
	public final static String URL_GET_STOCK_QUOTES_NASDAQ_SUFFIX = "/historical";
	
	// ----------------------- FINVIZ CONSTANTS ---------------------------------
	public final static String URL_GET_COMPANY_STATISTICS_FINVIZ = "https://www.finviz.com/quote.ashx?t=";
	
	
	// ------------------------ Path --------------------------------
	public final static String PATH_LOGGER_PROPERTY = "log4j.properties";
	
	public final static String PATH_COMPANY_INFO_OUTPUT = "StockQuotes\\";
}