/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.systems;

import java.util.ArrayList;
import java.util.List;

import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.utils.TAUtil;
import org.springframework.stereotype.Component;

/**
 * Explosive volume based breakouts system.
 * 
 * @author Maple
 *
 */
@Component
public class EVBBSystem {
	
	public final static int THRESOLD = 11;
	
	/**
	 * Analyzes the company and finds all the moments which satisfied the EVBBSystem's condition
	 * 
	 * @param company
	 * @return
	 */
	public List<EVBBSystemResult> analyzeAll(CompanyModel company) {
		ArrayList<EVBBSystemResult> result = new ArrayList<EVBBSystemResult>();
		if(company == null || company.getQuoteList() == null) {
			return result;
		}
		List<StockQuoteModel> quotes = company.getQuoteList();
		for(int i = quotes.size() - 1; i >= 0; --i) {
			EVBBSystemResult tmp = analyzeByIndex(company, i);
			if(tmp != null) {
				result.add(tmp);
			}
		}
		return result;
	}
	
	public EVBBSystemResult analyzeLast(CompanyModel company) {
		return null;
	}

	private EVBBSystemResult analyzeByIndex(CompanyModel company, int quoteIndex) {
		EVBBSystemResult result = new EVBBSystemResult();
		
		// fundamental requirements
		result.setInsiderOwership(testInsiderOwnership(company));
		result.setLowFloat(testLowFloat(company));
		result.setNoNews(testNoNews(company, quoteIndex));
		
		// technical requirements
		result.setLowResistance(testLowResistance(company, quoteIndex));
		result.setHighBreakoutPrice(testHighBreakoutPrice(company, quoteIndex));
		result.setLowSMAVolume(testLowSMAVolume(company, quoteIndex));
		result.setIntradayUp(testIntradayUp(company, quoteIndex));
		result.setBeforeUp(testBeforeDayUp(company, quoteIndex));
		result.setAboveLowPriceLimit(testAboveLowPriceLimit(company, quoteIndex));
		result.setBelowHighPriceLimit(testBelowHighPriceLimit(company, quoteIndex));
		result.setSpikeVolume(testSpikeVolume(company, quoteIndex));
		result.setEmaSmaCorelative(testEMAAndSMA(company, quoteIndex));
		
		if(result.isSatisfied()) {
			// set base info
			result.setCompany(company);
			result.setDayIndex(quoteIndex);
			
			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * Evaluates the system's success rate.
	 * 
	 * @param company
	 * @return
	 */
	public EVBBSystemResult evaluate(EVBBSystemResult evbbResult) {
		return null;
	}
	
	
	
	private boolean testInsiderOwnership(CompanyModel company) {
		final int THRESHOLD = 10;
		if(company.getStatistics().getInsiderOwnPerc() != null && company.getStatistics().getInsiderOwnPerc() >= THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testLowFloat(CompanyModel company) {
		final int THRESHOLD = 35000000;
		if(company.getStatistics().getShsFloat() != null && company.getStatistics().getShsFloat() <= THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testNoNews(CompanyModel company, int targetIndex) {
		// TODO 
		return false;
	}
	
	private boolean testLowResistance(CompanyModel company, int targetIndex) {
		final int PAST_YEAR_DAYS = 260;
		final int MULTIPLE_THRESHOLD = 2;
		final int SMA_DAYS = 50;
		if(targetIndex < PAST_YEAR_DAYS) {
			return false;
		}
		List<StockQuoteModel> quotes = company.getQuoteList();
			
		// get volume of max volume resistance
		int maxVolumeOfResistance = TAUtil.MaxResistanceVolumeByIndex(quotes, targetIndex, PAST_YEAR_DAYS);
		
		if(maxVolumeOfResistance > 0 && maxVolumeOfResistance * MULTIPLE_THRESHOLD <= TAUtil.SMAVolumeByIndex(quotes, targetIndex, SMA_DAYS)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testHighBreakoutPrice(CompanyModel company, int targetIndex) {
		final int DAYS_OF_MAX_HIGH_PRICE = 60;
		
		if(targetIndex < DAYS_OF_MAX_HIGH_PRICE) {
			return false;
		}
		
		List<StockQuoteModel> quotes = company.getQuoteList();
		if(quotes.get(targetIndex).getClose() > TAUtil.MaxHighPriceByIndex(quotes, targetIndex - 1, DAYS_OF_MAX_HIGH_PRICE)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testLowSMAVolume(CompanyModel company, int targetIndex) {
		final int VOLUME_THRESHOLD = 300000;
		final int SMA_DAYS = 50;
		
		if(targetIndex < SMA_DAYS) {
			return false;
		}
		
		if(TAUtil.SMAVolumeByIndex(company.getQuoteList(), targetIndex - 1, SMA_DAYS) <= VOLUME_THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testIntradayUp(CompanyModel company, int targetIndex) {
		StockQuoteModel quote = company.getQuoteList().get(targetIndex);
		if(quote.getOpen() < quote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testBeforeDayUp(CompanyModel company, int targetIndex) {
		if(targetIndex <= 0) {
			return false;
		}
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		StockQuoteModel beforeQuote = company.getQuoteList().get(targetIndex - 1);
		if(beforeQuote.getClose() < curQuote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testAboveLowPriceLimit(CompanyModel company, int targetIndex) {
		final double LOW_PRICE = 2.0;
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		if(LOW_PRICE < curQuote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testBelowHighPriceLimit(CompanyModel company, int targetIndex) {
		final double HIGH_PRICE = 25.0;
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		if(HIGH_PRICE > curQuote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testSpikeVolume(CompanyModel company, int targetIndex) {
		if(targetIndex <= 0) {
			return false;
		}
		
		final int VOLUME_TIMES = 3;
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		StockQuoteModel beforeQuote = company.getQuoteList().get(targetIndex - 1);
		if(curQuote.getVolume() >= beforeQuote.getVolume() * VOLUME_TIMES) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testEMAAndSMA(CompanyModel company, int targetIndex) {
		final double SMA_TIMES = 1.5;
		final int EMA_SMA_DAYS = 50;
		
		if(targetIndex < EMA_SMA_DAYS) {
			return false;
		}
		List<StockQuoteModel> quotes = company.getQuoteList();
		if(TAUtil.EMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS) <= TAUtil.SMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS) * SMA_TIMES) {
			return true;
		} else {
			return false;
		}
	}
	
}