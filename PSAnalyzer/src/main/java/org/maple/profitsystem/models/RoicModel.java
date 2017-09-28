package org.maple.profitsystem.models;

public class RoicModel {

	private String symbol;
	
	private String sector;
	
	private double roic;
	
	private int days;
	
	private int entryDate;
	
	public String toString() {
		return symbol + "," + 
				sector + "," + 
				roic + "," + 
				days + "," + 
				entryDate;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public double getRoic() {
		return roic;
	}

	public void setRoic(double roic) {
		this.roic = roic;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(int entryDate) {
		this.entryDate = entryDate;
	}
	
}
