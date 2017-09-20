/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.constants;

public class CommonConstants {
	
	public final static int MAX_THREADS = 15;
	
	public final static String NULL_STRING = "null";
	
	public final static int BUFFER_SIZE_OF_READER = 524288;	// 512 * 1024
	
	
	// -------------- Date Format -----------------------------------
	public final static String DATE_FORMAT_OUT = "yyyyMMdd";
	public final static String DATE_FORMAT_NASDAQ_IN = "yyyy/MM/dd";
	
	// ---------------- CSV ------------------------------
	public final static String CSV_SEPRATOR_BETWEEN_FIELD = "\",\"";
	public final static String CSV_SURROUNDER_OF_FIELD = "\"";
	public final static String CSV_NEWLINE = "\n";
	public final static String CSV_NEWLINE_REG = "[\r]?\n";
}