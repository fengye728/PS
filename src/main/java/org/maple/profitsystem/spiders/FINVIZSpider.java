package org.maple.profitsystem.spiders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyStatisticsModel;
import org.maple.profitsystem.utils.HttpRequestUtil;

public class FINVIZSpider {
	
	private final static String URL_GET_COMPANY_STATISTICS_FINVIZ = "https://www.finviz.com/quote.ashx?t=";
	
	private final static int MAX_RETRY_TIMES = 5;
	
	private final static String INSIDER_OWN_REG = "Insider Own</td>.*?<b>(.*?)</b>";
	
	private final static String INST_OWN_REG = "Inst Own</td>.*?<b>(.*?)</b>";
	
	private final static String SHS_OUTSTAND_REG = "Inst Own</td>.*?<b>(.*?)</b>";
	
	private final static String SHS_FLOAT_REG = "Shs Float</td>.*?<b>(.*?)</b>";
	
	private static String getURLOfCompanyStatistics(String symbol) {
		return URL_GET_COMPANY_STATISTICS_FINVIZ + symbol;
	}

	/**
	 * Fetch the statistics of the specified company by symbol.
	 * 
	 * @param symbol
	 * @return
	 * @throws HttpException
	 * @throws PSException
	 */
	public static CompanyStatisticsModel fetchCompanyStatistics(String symbol) throws HttpException, PSException {
		CompanyStatisticsModel result = new CompanyStatisticsModel();
		String response = HttpRequestUtil.getMethod(getURLOfCompanyStatistics(symbol), null, MAX_RETRY_TIMES);
		
		Pattern insiderOwnPat = Pattern.compile(INSIDER_OWN_REG);
		Pattern instOwnPat = Pattern.compile(INST_OWN_REG);
		Pattern shsFloatPat = Pattern.compile(SHS_FLOAT_REG);
		
		int count = 0;
		// match insider ownership
		Matcher tmpMatcher = insiderOwnPat.matcher(response);
		if(tmpMatcher.find()) {
			result.setInsiderOwnPerc(convertStringPerc2DoublePerc(tmpMatcher.group(1)));
			++count;
		}
		
		// match institutional ownership
		tmpMatcher = instOwnPat.matcher(response);
		if(tmpMatcher.find()) {
			result.setInstOwnPerc(convertStringPerc2DoublePerc(tmpMatcher.group(1)));
			++count;
		}
		
		// TODO match shares outstanding
		
		// match float shares 
		tmpMatcher = shsFloatPat.matcher(response);
		if(tmpMatcher.find()) {
			result.setShsFloat(converDisplayNum2Integer(tmpMatcher.group(1)));
			++count;
		}
		
		if(count == 0) {
			throw new PSException("No statistics info: " + symbol);
		}
		return result;
	}
	
	private static Double convertStringPerc2DoublePerc(String perc) {
		try {
			return Double.valueOf(perc.substring(0, perc.length() - 1));
		} catch(Exception e) {
			return null;
		}
	}
	

	private static Integer converDisplayNum2Integer(String dispNum) {
		try{
			double number = Double.valueOf(dispNum.substring(0, dispNum.length() - 1));
			int multiplier = 0;
			switch(Character.toUpperCase(dispNum.charAt(dispNum.length() - 1))) {
			case 'K':
				multiplier = 1000;
				break;
			case 'M':
				multiplier = 1000000;
				break;
			case 'B':
				multiplier = 1000000000;
				break;
			}
			return (int) (number * multiplier);
		} catch(Exception e) {
			return null;
		}
	}
	
}
