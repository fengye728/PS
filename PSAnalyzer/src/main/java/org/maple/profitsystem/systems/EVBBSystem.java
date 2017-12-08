/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.systems;

import java.util.LinkedList;
import java.util.List;

import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.utils.TAUtil;
import org.springframework.stereotype.Component;

/**
 * Explosive volume based breakout system.
 * 
 * @author Maple
 *
 */
@Component
public class EVBBSystem {
	
	// Total 12
	public final static int THRESOLD = 11;
	
	private final static int ENTRY_WAIT_DAYS_AFTER_SPIKE = 60;
	
	/**
	 * Analyzes the company and finds the all moments which satisfied the EVBBSystem's condition
	 * 
	 * @param company
	 * @return
	 */
	public List<EVBBSystemResult> analyzeAll(CompanyModel company) {

		if(company == null || company.getQuoteList() == null) {
			return null;
		}
		List<EVBBSystemResult> result = new LinkedList<EVBBSystemResult>();
		List<StockQuoteModel> quotes = company.getQuoteList();
		
		for(int i = 0; i < quotes.size(); ++i) {
			EVBBSystemResult tmp = analyzeByIndex(company, i);
			if(tmp != null) {
				result.add(tmp);
			}
		}
		return result;
	}	
	
	
	/**
	 * Analyzes the company and finds the old moments which satisfied the EVBBSystem's condition
	 * 
	 * @param company
	 * @return
	 */
	public List<EVBBSystemResult> analyzeBefore(CompanyModel company) {

		if(company == null || company.getQuoteList() == null) {
			return null;
		}
		List<EVBBSystemResult> result = new LinkedList<EVBBSystemResult>();
		List<StockQuoteModel> quotes = company.getQuoteList();
		
		int end = getStartIndexForWatch(quotes);
		for(int i = 0; i < end; ++i) {
			EVBBSystemResult tmp = analyzeByIndex(company, i);
			if(tmp != null) {
				result.add(tmp);
			}
		}
		return result;
	}
	
	/**
	 * Analyzes the company and finds the newest moments which satisfied the EVBBSystem's condition
	 * 
	 * @param company
	 * @return
	 */
	public List<EVBBSystemResult> analyzeLast(CompanyModel company) {
		if(company == null || company.getQuoteList() == null) {
			return null;
		}
		List<EVBBSystemResult> result = new LinkedList<EVBBSystemResult>();
		List<StockQuoteModel> quotes = company.getQuoteList();
		int start = getStartIndexForWatch(quotes);

		for(int i = start; i < quotes.size(); ++i) {
			EVBBSystemResult tmp = analyzeByIndex(company, i);
			if(tmp != null) {
				result.add(tmp);
			}
		}
		return result;
	}
	
	/**
	 * Get the limit price of entry price.
	 * @param evbbResult
	 * @return
	 */
	public double entryPrice(EVBBSystemResult evbbResult) {
		StockQuoteModel quote = evbbResult.getCompany().getQuoteList().get(evbbResult.getDayIndex());
		return (quote.getHigh() + quote.getLow()) / 2;
	}
	
	// ---------------------- Private Functions ----------------------------------
	
	/**
	 * Get the start index for quotes need to watch.
	 * @param size
	 * @return
	 */
	private int getStartIndexForWatch(List<StockQuoteModel> quotes) {
		return Math.max(quotes.size() - ENTRY_WAIT_DAYS_AFTER_SPIKE, 0);
	}
	
	/**
	 * Analyze the date that quoteIndex specified of the company.
	 * 
	 * @param company
	 * @param quoteIndex
	 * @return
	 */
	private EVBBSystemResult analyzeByIndex(CompanyModel company, int quoteIndex) {
		
		if(hasNoTradingDayBefore(company, quoteIndex)) {
			return null;
		}
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
	
	private boolean hasNoTradingDayBefore(CompanyModel company, int targetIndex) {
		final int BEFORE_DAYS = 60;
		
		int beforeDays = Math.min(BEFORE_DAYS, targetIndex);
		List<StockQuoteModel> quotes = company.getQuoteList();
		for(int i = targetIndex - beforeDays; i < targetIndex - 1; ++i) {
			if(quotes.get(i).getVolume() == 0) {
				return true;
			}
		}
		return false;
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
		final int PAST_YEAR_DAYS = 250;
		final int MULTIPLE_THRESHOLD = 2;
		final int SMA_DAYS = 50;
		if(targetIndex < PAST_YEAR_DAYS) {
			return false;
		}
		List<StockQuoteModel> quotes = company.getQuoteList();
			
		// get volume of max volume resistance
		Integer maxVolumeOfResistance;
		try {
			maxVolumeOfResistance = TAUtil.MaxResistanceVolumeByIndex(quotes, targetIndex, PAST_YEAR_DAYS);
			if(maxVolumeOfResistance <= TAUtil.SMAVolumeByIndex(quotes, targetIndex, SMA_DAYS) * MULTIPLE_THRESHOLD) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
			return false;
		}
		
	}
	
	private boolean testHighBreakoutPrice(CompanyModel company, int targetIndex) {
		final int DAYS_OF_MAX_HIGH_PRICE = 60;
		
		if(targetIndex < DAYS_OF_MAX_HIGH_PRICE) {
			return false;
		}
		
		List<StockQuoteModel> quotes = company.getQuoteList();
		try {
			if(quotes.get(targetIndex).getClose() > TAUtil.MaxHighPriceByIndex(quotes, targetIndex - 1, DAYS_OF_MAX_HIGH_PRICE)) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
			return false;
		}
	}
	
	private boolean testLowSMAVolume(CompanyModel company, int targetIndex) {
		final int VOLUME_THRESHOLD = 30000;
		final int SMA_DAYS = 50; 
		
		if(targetIndex < SMA_DAYS) {
			return false;
		}
		
		try {
			if(TAUtil.SMAVolumeByIndex(company.getQuoteList(), targetIndex - 1, SMA_DAYS) >= VOLUME_THRESHOLD) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
			return false;
		}
//		final int VOLUME_THRESHOLD = 300000;
//		final int SMA_DAYS = 50; 
//		
//		if(targetIndex < SMA_DAYS) {
//			return false;
//		}
//		
//		try {
//			if(TAUtil.SMAVolumeByIndex(company.getQuoteList(), targetIndex - 1, SMA_DAYS) <= VOLUME_THRESHOLD) {
//				return true;
//			} else {
//				return false;
//			}
//		} catch (PSException e) {
//			return false;
//		}
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
		final int SMA_DAYS = 50;
		try {
			double price = TAUtil.SMAPriceByIndex(company.getQuoteList(), targetIndex, SMA_DAYS);
			if(LOW_PRICE < price) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
			return false;
		}
	}
	
	private boolean testBelowHighPriceLimit(CompanyModel company, int targetIndex) {
		final double HIGH_PRICE = 25.0;
		final int SMA_DAYS = 50;
		try {
			double price = TAUtil.SMAPriceByIndex(company.getQuoteList(), targetIndex, SMA_DAYS);
			if(HIGH_PRICE > price) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
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
		try {
			if(curQuote.getVolume() >= beforeQuote.getVolume() * VOLUME_TIMES 
					&& curQuote.getVolume() >= TAUtil.SMAVolumeByIndex(company.getQuoteList(), targetIndex, 50)) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
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
		try {
			double ema = TAUtil.EMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS);
			double sma = TAUtil.SMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS);
			if(sma < ema && ema < sma *  SMA_TIMES) {
			//if(ema < sma *  SMA_TIMES) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
			return false;
		}
	}
	
}