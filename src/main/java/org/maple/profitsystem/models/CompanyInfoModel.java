/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.utils.CSVUtil;

public class CompanyInfoModel{
	
	private static Logger logger = Logger.getLogger(CompanyInfoModel.class);
	
	private String symbol;
	private String name;
	private Integer amountShares;

	private Integer ipoYear;	// yyyyMMdd

	private String sector;
	private String industry;
	
	private Integer lastQuoteDt = 0;	// yyyyMMdd
	
	private List<StockQuoteModel> quoteList = new ArrayList<>();
	
	/*
	private static double parseMarketCapFieldFromString(String strMarCap) {
		double number = Double.valueOf(strMarCap.substring(1, strMarCap.length() - 1));
		int multiplier = 0;
		switch(Character.toUpperCase(strMarCap.charAt(strMarCap.length() - 1))) {
		case 'K':
			multiplier = 1000;
			break;
		case 'M':
			multiplier = 1000000;
			break;
		case 'B':
			multiplier = 1000000000;
			break;
		}
		return number * multiplier;
	}
	*/
	
	/**
	 * Add newest quote list into quoteList and update lastQuoteDt.
	 * 
	 * @param newestList
	 */
	public void addNewestQuoteList(List<StockQuoteModel> newestList) {
		if(null == newestList || newestList.isEmpty()) {
			return ;
		}
		quoteList.addAll(newestList);
		Collections.sort(quoteList);
		// update lastQuoteDt
		if(quoteList.size() > 0) {
			this.lastQuoteDt = quoteList.get(quoteList.size() - 1).getQuoteDate();
		}
	}
	
	/**
	 * 
	 * @param outPath
	 */
	public void persist2Disk(String outPath) {
		String filename = this.symbol;
		
		FileWriter fw = null;
		try {
			
			File file = new File(outPath + filename);
			if(!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			
			// write company info
			fw.write(this.formatFullCSV());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Format full CompanyInfoModel(with quote list) to a csv string.
	 * 
	 * @return A csv string.
	 */
	public String formatFullCSV() {
		
		String companySummary = this.toString() + CommonConstants.CSV_NEWLINE + CommonConstants.CSV_NEWLINE;
		
		StringBuilder quotes = new StringBuilder();
		// write quotes
		for(StockQuoteModel quote : this.quoteList) {
			quotes.append(quote.toString() + CommonConstants.CSV_NEWLINE);
		}
		
		return companySummary + quotes.toString();
	}
	
	/**
	 * Parse a full file csv to full CompanyInfoModel(with quote list).
	 * 
	 * @param csv
	 * @return
	 * @throws PSException 
	 */
	public static CompanyInfoModel parseFullFromFileCSV(String csv) throws PSException {
		String[] lines = csv.split(CommonConstants.CSV_NEWLINE_REG);
		CompanyInfoModel result = CompanyInfoModel.parseBaseFromFileCSV(lines[0]);
		
		List<StockQuoteModel> quotes = new ArrayList<>();
		for(int i = 2; i < lines.length; ++i) {
			try {
				quotes.add(StockQuoteModel.parseFromFileCSV(lines[i]));
			} catch(PSException e) {
				logger.error("Parse quote failed - " + result.getSymbol() + ": " + e.getMessage());
			}
		}
		result.addNewestQuoteList(quotes);
		
		return result;
	}
	
	public static CompanyInfoModel parseFromTransportCSV(String csv) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csv);

		try {
			CompanyInfoModel result = new CompanyInfoModel();
			
			result.symbol = fields[0];
			result.name = fields[1];
			result.amountShares = (int) (Double.valueOf(fields[3]) / Double.valueOf(fields[2]));
			try{
				result.ipoYear = Integer.valueOf(fields[5]);
			} catch(NumberFormatException e){
				result.ipoYear = 0;
			}
			
			result.sector = fields[6];
			result.industry = fields[7];
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
	}
	
	public static CompanyInfoModel parseBaseFromFileCSV(String csv) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csv);
		CompanyInfoModel result = new CompanyInfoModel();
		
		try {
			result.symbol = fields[0];
			result.name = fields[1];
			result.amountShares = Integer.valueOf(fields[2]);
			result.ipoYear = Integer.valueOf(fields[3]);
			result.sector = fields[4];
			result.industry = fields[5];
			result.lastQuoteDt = Integer.valueOf(fields[6]);
			return result;
		} catch (Exception e) {
			throw new PSException(e.getMessage());
		}
	}

	@Override
	public String toString() {	
		return CommonConstants.CSV_SURROUNDER_OF_FIELD + this.symbol + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.name + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.amountShares + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.ipoYear + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.sector + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.industry + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.lastQuoteDt + CommonConstants.CSV_SURROUNDER_OF_FIELD;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(o instanceof CompanyInfoModel) {
			return this.symbol.compareTo(((CompanyInfoModel)o).getSymbol()) == 0 ? true : false;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		if(this.symbol == null) {
			return "".hashCode();
		} else {
			return this.symbol.hashCode();
		}
	}
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAmountShares() {
		return amountShares;
	}

	public void setAmountShares(Integer amountShares) {
		this.amountShares = amountShares;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public Integer getIpoYear() {
		return ipoYear;
	}

	public void setIpoYear(Integer ipoYear) {
		this.ipoYear = ipoYear;
	}

	public Integer getLastQuoteDt() {
		return lastQuoteDt;
	}

	public void setLastQuoteDt(Integer lastQuoteDt) {
		this.lastQuoteDt = lastQuoteDt;
	}

	public List<StockQuoteModel> getQuoteList() {
		return quoteList;
	}

	public void setQuoteList(List<StockQuoteModel> quoteList) {
		this.quoteList = quoteList;
	}
}