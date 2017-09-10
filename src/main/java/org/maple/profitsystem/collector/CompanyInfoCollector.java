/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyInfoModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.utils.HttpRequestUtil;
import org.maple.profitsystem.utils.TradingDateUtil;

public class CompanyInfoCollector {
	
	private static Logger logger = Logger.getLogger(CompanyInfoCollector.class);
	
	private final static int REQUEST_MAX_RETRY_TIMES = 5; 
	
	public static List<CompanyInfoModel> fetchCompanyInfoListFromNasdaq() throws PSException {
		List<CompanyInfoModel> result = new ArrayList<>();
		String response = null;
		
		boolean requestSuccess = false;
		for(int i = 0; i < REQUEST_MAX_RETRY_TIMES && !requestSuccess; i++) {
			try {
				response = HttpRequestUtil.getMethod(CommonConstants.URL_GET_COMPANY_LIST_NASDAQ, null);
				requestSuccess = true;
			} catch (PSException e1) {
			}
		}
		if(!requestSuccess){
			throw new PSException("Request for fetching company list fail!");
		}
		String[] lines = response.split(CommonConstants.NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD);
		for(int i = 1; i < lines.length; ++i) {
			try{
				result.add(CompanyInfoModel.parseFromCSV(lines[i]));
			} catch(Exception e) {
				// This company is which for nasdaq test or had been bankrupted.
				//System.out.println(lines[i]);
			}
		}
		return result;
	}
	
	/**
	 * Fetch all stock quotes 
	 * 
	 * @param companyList
	 */
	public static void fetchStockQuotesByCompanyList(List<CompanyInfoModel> companyList) {
		List<CompanyInfoModel> failedList = new ArrayList<>();
		for(CompanyInfoModel company : companyList) {
			try {
				company.getQuoteList().addAll(fetchStockQuotesBySymbol(company));
				logger.info("Fetch success: " + company.getSymbol());
			} catch (PSException e) {
				failedList.add(company);
			}
			company.persistQuoteList("StockQuotes\\");
		}
		System.out.println("Failed Companies:" + failedList.size());
		for(CompanyInfoModel failedCompany : failedList) {
			System.out.println(failedCompany.getSymbol());
		}
	}
	
	public static List<StockQuoteModel> fetchStockQuotesBySymbol(CompanyInfoModel company) throws PSException {
		String responseStr = postStockQuotesStr(company.getSymbol(), company.getLastQuoteDt());
		String[] records = responseStr.split("[\r]?\n");
		
		List<StockQuoteModel> result = new ArrayList<>();
		
		for(int i = 2; i < records.length; ++i) {
			try {
				result.add(StockQuoteModel.parseFromTransportCSV(company.getSymbol(), records[i]));
			} catch (PSException e) {
				logger.error(e.getMessage());
			}
		}
		return result;
	}
	
	private static String combineStockQuotesUrl(String symbol) {
		return CommonConstants.URL_GET_STOCK_QUOTES_NASDAQ_PREFIX + symbol.toLowerCase() + CommonConstants.URL_GET_STOCK_QUOTES_NASDAQ_SUFFIX;
	}
	
	private static String postStockQuotesStr(String symbol, Integer startDt) throws PSException {
		String baseUrl = CompanyInfoCollector.combineStockQuotesUrl(symbol);
		
		final String DATA_FIELD_FIVE_DAY = "5d";
		final int FIVE_DAY_DAYS = 5;
		final String DATA_FIELD_SIX_MONTH = "6m";
		final int SIX_MONTH_DAYS = 150;
		final String DATA_FIELD_ONE_YEAR = "1y";
		final int ONE_YEAR_DAYS = 365;
		final String DATA_FIELD_TEN_YEAR = "10y";
		
		String fieldDate = null;
		if(startDt <= 0) {
			fieldDate = DATA_FIELD_TEN_YEAR;
		} else {
			int intervalTradingDays = TradingDateUtil.betweenTradingDays(TradingDateUtil.convertNumDate2Date(startDt), new Date());
			if(intervalTradingDays < FIVE_DAY_DAYS) {
				fieldDate = DATA_FIELD_FIVE_DAY;
			} else if(intervalTradingDays < SIX_MONTH_DAYS) {
				fieldDate = DATA_FIELD_SIX_MONTH;
			} else if(intervalTradingDays < ONE_YEAR_DAYS) {
				fieldDate = DATA_FIELD_ONE_YEAR;
			} else {
				fieldDate = DATA_FIELD_TEN_YEAR;
			}
		}
		
		String postData = fieldDate + "|true|" + symbol.toUpperCase();
		Map<String, String> propertyMap = new HashMap<>();
		propertyMap.put("Content-Type", "application/json");
		
		String result = null;
		
		boolean requestSuccess = false;
		for(int i = 0; i < REQUEST_MAX_RETRY_TIMES && !requestSuccess; i++) {
			try {
				result = HttpRequestUtil.postMethod(baseUrl, propertyMap, postData);
				requestSuccess = true;
			} catch (PSException e1) {
			}
		}
		if(!requestSuccess){
			throw new PSException("Request for fetching stock quotes fail: " + symbol);
		}
		return result;
	}
}