package org.maple.profitsystem.utils;

import java.util.List;

import org.maple.profitsystem.models.StockQuoteModel;

public class TAUtil {

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
	
	public static Double MaxHighPriceByIndex(List<StockQuoteModel> quotes, int targetIndex, int days) {
		int startIndex = targetIndex - days + 1;
		if(startIndex < 0) {
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
}
