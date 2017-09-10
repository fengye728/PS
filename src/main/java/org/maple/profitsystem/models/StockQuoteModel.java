package org.maple.profitsystem.models;

import org.apache.log4j.Logger;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.utils.TransportUtil;

public class StockQuoteModel {

	private static Logger logger = Logger.getLogger(StockQuoteModel.class);
	
	private String symbol;
	
	private Integer quoteDate;	// yyyyMMDD
	
	private Double open;
	
	private Double close;
	
	private Double high;
	
	private Double low;
	
	private Integer volume;
	
	public static StockQuoteModel parseFromTransportCSV(String symbol, String csvRecord) throws PSException {
		String[] fields = csvRecord.split(",");
		try{
			StockQuoteModel result = new StockQuoteModel();
			result.symbol = symbol;
			result.quoteDate = Integer.valueOf(TransportUtil.stripCSVField(fields[0]).replaceAll("/", ""));
			result.close = Double.valueOf(TransportUtil.stripCSVField(fields[1]));
			result.volume = Double.valueOf(TransportUtil.stripCSVField(fields[2])).intValue();
			result.open = Double.valueOf(TransportUtil.stripCSVField(fields[3]));
			result.high = Double.valueOf(TransportUtil.stripCSVField(fields[4]));
			result.low = Double.valueOf(TransportUtil.stripCSVField(fields[5]));
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
		
	}
	
	public static StockQuoteModel parseFromFileCSV(String csvRecord) throws PSException {
		String[] fields = csvRecord.split(",");
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
	
	public String toString() {
		return this.symbol + "," + this.quoteDate + "," + this.open + ","  + this.close + ","  + this.high + ","  + this.low + "," + this.volume;
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
