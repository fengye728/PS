package org.maple.profitsystem.models;

import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.utils.CSVUtil;

public class CompanyStatisticsModel {

	private Double insiderOwnPerc;
	
	private Double instOwnPerc;
	
	private Integer shsOutstand;
	
	private Integer shsFloat;
	
	@Override
	public String toString() {
		return CommonConstants.CSV_SURROUNDER_OF_FIELD + insiderOwnPerc + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ instOwnPerc + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ shsOutstand + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD
				+ shsFloat + CommonConstants.CSV_SURROUNDER_OF_FIELD;
	}
	
	public static CompanyStatisticsModel parseFromFileCSV(String csvRecord) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csvRecord);
		try{
			CompanyStatisticsModel result = new CompanyStatisticsModel();
			
			result.insiderOwnPerc = Double.valueOf(fields[0]);
			result.instOwnPerc = Double.valueOf(fields[1]);
			result.shsOutstand = Integer.valueOf(fields[2]);
			result.shsFloat = Integer.valueOf(fields[3]);
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
	}
	
	public Double getInsiderOwnPerc() {
		return insiderOwnPerc;
	}

	public void setInsiderOwnPerc(Double insiderOwnPerc) {
		this.insiderOwnPerc = insiderOwnPerc;
	}

	public Double getInstOwnPerc() {
		return instOwnPerc;
	}

	public void setInstOwnPerc(Double instOwnPerc) {
		this.instOwnPerc = instOwnPerc;
	}

	public Integer getShsOutstand() {
		return shsOutstand;
	}

	public void setShsOutstand(Integer shsOutstand) {
		this.shsOutstand = shsOutstand;
	}

	public Integer getShsFloat() {
		return shsFloat;
	}

	public void setShsFloat(Integer shsFloat) {
		this.shsFloat = shsFloat;
	}

}
