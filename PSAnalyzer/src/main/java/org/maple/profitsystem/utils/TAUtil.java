package org.maple.profitsystem.utils;

import java.util.List;

import org.maple.profitsystem.models.StockQuoteModel;

public class TAUtil {
	
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
	 */
	public static Integer SMAVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			return 0;
		}
		long amountVolume = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			amountVolume += quotes.get(i).getVolume();
		}
		return (int)(amountVolume / days);
	}
	
	/**
	 * EMA of Volume.(Include targetIndex)
	 * @param quotes
	 * @param targetIndex
	 * @param days
	 * @return
	 */
	public static Integer EMAVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			return 0;
		}
		
		int emaLast = quotes.get(startIndex).getVolume();
		
		for(int i = startIndex + 1; i < startIndex + days; ++i) {
			int n = i - startIndex + 1;
			emaLast = (2 * quotes.get(i).getVolume() + (n - 1) * emaLast) / (n + 1);
		}
		return emaLast;
	}
	
	public static Integer MaxResistanceVolumeByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) {
		// End to day before targetDt
		int startIndex = targetIndex - days;
		if(startIndex < 0) {
			return 0;
		}
		
		// get volume of max volume resistance
		int maxVolumeOfResistance = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			if(quotes.get(i).getHigh() >= quotes.get(targetIndex).getClose() && quotes.get(i).getVolume() > maxVolumeOfResistance) {
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
	 */
	public static double MaxHighPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
			startIndex = 0;
			days = targetIndex + 1;
		}
		
		double maxHighPrice = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			if(quotes.get(i).getHigh() > maxHighPrice) {
				maxHighPrice = quotes.get(i).getHigh();
			}
		}
		return maxHighPrice;	
	}
	
	public static double LowestPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int period) {
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
	 * Get simple moving average of volume from targetDt-days(exclusive) to targetDt(inclusive).
	 * 
	 * @param company
	 * @param targetDt
	 * @param days
	 * @return SMA of volume if success, otherwise zero.
	 */
	public static Integer SMAVolume(List<StockQuoteModel> quotes, int targetDt, int days) {
		int startIndex = getBeforeDaysStockQuoteIndex(quotes, targetDt, days);
		if(startIndex == -1) {
			return 0;
		}
		long amountVolume = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			amountVolume += quotes.get(i).getVolume();
		}
		return (int)(amountVolume / days);
	}
	
	public static Integer EMAVolume(List<StockQuoteModel> quotes, int targetDt, int days) {
		int startIndex = getBeforeDaysStockQuoteIndex(quotes, targetDt, days);
		if(startIndex == -1) {
			return 0;
		}
		
		int emaLast = quotes.get(startIndex).getVolume();
		
		for(int i = startIndex + 1; i < startIndex + days; ++i) {
			int n = i - startIndex + 1;
			emaLast = (2 * quotes.get(i).getVolume() + (n - 1) * emaLast) / (n + 1);
		}
		return emaLast;
	}
	
	/**
	 * The max volume of overhead resistance for targetDt in the past days(Exclude targetDt).
	 * 
	 * @param quotes
	 * @param targetDt
	 * @param days
	 * @return Otherwise zero.
	 */
	public static Integer MaxResistanceVolume(List<StockQuoteModel> quotes, int targetDt, int days) {
		// End to day before targetDt
		int startIndex = getBeforeDaysStockQuoteIndex(quotes, targetDt - 1, days);
		if(startIndex == -1) {
			return 0;
		}
		
		int targetDayIndex = startIndex + targetDt - 1;
		
		// get volume of max volume resistance
		int maxVolumeOfResistance = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			if(quotes.get(i).getHigh() >= quotes.get(targetDayIndex).getClose() && quotes.get(i).getVolume() > maxVolumeOfResistance) {
				// resistance line of max volume now
				maxVolumeOfResistance = quotes.get(i).getVolume();
			}
		}
		return maxVolumeOfResistance;
	}
	
	/**
	 * The max high price of [days] days before [targetDt].
	 * 
	 * @param quotes
	 * @param targetDt
	 * @param days
	 * @return
	 */
	public static Double MaxHighPrice(List<StockQuoteModel> quotes, int targetDt, int days) {
		int startIndex = getBeforeDaysStockQuoteIndex(quotes, targetDt, days);
		if(startIndex == -1) {
			return 0.0;
		}
		
		// get volume of max volume resistance
		double maxHighPrice = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			if(quotes.get(i).getHigh() > maxHighPrice) {
				maxHighPrice = quotes.get(i).getHigh();
			}
		}
		return maxHighPrice;	
	}
	/**
	 * See getBeforeDaysStockQuotes.
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
	 * Get the quotes whose date between endDt-days(exclusive) and endDt(Inclusive).
	 * 
	 * @param quotes
	 * @param endDt
	 * @param days More then zero.
	 * @return List of StockQuoteModel if these satisfied condition existed, otherwise null.
	 */
	public static List<StockQuoteModel> getBeforeDaysStockQuotes(List<StockQuoteModel> quotes ,int endDt, int days) {
		if(quotes.isEmpty() || quotes.size() < days) {
			return null;
		} else {
			int endIndex = quotes.size();	// exclusive
			
			while(endIndex - days >= 0) {
				if(quotes.get(endIndex - 1).getQuoteDate() <= endDt) {
					break;
				}
			}
			int startIndex = endIndex - days; // inclusive
			if(startIndex < 0) {
				return null;
			} else {
				return quotes.subList(startIndex, endIndex);
			}
		}
	}
	
	/**
	 * Get the normalized price of the quote.
	 * 
	 * @param quote
	 * @return
	 */
	public static double NP(StockQuoteModel quote) {
		if(quote == null) {
			return 0.0;
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
	 */
	public static double SMANP(List<StockQuoteModel> quotes, int targetIndex, int period) {
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
	 */
	public static double MD(List<StockQuoteModel> quotes, int targetIndex, int period) {
		int startIndex = targetIndex - period + 1;
		if(quotes == null || startIndex < 0) {
			return 0;
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
	 */
	public static Double CCI(List<StockQuoteModel> quotes, int targetIndex) {
		final int CCI_PERIOD = 20;
		if(targetIndex - CCI_PERIOD < 0) {
			return null;
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
	 */
	public static Double FiveDayOscillator(List<StockQuoteModel> quotes, int targetIndex) {
		final int OSCILLATOR_PERIOD = 5;
		if(targetIndex - OSCILLATOR_PERIOD < 0) {
			return null;
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
	 */
	public static Double ThreeDayDifference(List<StockQuoteModel> quotes, int targetIndex) {
		final int DIFFERENCE_PERIOD = 2;
		if(targetIndex - DIFFERENCE_PERIOD < 0) {
			return null;
		}
		return FiveDayOscillator(quotes, targetIndex) - FiveDayOscillator(quotes, targetIndex - DIFFERENCE_PERIOD);
	}
}
