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
import org.maple.profitsystem.models.RoicModel;
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
	
	private final static int ENTRY_WAIT_DAYS_AFTER_SPIKE = 3;
	
	public static int EXIT_MAX_WAIT_DAYS = 30;
	
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
	
	public Double evaluateByCCI(EVBBSystemResult evbbResult) throws PSException {
		final int MAX_PERIOD = 30;
		List<StockQuoteModel> quotes = evbbResult.getCompany().getQuoteList();
		int len = quotes.size() < evbbResult.getDayIndex() + MAX_PERIOD ? quotes.size() : evbbResult.getDayIndex() + MAX_PERIOD;
		
		double lastCCI = 0;
		int i = evbbResult.getDayIndex();
		for(; i < len && lastCCI >= 0; i++) {
			lastCCI = TAUtil.CCI(quotes, i);
		}
		--i;
		if(i <= evbbResult.getDayIndex()) {
			return null;
		}
		double entryP = entryPrice(evbbResult);
		return (quotes.get(i).getOpen() - entryP) / entryP;
	}
	
	/**
	 * Leave when the next day open.
	 * @param evbbResult
	 * @return
	 * @throws PSException 
	 */
	public RoicModel evaluateByTDD(EVBBSystemResult evbbResult) throws PSException {
		Integer entryIndex = getEntryDateIndex(evbbResult);
		if(null == entryIndex) {
			return null;
		}
		
		double entryP = entryPrice(evbbResult);
		RoicModel result = new RoicModel();
		
		Integer exitIndex = getExitSignalDateIndexByTDD(evbbResult.getDayIndex(), evbbResult);
		if(null == exitIndex) {
			// no signal for exitting
			return null;
		} else if(exitIndex < entryIndex) {
			// haven't entry
			return null;
		} else if(exitIndex == evbbResult.getCompany().getQuoteList().size() - 1) {
			// need to leave in next day
			return null;
		} else {
			++exitIndex;
		}
		
		double exitP = evbbResult.getCompany().getQuoteList().get(exitIndex).getOpen();
		
		double roic = (exitP - entryP) / entryP;
		
		
		result.setSymbol(evbbResult.getCompany().getSymbol());
		result.setSector(evbbResult.getCompany().getSector());
		result.setRoic(roic);
		result.setDays(exitIndex - entryIndex);
		result.setEntryIndex(entryIndex);
		
		return result;
	}
	
	/**
	 * Check if the evbbResult can entry.
	 * 
	 * @param evbbResult
	 * @return Index of entry date in quotes if entry success, null otherwise.  
	 */
	public Integer getEntryDateIndex(EVBBSystemResult evbbResult) {
		StockQuoteModel quote = evbbResult.getCompany().getQuoteList().get(evbbResult.getDayIndex());
		
		// TODO Any no trading before 30 days
		if(hasNoTradingDayBefore(evbbResult.getCompany(), evbbResult.getDayIndex())) {
			return null;
		}
		
		// TODO FDO Filter
//		try {
//			if(TAUtil.FiveDayOscillator(evbbResult.getCompany().getQuoteList(), evbbResult.getDayIndex()) < 70) {
//				return null;
//			}
//		} catch (PSException e) {
//			e.printStackTrace();
//		}

		double point = entryPrice(evbbResult);
		
		int leftDays = Math.min(ENTRY_WAIT_DAYS_AFTER_SPIKE, evbbResult.getCompany().getQuoteList().size() - evbbResult.getDayIndex() - 1);
		for(int i = 1; i <= leftDays; ++i) {
			quote = evbbResult.getCompany().getQuoteList().get(evbbResult.getDayIndex() + i);
			if(quote.getLow() <= point) {
				return evbbResult.getDayIndex() + i;
			}
		}
		return null;
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
	
	/**
	 * Get index of exit date by Three Days Difference.
	 * @param evbbResult
	 * @return The index of exit date satisfied the TDD requirements or reach the max wait days.
	 * @throws PSException 
	 */
	private Integer getExitSignalDateIndexByTDD(int entryIndex, EVBBSystemResult evbbResult) throws PSException {
		final double TDD_THRESHOLD = 0;
		final int FDO_BEARISH_THRESHOLD = 30; 
		// Leave when second TDD is negative.
		final int NEGATIVE_TIME_THRESHOLD = 2;
		
		List<StockQuoteModel> quotes = evbbResult.getCompany().getQuoteList();
		
		int negativeCount = 0;
		boolean lastOverThreshold = true;
		for(int i = entryIndex; i < quotes.size(); ++i) {
			double fdo = TAUtil.FiveDayOscillator(quotes, i);
			double tdd = TAUtil.ThreeDayDifference(quotes, i);
			
			if(tdd <= TDD_THRESHOLD) {
				if(lastOverThreshold || fdo < FDO_BEARISH_THRESHOLD) {
					lastOverThreshold = false;
					++negativeCount;
				}
				if(negativeCount >= NEGATIVE_TIME_THRESHOLD) {
					return i;
				}
			} else {
				lastOverThreshold = true;
			}
		}
		
		// null if not yet exit
		return null;
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
		final int BEFORE_DAYS = 30;
		
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
		final int VOLUME_THRESHOLD = 300000;
		final int SMA_DAYS = 50;
		
		if(targetIndex < SMA_DAYS) {
			return false;
		}
		
		try {
			if(TAUtil.SMAVolumeByIndex(company.getQuoteList(), targetIndex - 1, SMA_DAYS) <= VOLUME_THRESHOLD) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
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
		try {
			double ema = TAUtil.EMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS);
			double sma = TAUtil.SMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS);
			//if(sma < ema && ema < sma *  SMA_TIMES) {
			if(ema < sma *  SMA_TIMES) {
				return true;
			} else {
				return false;
			}
		} catch (PSException e) {
			return false;
		}
	}
	
}