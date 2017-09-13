/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem;

import java.io.File;
import java.io.IOException;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.maple.profitsystem.collector.CompanyInfoCollector;
import org.maple.profitsystem.constants.CommonConstants;

public class Application {
	private static Logger logger = Logger.getLogger(Application.class);
	
	static void init() {
		// init logger
		PropertyConfigurator.configure(CommonConstants.PATH_LOGGER_PROPERTY);
		// create stock quote directory
		File stockQuotesDir = new File(CommonConstants.PATH_COMPANY_INFO_OUTPUT);
		if(!stockQuotesDir.exists()) {
			stockQuotesDir.mkdirs();
		}
		
		// set time zone of EST
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
	}
	
	public static void main(String[] argv) throws IOException {
		init();
		logger.info("Startup Profit System...");
		CompanyInfoCollector.getAndUpdateCompanyFullInfoList(CommonConstants.LOAD_OPTION_DISK, CommonConstants.PERSIST_OPTION_DISK);
		
		logger.info("Stop Profit System!");
	}
}