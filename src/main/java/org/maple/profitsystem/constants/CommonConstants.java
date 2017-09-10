/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.constants;

public class CommonConstants {
	
	public final static String DATE_FORMAT_OUT = "yyyyMMdd";
	
	public final static String DATE_FORMAT_NASDAQ_IN = "yyyyMMdd";
	
	public final static String NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD = ",[\r]?\n";
	
	public final static String NASDAQ_COMPANY_LIST_SEPRATOR_OF_FIELD = "\",\"";
	
	public final static int NASDAQ_COMPANY_LIST_RECORD_FIELDS_NUMBER = 9;
	
	public final static String URL_GET_COMPANY_LIST_NASDAQ = "http://www.nasdaq.com/screening/companies-by-industry.aspx?render=download";
	
	public final static String URL_GET_STOCK_QUOTES_NASDAQ_PREFIX = "http://www.nasdaq.com/symbol/";
	
	public final static String URL_GET_STOCK_QUOTES_NASDAQ_SUFFIX = "/historical";
	
	
	
	
	// Path
	public final static String LOGGER_PROPERTY_PATH = "log4j.properties";
	
	public final static String STOCK_QUOTES_OUTPUT_PATH = "StockQuotes\\";
}