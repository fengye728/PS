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
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.maple.profitsystem.collector.CompanyInfoCollector;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyInfoModel;

public class Application {
	
	static void init() {
		// init logger
		PropertyConfigurator.configure(CommonConstants.LOGGER_PROPERTY_PATH);
		File stockQuotesDir = new File(CommonConstants.STOCK_QUOTES_OUTPUT_PATH);
		if(!stockQuotesDir.exists()) {
			stockQuotesDir.mkdirs();
		}
		
	}
	
	public static void main(String[] argv) throws IOException {
		init();
		
		List<CompanyInfoModel> list;
		try {
			list = CompanyInfoCollector.fetchCompanyInfoListFromNasdaq();
			CompanyInfoCollector.fetchStockQuotesByCompanyList(list);
		} catch (PSException e) {
			e.printStackTrace();
		}
	
	}
}