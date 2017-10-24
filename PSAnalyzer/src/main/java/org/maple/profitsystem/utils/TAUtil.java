package org.maple.profitsystem.utils;

import java.util.List;

import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.StockQuoteModel;

public class TAUtil {
	
	public static int MaxSellVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int period) {
		int startIndex = getBeforePeriodIndex(targetIndex, period);
		int maxVolume = 0;
		for(int i = startIndex; i <= targetIndex; ++i) {
			StockQuoteModel quote = quotes.get(i);
			if(quote.getClose() > quote.getOpen()) {
				continue;
			}
			if(quote.getVolume() > maxVolume) {
				maxVolume = quote.getVolume();
			}
		}
		return maxVolume;
	}
	
	public static int MaxVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int period) {
		int startIndex = getBeforePeriodIndex(targetIndex, period);
		int maxVolume = 0;
		for(int i = startIndex; i <= targetIndex; ++i) {
			if(quotes.get(i).getVolume() > maxVolume) {
				maxVolume = quotes.get(i).getVolume();
			}
		}
		return maxVolume;
	}
	
	/**
	 * Get the index before targetIndex [period] days.(Include targetIndex)
	 * @param targetIndex
	 * @param period
	 * @return
	 */
	public static int getBeforePeriodIndex(int targetIndex, int period) {
		int startIndex = targetIndex - period + 1;
		if(startIndex < 0) {
			startIndex = 0;
		}
		return startIndex;
	}

	/**
	 * SMA of Volume.(Include targetIndex)
	 * @param quotes
	 * @param targetIndex
	 * @param days
	 * @return
	 * @throws PSException 
	 */
	public static int SMAVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) throws PSException {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			throw new PSException("No enough records for SMAVolumeByIndex");
		}
		long amountVolume = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			amountVolume += quotes.get(i).getVolume();
		}
		return (int)(amountVolume / days);
	}
	
	public static int SMAPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) throws PSException {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			throw new PSException("No enough records for SMAPriceByIndex");
		}
		long amountVolume = 0;
		for(int i = startIndex; i <= targetIndex; ++i) {
			amountVolume += quotes.get(i).getClose();
		}
		return (int)(amountVolume / days);
	}
	/**
	 * EMA of Volume.(Include targetIndex)
	 * @param quotes
	 * @param targetIndex
	 * @param days
	 * @return
	 * @throws PSException 
	 */
	public static int EMAVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) throws PSException {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			throw new PSException("No enough records for EMAVolumeByIndex");
		}
		
		int emaLast = TAUtil.SMAVolumeByIndex(quotes, startIndex, days);
		
		for(int i = startIndex + 1; i <= targetIndex; ++i) {
			emaLast = (2 * quotes.get(i).getVolume() + (days - 1) * emaLast) / (days + 1);
		}
		return emaLast;
	}
	
	public static double EMAPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) throws PSException {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			throw new PSException("No enough records for EMAPriceByIndex");
		}
		
		double emaLast = TAUtil.SMAPriceByIndex(quotes, startIndex, days);
		
		for(int i = startIndex + 1; i <= targetIndex; ++i) {
			emaLast = (2 * quotes.get(i).getClose() + (days - 1) * emaLast) / (days + 1);
		}
		return emaLast;
	}
	
	/**
	 * The max volume of overhead resistance for targetIndex in the past days(Exclude targetIndex).
	 * 
	 * @param quotes
	 * @param targetIndex
	 * @param days
	 * @return The max volume
	 * @throws PSException 
	 */
	public static int MaxResistanceVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) throws PSException {
		// End to day before targetDt
		int startIndex = targetIndex - days;
		if(startIndex < 0) {
			throw new PSException("No enough records for MaxResistanceVolumeByIndex");
		}
		
		// get volume of max volume resistance
		int maxVolumeOfResistance = 0;
		double targetMidPrice = (quotes.get(targetIndex).getLow() + quotes.get(targetIndex).getHigh()) / 2;
		for(int i = startIndex; i < targetIndex; ++i) {
			if(quotes.get(i).getHigh() >= targetMidPrice && quotes.get(i).getVolume() > maxVolumeOfResistance) {
				// resistance line of max volume now
				maxVolumeOfResistance = quotes.get(i).getVolume();
			}
		}
		return maxVolumeOfResistance;
	}
	
	/**
	 * The highest price in [days] period from targetIndex - days + 1(Include) to targetIndex(Include).
	 * 
	 * @param quotes
	 * @param targetIndex
	 * @param days
	 * @return
	 * @throws PSException 
	 */
	public static double MaxHighPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) throws PSException {
		if(days <= 0) {
			throw new PSException("Period is zero or negative!");
		}
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			startIndex = 0;
			days = targetIndex + 1;
		}
		
		double maxHighPrice = 0;
		for(int i = startIndex; i <= targetIndex; ++i) {
			if(quotes.get(i).getHigh() > maxHighPrice) {
				maxHighPrice = quotes.get(i).getHigh();
			}
		}
		return maxHighPrice;	
	}
	
	/**
	 * The lowest price in [days] period from targetIndex - days + 1(Include) to targetIndex(Include).
	 * 
	 * @param quotes
	 * @param targetIndex
	 * @param days
	 * @return
	 * @throws PSException 
	 */
	public static double LowestPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int period) throws PSException {
		if(period <= 0) {
			throw new PSException("Period is zero or egative!");
		}
		int startIndex = getBeforePeriodIndex(targetIndex, period);
		
		double lowestPrice = Double.MAX_VALUE;
		for(int i = startIndex; i <= targetIndex; ++i) {
			if(quotes.get(i).getLow() < lowestPrice) {
				lowestPrice = quotes.get(i).getLow();
			}
		}
		return lowestPrice;
	}
	
	/**
	 * Get the quotes whose date between endDt-days(exclusive) and endDt(Inclusive).
	 * 
	 * 		The list from value of return(Inclusive) to startIndex + days(Exclusive).
	 * 
	 * 		Remark: This is just for performance.
	 * 
	 * @param quotes
	 * @param endDt
	 * @param days days More then zero.
	 * @return The index of endDt-days+1 quote if existed, otherwise -1.
	 */
	public static int getBeforeDaysStockQuoteIndex(List<StockQuoteModel> quotes ,int endDt, int days) {
		int result = -1;
		if(!quotes.isEmpty()) {
			int endIndex = quotes.size();	// exclusive
			while(endIndex - days >= 0 && endIndex > 0) {
				if(quotes.get(endIndex - 1).getQuoteDate() <= endDt) {
					break;
				}
				--endIndex;
			}
			int startIndex = endIndex - days; // inclusive
			if(startIndex >= 0) {
				result = startIndex;
			}
		}
		return result;
	}
	
	/**
	 * Get the normalized price of the quote.
	 * 
	 * @param quote
	 * @return
	 * @throws PSException 
	 */
	public static double NP(StockQuoteModel quote) throws PSException {
		if(quote == null) {
			throw new PSException(CommonConstants.ERROR_MSG_QUOTE_NULL);
		}
		return (quote.getHigh() + quote.getLow() + quote.getClose()) / 3;
	}
	
	/**
	 * Get the [period] period SMAP of NP.(Include targetIndex)
	 * 
	 * @param quotes
	 * @param targetIndex
	 * @param period
	 * @return
	 * @throws PSException 
	 */
	public static double SMANP(List<StockQuoteModel> quotes, int targetIndex, int period) throws PSException {
		int startIndex = targetIndex - period + 1;
		if(quotes == null || startIndex < 0) {
			return 0;
		}
		double amountNP = 0;
		for(int i = startIndex; i < startIndex + period; ++i) {
			amountNP += NP(quotes.get(i));
		}
		return (amountNP / period);
	}
	
	/**
	 * Get the Mean Deviation in period.(Include targetIndex)
	 * @param quotes
	 * @param targetIndex
	 * @param period
	 * @return
	 * @throws PSException 
	 */
	public static double MD(List<StockQuoteModel> quotes, int targetIndex, int period) throws PSException {
		int startIndex = targetIndex - period + 1;
		if(quotes == null || startIndex < 0) {
			throw new PSException("quotes is null or quotes have not enough records!");
		}
		double amountMD = 0;
		for(int i = startIndex; i < startIndex + period; ++i) {
			amountMD += Math.abs(SMANP(quotes, i, i - startIndex + 1) - NP(quotes.get(i)));
		}
		return (amountMD / period);		
	}
	
	/**
	 * Commodity Channel Index (CCI): A momentum indicator.(For EVBBSystem)
	 * 
	 * @param quotes
	 * @param targetIndex
	 * @return
	 * @throws PSException 
	 */
	public static double CCI(List<StockQuoteModel> quotes, int targetIndex) throws PSException {
		final int CCI_PERIOD = 20;
		if(targetIndex - CCI_PERIOD < 0) {
			throw new PSException("No enough records in quotes for CCI");
		} 
		return (NP(quotes.get(targetIndex)) - SMANP(quotes, targetIndex, CCI_PERIOD)) / (0.15 * MD(quotes, targetIndex, CCI_PERIOD));
	}
	
	/**
	 * Five Day Oscillator.(For EVBBSystem)
	 * 
	 * 	Remark:
	 * 		Bearish: < 30
	 * 		Neutral: 30 ~ 70
	 * 		Bullish: > 70
	 * @param quotes
	 * @param targetIndex
	 * @return
	 * @throws PSException 
	 */
	public static double FiveDayOscillator(List<StockQuoteModel> quotes, int targetIndex) throws PSException {
		final int OSCILLATOR_PERIOD = 5;
		if(targetIndex - OSCILLATOR_PERIOD < 0) {
			throw new PSException("No enough records for FDO(Five Day Oscillator)");
		}
		double highestPrice = MaxHighPriceByIndex(quotes, targetIndex, OSCILLATOR_PERIOD);
		double lowestPrice = LowestPriceByIndex(quotes, targetIndex, OSCILLATOR_PERIOD);
		double A = highestPrice - quotes.get(targetIndex - OSCILLATOR_PERIOD + 1).getOpen();
		double B = quotes.get(targetIndex).getClose() - lowestPrice;
		
		double oscillator = ((A + B) * 100) / ((highestPrice - lowestPrice) * 2);
		
		return oscillator;
	}
	
	/**
	 * Three Days Difference: calculated by FiveDayOscillator(today) - FiveDayOscillator(today - 2).
	 * 	
	 * @param quotes
	 * @param targetIndex
	 * @return
	 * @throws PSException 
	 */
	public static Double ThreeDayDifference(List<StockQuoteModel> quotes, int targetIndex) throws PSException {
		final int DIFFERENCE_PERIOD = 2;
		if(targetIndex - DIFFERENCE_PERIOD < 0) {
			return null;
		}
		return FiveDayOscillator(quotes, targetIndex) - FiveDayOscillator(quotes, targetIndex - DIFFERENCE_PERIOD);
	}
}
