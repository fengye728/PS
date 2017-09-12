/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.spiders;

import java.util.ArrayList;
import java.util.Calendar;
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

public class NASDAQSpider {
	
	private static Logger logger = Logger.getLogger(NASDAQSpider.class);
	
	private final static int REQUEST_MAX_RETRY_TIMES = 5;
	
	/**
	 * Fetch a list of companies from nasdaq.
	 * 
	 * @return List of CompanyInfoModel, otherwise a empty list.
	 * @throws PSException
	 */
	public static List<CompanyInfoModel> fetchCompanyListWithBaseInfo() {
		List<CompanyInfoModel> result = new ArrayList<>();
		
		try {
			String response = HttpRequestUtil.getMethod(CommonConstants.URL_GET_COMPANY_LIST_NASDAQ, null, REQUEST_MAX_RETRY_TIMES);
			String[] lines = response.split(CommonConstants.NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD);
			for(int i = 1; i < lines.length; ++i) {
				try{
					result.add(CompanyInfoModel.parseFromTransportCSV(lines[i]));
				} catch(Exception e) {
					// This company is which for nasdaq test or had been bankrupted.
					logger.error("Invalid company: " + lines[i]);
				}
			}
			return result;
		} catch (PSException e1) {
			logger.error(e1.getMessage());
			return result;
		}
	}
	
	/**
	 * Fetch the stock quotes newer than all of quotes in CompanyInfoModel.quoteList.
	 * 
	 * @param company
	 * @return List of StockQuoteModel
	 * @throws PSException
	 */
	public static List<StockQuoteModel> fetchNewestStockQuotesByCompany(CompanyInfoModel company) throws PSException {
		List<StockQuoteModel> result = new ArrayList<>();
		String responseStr = fetchHistoricalQuotes(company.getSymbol(), company.getLastQuoteDt());
		if(responseStr == null)
			return result;
		
		// parse response and get quote list
		String[] records = responseStr.split(CommonConstants.NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD);
		for(int i = 2; i < records.length; ++i) {
			try {
				StockQuoteModel tmp = StockQuoteModel.parseFromTransportCSV(company.getSymbol(), records[i]);
				// check whether the quote existed 
				if(tmp.getQuoteDate() <= company.getLastQuoteDt()) {
					break;
				} else {
					result.add(tmp);
				}
			} catch (PSException e) {
				logger.error(company.getSymbol() + ": " + records[i]);
			}
		}
		return result;
	}
	
	/**
	 * Get string response for getting stock quotes from startDt to now date by http post method. 
	 * 
	 * @param symbol
	 * @param startDt
	 * @return
	 * @throws PSException
	 */
	private static String fetchHistoricalQuotes(String symbol, Integer startDt) throws PSException {
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
			Calendar now = Calendar.getInstance();
			if(!TradingDateUtil.hasMarketOpened(now)) {
				now.add(Calendar.DAY_OF_MONTH, -1);
			}
			int intervalTradingDays = TradingDateUtil.betweenTradingDays(TradingDateUtil.convertNumDate2Date(startDt), now.getTime());
			if(intervalTradingDays <= 0) {
				return null;
			} else if(intervalTradingDays < FIVE_DAY_DAYS) {
				fieldDate = DATA_FIELD_FIVE_DAY;
			} else if(intervalTradingDays < SIX_MONTH_DAYS) {
				fieldDate = DATA_FIELD_SIX_MONTH;
			} else if(intervalTradingDays < ONE_YEAR_DAYS) {
				fieldDate = DATA_FIELD_ONE_YEAR;
			} else {
				fieldDate = DATA_FIELD_TEN_YEAR;
			}
		}
		
		String baseUrl = combineHistoricalQuotesUrl(symbol);
		String postData = fieldDate + "|true|" + symbol.toUpperCase();
		Map<String, String> propertyMap = new HashMap<>();
		propertyMap.put("Content-Type", "application/json");
		
		String result = null;
		try {
			result = HttpRequestUtil.postMethod(baseUrl, propertyMap, postData, REQUEST_MAX_RETRY_TIMES);
		} catch (PSException e1) {
			throw new PSException("Request for fetching stock quotes fail: " + symbol);
		}

		return result;
	}
	
	/**
	 * Combine the url to fetch historical quotes of the symbol specified stock.
	 * 
	 * @param symbol
	 * @return
	 */
	private static String combineHistoricalQuotesUrl(String symbol) {
		return CommonConstants.URL_GET_STOCK_QUOTES_NASDAQ_PREFIX + symbol.toLowerCase() + CommonConstants.URL_GET_STOCK_QUOTES_NASDAQ_SUFFIX;
	}
}