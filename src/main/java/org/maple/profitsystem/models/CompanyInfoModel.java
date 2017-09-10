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
import java.util.List;

import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.utils.TransportUtil;

public class CompanyInfoModel {
	
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
	
	public void persistQuoteList(String outPath) {
		String filename = this.symbol;
		
		FileWriter fw = null;
		try {
			
			File file = new File(outPath + filename);
			if(!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			
			for(StockQuoteModel quote : quoteList) {
				fw.write(quote.toString() + "\n");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
	}
	
	public static CompanyInfoModel parseFromCSV(String csv) {
		String[] fields = csv.split(CommonConstants.NASDAQ_COMPANY_LIST_SEPRATOR_OF_FIELD);
		
		if(fields.length != CommonConstants.NASDAQ_COMPANY_LIST_RECORD_FIELDS_NUMBER) {
			return null;
		} else {
			CompanyInfoModel result = new CompanyInfoModel();
			
			result.symbol = TransportUtil.stripCSVField(fields[0]);
			result.name = TransportUtil.stripCSVField(fields[1]);
			result.amountShares = (int) (Double.valueOf(TransportUtil.stripCSVField(fields[3])) / Double.valueOf(TransportUtil.stripCSVField(fields[2])));
			try{
				result.ipoYear = Integer.valueOf(TransportUtil.stripCSVField(fields[5]));
			} catch(NumberFormatException e){
				result.ipoYear = 0;
			}
			
			result.sector = TransportUtil.stripCSVField(fields[6]);
			result.industry = TransportUtil.stripCSVField(fields[7]);
			
			return result;
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