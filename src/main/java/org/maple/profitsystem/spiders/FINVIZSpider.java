package org.maple.profitsystem.spiders;

import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.utils.HttpRequestUtil;

public class FINVIZSpider {
	
	private final static String URL_GET_COMPANY_STATISTICS_FINVIZ = "https://www.finviz.com/quote.ashx?t=";
	
	private final static int MAX_RETRY_TIMES = 5;
	
	private final static String INSIDER_OWN_REG = "Insider Own.*?<b>(.*?)</b>";
	
	private static String getURLOfCompanyStatistics(String symbol) {
		return URL_GET_COMPANY_STATISTICS_FINVIZ + symbol;
	}

	public static void fetchCompanyStatistics(String symbol) throws PSException {
		String response = HttpRequestUtil.getMethod(getURLOfCompanyStatistics(symbol), null, MAX_RETRY_TIMES);
		
	}
}
