/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.systems;

import java.util.List;

import org.maple.profitsystem.models.CompanyInfoModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.utils.TAUtil;

/**
 * Explosive volume based breakouts system.
 * 
 * @author Maple
 *
 */
public class EVBBSystem {
	
	public final static int MIN_PERIOD_DAYS = 261; 
	/**
	 * Analyzes the company and finds all the moments which satisfied the EVBBSystem's condition
	 * 
	 * @param company
	 * @return
	 */
	public static EVBBSystemResult analyze(CompanyInfoModel company) {
		// TODO
		return null;
	}
	
	/**
	 * Evaluates the system's success rate.
	 * 
	 * @param company
	 * @return
	 */
	public static EVBBSystemResult evaluate(CompanyInfoModel company) {
		return null;
	}
	
	
	private boolean testInsiderOwnership(CompanyInfoModel company) {
		final int THRESHOLD = 10;
		if(company.getStatistics().getInsiderOwnPerc() != null && company.getStatistics().getInsiderOwnPerc() >= THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testSharesFloat(CompanyInfoModel company) {
		final int THRESHOLD = 35000000;
		if(company.getStatistics().getInsiderOwnPerc() != null && company.getStatistics().getShsFloat() <= THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testNews(CompanyInfoModel company, int targetDate) {
		// TODO 
		return false;
	}
	
	private boolean testResistanceVolume(CompanyInfoModel company, int targetIndex) {
		final int PAST_YEAR_DAYS = 260;
		final int MULTIPLE_THRESHOLD = 2;
		final int SMA_DAYS = 50;
		List<StockQuoteModel> quotes = company.getQuoteList();
			
		// get volume of max volume resistance
		int maxVolumeOfResistance = TAUtil.MaxResistanceVolumeByIndex(quotes, targetIndex, PAST_YEAR_DAYS);
		
		if(maxVolumeOfResistance > 0 && maxVolumeOfResistance * MULTIPLE_THRESHOLD <= TAUtil.SMAVolumeByIndex(quotes, targetIndex, SMA_DAYS)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testSpikePriceHigh(CompanyInfoModel company, int targetIndex) {
		final int DAYS_OF_MAX_HIGH_PRICE = 60;
		List<StockQuoteModel> quotes = company.getQuoteList();
		if(quotes.get(targetIndex).getClose() > TAUtil.MaxHighPriceByIndex(quotes, targetIndex - 1, DAYS_OF_MAX_HIGH_PRICE)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testSMAVolume(CompanyInfoModel company, int targetIndex) {
		final int VOLUME_THRESHOLD = 300000;
		final int SMA_DAYS = 50;
		
		if(TAUtil.SMAVolumeByIndex(company.getQuoteList(), targetIndex - 1, SMA_DAYS) <= VOLUME_THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testSameDayUp(CompanyInfoModel company, int targetIndex) {
		StockQuoteModel quote = company.getQuoteList().get(targetIndex);
		if(quote.getOpen() < quote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testBeforeDayUp(CompanyInfoModel company, int targetIndex) {
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		StockQuoteModel beforeQuote = company.getQuoteList().get(targetIndex - 1);
		if(beforeQuote.getClose() < curQuote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testPriceLowLimit(CompanyInfoModel company, int targetIndex) {
		final double LOW_PRICE = 2.0;
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		if(LOW_PRICE < curQuote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testPriceHighLimit(CompanyInfoModel company, int targetIndex) {
		final double HIGH_PRICE = 25.0;
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		if(HIGH_PRICE > curQuote.getClose()) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testVolume(CompanyInfoModel company, int targetIndex) {
		final int VOLUME_TIMES = 3;
		StockQuoteModel curQuote = company.getQuoteList().get(targetIndex);
		StockQuoteModel beforeQuote = company.getQuoteList().get(targetIndex - 1);
		if(curQuote.getVolume() >= beforeQuote.getVolume() * VOLUME_TIMES) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testEMAAndSMA(CompanyInfoModel company, int targetIndex) {
		final double SMA_TIMES = 1.5;
		final int EMA_SMA_DAYS = 50;
		List<StockQuoteModel> quotes = company.getQuoteList();
		if(TAUtil.EMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS) <= TAUtil.SMAVolumeByIndex(quotes, targetIndex - 1, EMA_SMA_DAYS) * SMA_TIMES) {
			return true;
		} else {
			return false;
		}
	}
	
}