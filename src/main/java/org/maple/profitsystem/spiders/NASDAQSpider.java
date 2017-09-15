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
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
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
	 * @throws HttpException 
	 */
	public static List<CompanyModel> fetchCompanyListWithBaseInfo() throws HttpException {
		final CharSequence INVALID_COMPANY_CHAR = "^";
		
		List<CompanyModel> result = new ArrayList<>();
		
		String response = HttpRequestUtil.getMethod(CommonConstants.URL_GET_COMPANY_LIST_NASDAQ, null, REQUEST_MAX_RETRY_TIMES);
		String[] lines = response.split(CommonConstants.NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD);
		for(int i = 1; i < lines.length; ++i) {
			try{
				result.add(CompanyModel.parseFromTransportCSV(lines[i]));
			} catch(Exception e) {
				// This company is which for nasdaq test or had been bankrupted.
				logger.error("Invalid company: " + lines[i]);
			}
		}
		// filter invalid company
		return result.stream().filter(company -> !company.getSymbol().contains(INVALID_COMPANY_CHAR)).collect(Collectors.toList());
	}
	
	
	/**
	 * Fetch the stock quotes newer than all of quotes in CompanyInfoModel.quoteList.
	 * 
	 * @param company
	 * @return List of StockQuoteModel
	 * @throws PSException
	 * @throws HttpException 
	 */
	public static List<StockQuoteModel> fetchNewestStockQuotesByCompany(CompanyModel company) throws PSException, HttpException {
		List<StockQuoteModel> result = new ArrayList<>();
		String responseStr = fetchHistoricalQuotes(company.getSymbol(), company.getLastQuoteDt());
		if(responseStr == null)
			return result;
		
		// parse response and get quote list
		String[] records = responseStr.split(CommonConstants.CSV_NEWLINE_REG);
		if(records.length <= 5) {
			throw new PSException(company.getSymbol() + ": Content of quote list error!");
		}
		for(int i = 2; i < records.length; ++i) {
			try {
				StockQuoteModel tmp = StockQuoteModel.parseFromTransportCSV(records[i]);
				// check whether the quote existed 
				if(tmp.getQuoteDate() <= company.getLastQuoteDt()) {
					break;
				} else {
					result.add(tmp);
				}
			} catch (PSException e) {
				logger.error("Parse a quote record failed - " + company.getSymbol() + ": " + records[i]);
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
	 * @throws HttpException 
	 */
	private static String fetchHistoricalQuotes(String symbol, Integer startDt) throws HttpException {
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
		
		return HttpRequestUtil.postMethod(baseUrl, propertyMap, postData, REQUEST_MAX_RETRY_TIMES);
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