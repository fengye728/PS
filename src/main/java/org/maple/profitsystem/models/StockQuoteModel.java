package org.maple.profitsystem.models;

import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.utils.CSVUtil;
import org.maple.profitsystem.utils.TradingDateUtil;

public class StockQuoteModel implements Comparable<StockQuoteModel>{

	//private static Logger logger = Logger.getLogger(StockQuoteModel.class);
	
	private String symbol;
	
	private Integer quoteDate;	// yyyyMMDD
	
	private Double open;
	
	private Double close;
	
	private Double high;
	
	private Double low;
	
	private Integer volume;
	
	/**
	 * Sort by date ascending order
	 */
	@Override
	public int compareTo(StockQuoteModel o) {
		if(symbol.equals(o.getSymbol())) {
			return -TradingDateUtil.betweenTradingDays(TradingDateUtil.convertNumDate2Date(this.getQuoteDate()), 
					TradingDateUtil.convertNumDate2Date(o.getQuoteDate()));
		} else {
			return -1;
		}
	}
	
	/**
	 * Parse csv format record stored in transporting to StockQuoteModel.
	 * 
	 * @param symbol
	 * @param csvRecord
	 * @return
	 * @throws PSException
	 */
	public static StockQuoteModel parseFromTransportCSV(String symbol, String csvRecord) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csvRecord);
		try{
			StockQuoteModel result = new StockQuoteModel();
			result.symbol = symbol;
			result.quoteDate = Integer.valueOf(fields[0].replaceAll("/", ""));
			result.close = Double.valueOf(fields[1]);
			result.volume = Double.valueOf(fields[2]).intValue();
			result.open = Double.valueOf(fields[3]);
			result.high = Double.valueOf(fields[4]);
			result.low = Double.valueOf(fields[5]);
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
		
	}
	
	/**
	 * Parse csv format record stored in file to StockQuoteModel.
	 * 
	 * @param csvRecord
	 * @return
	 * @throws PSException
	 */
	public static StockQuoteModel parseFromFileCSV(String csvRecord) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csvRecord);
		try{
			StockQuoteModel result = new StockQuoteModel();
			result.symbol = fields[0];
			result.quoteDate = Integer.valueOf(fields[1]);
			result.open = Double.valueOf(fields[2]);
			result.close = Double.valueOf(fields[3]);
			result.high = Double.valueOf(fields[4]);
			result.low = Double.valueOf(fields[5]);
			result.volume = Integer.valueOf(fields[6]);
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return CommonConstants.CSV_SURROUNDER_OF_FIELD + this.symbol + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ this.quoteDate + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ this.open + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ this.close + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ this.high + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD  
				+ this.low + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ this.volume + CommonConstants.CSV_SURROUNDER_OF_FIELD;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Integer getQuoteDate() {
		return quoteDate;
	}

	public void setQuoteDate(Integer quoteDate) {
		this.quoteDate = quoteDate;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}
}
