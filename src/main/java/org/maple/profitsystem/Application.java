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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.PropertyConfigurator;
import org.maple.profitsystem.collector.CompanyInfoCollector;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.models.CompanyInfoModel;

public class Application {
	
	static void init() {
		// init logger
		PropertyConfigurator.configure(CommonConstants.LOGGER_PROPERTY_PATH);
		// create stock quote directory
		File stockQuotesDir = new File(CommonConstants.STOCK_QUOTES_OUTPUT_PATH);
		if(!stockQuotesDir.exists()) {
			stockQuotesDir.mkdirs();
		}
		
		// set time zone of EST
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		
//		Calendar now = Calendar.getInstance();
//		now.add(Calendar.DAY_OF_MONTH, -20);
//		
//		System.out.print(now.getTime());
	}
	
	static List<CompanyInfoModel> updateAll() {
		List<CompanyInfoModel> listOld = CompanyInfoCollector.loadFullCompanyInfoListFromDisk(CommonConstants.STOCK_QUOTES_OUTPUT_PATH);
		List<CompanyInfoModel> listNew = CompanyInfoCollector.fetchCompanyInfoListFromNasdaq();
		
		listOld.addAll(listNew);
		// remove duplicate objects
		HashSet<CompanyInfoModel> set = new HashSet<>(listOld);
		List<CompanyInfoModel> result = new ArrayList<>(set);
		
		
		CompanyInfoCollector.fetchNewestStockQuotesByCompanyList(result);
		return result;
	}
	
	public static void main(String[] argv) throws IOException {
		init();
		
		List<CompanyInfoModel> list = updateAll();
		
		System.out.println(list);
	
	}
}