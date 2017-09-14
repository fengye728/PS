package org.maple.profitsystem.utils;

import java.util.List;

import org.maple.profitsystem.models.CompanyInfoModel;
import org.maple.profitsystem.models.StockQuoteModel;

public class TAUtil {

	/**
	 * Get simple moving average of volume from targetDt-days(exclusive) to targetDt(inclusive).
	 * 
	 * @param company
	 * @param targetDt
	 * @param days
	 * @return SMA of volume if success, otherwise zero.
	 */
	public Integer SMAVolume(CompanyInfoModel company, int targetDt, int days) {
		int startIndex = getBeforeDaysStockQuoteIndex(company.getQuoteList(), targetDt, days);
		if(startIndex == -1) {
			return 0;
		}
		long amountVolume = 0;
		for(int i = startIndex; i < startIndex + days; ++i) {
			amountVolume += company.getQuoteList().get(i).getVolume();
		}
		return (int)(amountVolume / days);
	}
	
	public Integer EMAVolume(CompanyInfoModel company, int targetDt, int days) {
		int startIndex = getBeforeDaysStockQuoteIndex(company.getQuoteList(), targetDt, days);
		if(startIndex == -1) {
			return 0;
		}
		
		int emaLast = company.getQuoteList().get(startIndex).getVolume();
		
		for(int i = startIndex + 1; i < startIndex + days; ++i) {
			int n = i - startIndex + 1;
			emaLast = (2 * company.getQuoteList().get(i).getVolume() + (n - 1) * emaLast) / (n + 1);
		}
		return emaLast;
	}
	
	/**
	 * See getBeforeDaysStockQuotes.
	 * 
	 * 		Remark: This is just for performance.
	 * 
	 * @param quotes
	 * @param endDt
	 * @param days days More then zero.
	 * @return The index of endDt-days+1 quote if existed, otherwise -1.
	 */
	public int getBeforeDaysStockQuoteIndex(List<StockQuoteModel> quotes ,int endDt, int days) {
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
	public List<StockQuoteModel> getBeforeDaysStockQuotes(List<StockQuoteModel> quotes ,int endDt, int days) {
		if(quotes.isEmpty()) {
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
