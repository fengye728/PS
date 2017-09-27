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
	
	public final static int THRESOLD = 10;
	
	private final static int ENTRY_WAIT_DAYS_AFTER_SPIKE = 3;
	
	public static int EXIT_MAX_WAIT_DAYS = 30;
	
	/**
	 * Analyzes the company and finds all the moments which satisfied the EVBBSystem's condition
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
	
	/**
	 * Evaluates the system's success rate.
	 * 
	 * @param company
	 * @return The ROIC(%) of the EVBB result, null when the result of the EVBB result is not clear.
	 */
	public Double evaluate(EVBBSystemResult evbbResult) {
		final int AFTER_DAYS = 30;
		//final int PROFIT_THRESHOLD = 10;
		
		List<StockQuoteModel> quoteList = evbbResult.getCompany().getQuoteList();
		if(quoteList.size() <= evbbResult.getDayIndex() + AFTER_DAYS) {
			return null;
		}
		double maxHigh = 0;
		for(int i = evbbResult.getDayIndex() + 1; i <= evbbResult.getDayIndex() + AFTER_DAYS; ++i) {
			if(quoteList.get(i).getHigh() > maxHigh) {
				maxHigh = quoteList.get(i).getHigh();
			}
		}
		double base = entryPoint(evbbResult);
		return (maxHigh - base) * 100 / base;
	}
	
	public Double evaluateByCCI(EVBBSystemResult evbbResult) {
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
		double entryP = entryPoint(evbbResult);
		return (quotes.get(i).getOpen() - entryP) / entryP;
	}
	
	public Double evaluateByTDD(EVBBSystemResult evbbResult) {
		Integer entryIndex = isEntry(evbbResult);
		if(null == entryIndex) {
			return null;
		}
		int exitIndex = getExitDateIndexByTDD(entryIndex, evbbResult);
		
		double entryP = entryPoint(evbbResult);
		double exitP = evbbResult.getCompany().getQuoteList().get(exitIndex).getClose();
		
		double roic = (exitP - entryP) / entryP;
		System.out.println(String.format("%s - %.2f - %d - %d", evbbResult.getCompany().getSymbol(), roic, exitIndex - entryIndex, evbbResult.getCompany().getQuoteList().get(entryIndex).getQuoteDate()));
		return roic;
	}
	/**
	 * Check if the evbbResult can entry.
	 * 
	 * @param evbbResult
	 * @return Index of entry date in quotes if entry success, null otherwise.  
	 */
	public Integer isEntry(EVBBSystemResult evbbResult) {
		
		StockQuoteModel quote = evbbResult.getCompany().getQuoteList().get(evbbResult.getDayIndex());
		double point = entryPoint(evbbResult);
		
		int leftDays = Math.min(ENTRY_WAIT_DAYS_AFTER_SPIKE, evbbResult.getCompany().getQuoteList().size() - evbbResult.getDayIndex() - 1);
		for(int i = 1; i <= leftDays; ++i) {
			quote = evbbResult.getCompany().getQuoteList().get(evbbResult.getDayIndex() + i);
			if(quote.getLow() <= point && point <= quote.getHigh()) {
				return evbbResult.getDayIndex() + i;
			}
		}
		return null;
	}
	
	/**
	 * Get the limit price of entry point.
	 * @param evbbResult
	 * @return
	 */
	public double entryPoint(EVBBSystemResult evbbResult) {
		StockQuoteModel quote = evbbResult.getCompany().getQuoteList().get(evbbResult.getDayIndex());
		return (quote.getClose() + quote.getOpen()) / 2;
	}
	
	/**
	 * Get index of exit date by Three Days Difference.
	 * @param evbbResult
	 * @return The index of exit date satisfied the TDD requirements or reach the max wait days.
	 */
	public int getExitDateIndexByTDD(int entryIndex, EVBBSystemResult evbbResult) {
		final double TDD_THRESHOLD = 0;
		// Leave when second TDD is negative.
		final int NEGATIVE_TIME_THRESHOLD = 2;
		List<StockQuoteModel> quotes = evbbResult.getCompany().getQuoteList();
		
		int negativeCount = 0;
		for(int i = entryIndex; i < quotes.size(); ++i) {
			if(TAUtil.ThreeDayDifference(quotes, i) < TDD_THRESHOLD) {
				++negativeCount;
				if(negativeCount >= NEGATIVE_TIME_THRESHOLD) {
					return i;
				}
			}
		}
		
		return Math.min(entryIndex + EXIT_MAX_WAIT_DAYS, quotes.size() - 1);
	}
	// ---------------------- Private Functions ----------------------------------
	
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
			
			if(isEntry(result) != null) {
				return result;
			}
		}
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