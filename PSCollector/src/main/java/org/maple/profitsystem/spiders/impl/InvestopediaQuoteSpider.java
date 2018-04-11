package org.maple.profitsystem.spiders.impl;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.spiders.QuoteSpider;
import org.maple.profitsystem.utils.CSVUtil;
import org.maple.profitsystem.utils.HttpRequestUtil;
import org.maple.profitsystem.utils.TradingDateUtil;

public class InvestopediaQuoteSpider implements QuoteSpider{
	
	private static Logger logger = Logger.getLogger(InvestopediaQuoteSpider.class);

	private static String BASE_URL = "https://www.investopedia.com/markets/api/partial/historical/?Type=Historical+Prices&Timeframe=Daily";
	
	private static String PARAM_DATE_FORMAT = "MMM dd, yyyy";
	
	private SimpleDateFormat sdf = new SimpleDateFormat(PARAM_DATE_FORMAT, Locale.ENGLISH);
	
	@Override
	public List<StockQuoteModel> fetchQuotes(String symbol, Integer startDt) throws PSException {
		final String TABLE_REGX_STR = "</th>[\\s]*</tr>([\\s\\S]*)</tbody>";
		
		try {
			String baseUrl = combineTargetUrl(symbol, startDt);
			String responseStr = HttpRequestUtil.getMethod(baseUrl, null, CommonConstants.REQUEST_MAX_RETRY_TIMES);
			
			List<StockQuoteModel> result = new ArrayList<>();
			
			Pattern r = Pattern.compile(TABLE_REGX_STR);
			Matcher m = r.matcher(responseStr);
			if(!m.find()) {
				throw new PSException(symbol + " - content error: " + responseStr.substring(100));
			}
			// parse html table to csv
			String csv = m.group(1).replaceAll("<tr.*?>\\s*<td.*?>", "");
			csv = csv.replaceAll("</td>\\s*?<td.*?>", CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD);
			csv = csv.replaceAll("\r?\n", "");
			csv = csv.replaceAll("</td>\\s*</tr>", "\n");
			
			// parse all csv records to model
			
			String[] records = csv.split(CommonConstants.CSV_NEWLINE_REG);
			for(int i = 0; i < records.length; ++i) {
				try {
					StockQuoteModel tmp = parseFromHtmlCSV(records[i]);
					if(tmp.getQuoteDate() <= startDt) {
						break;
					}
					result.add(tmp);
					
				} catch (Exception e) {
					logger.error(symbol + " parse record failed: " + records[i]);
				}
			}
			return result;
		} catch(Exception e) {
			throw new PSException(symbol + " - get quote failed: " + e.getMessage());
		}
	}

	private StockQuoteModel parseFromHtmlCSV(String csvRecord) throws Exception {
		String[] fields = CSVUtil.splitCSVRecord(csvRecord);
		try{
			StockQuoteModel result = new StockQuoteModel();
			result.setQuoteDate(TradingDateUtil.convertDate2NumDate(sdf.parse(fields[0].trim())));
			result.setOpen(Double.valueOf(fields[1].trim().replaceAll(",", "")));
			result.setHigh(Double.valueOf(fields[2].trim().replaceAll(",", "")));
			result.setLow(Double.valueOf(fields[3].trim().replaceAll(",", "")));
			result.setClose(Double.valueOf(fields[4].trim().replaceAll(",", "")));
			result.setVolume(Integer.valueOf(fields[5].trim().replaceAll(",", "")));
			
			return result;
		} catch(Exception e) {
			throw e;
		}		
	}
	
	@SuppressWarnings("deprecation")
	private String combineTargetUrl(String symbol, int startDt) {
		final int OLDEST_DATE = 19600101;
		// reset startDt
		if(startDt < OLDEST_DATE) {
			startDt = OLDEST_DATE;
		}
		// Date format
		Date startDate = TradingDateUtil.convertNumDate2Date(startDt);
		Date endDate = new Date();
		
		return BASE_URL + "&Symbol=" + symbol + "&StartDate=" + URLEncoder.encode(sdf.format(startDate)) + "&EndDate=" + URLEncoder.encode(sdf.format(endDate));
		
	}
}