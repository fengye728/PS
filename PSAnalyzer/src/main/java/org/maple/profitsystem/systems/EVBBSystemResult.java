package org.maple.profitsystem.systems;

import org.maple.profitsystem.models.CompanyModel;

public class EVBBSystemResult {
	
	private CompanyModel company;
	
	private int dayIndex;
	// ---------- Fundamental favors --------------------
	private boolean insiderOwership;
	
	private boolean lowFloat;
	
	private boolean noNews;
	
	// -------------- Technical favors ------------------
	private boolean lowResistance;
	
	private boolean highBreakoutPrice;
	
	private boolean lowSMAVolume;
	
	private boolean intradayUp;
	
	private boolean beforeUp;
	
	private boolean aboveLowPriceLimit;
	
	private boolean belowHighPriceLimit;
	
	private boolean spikeVolume;
	
	private boolean emaSmaCorelative;
	
	public boolean isSatisfied() {
		return satisfiedCount() >= EVBBSystem.THRESOLD;
	}
	
	public int satisfiedCount() {
		int count = 0;
		if(insiderOwership) {
			++count;
		}
		if(lowFloat) {
			++count;
		}
		
		if(noNews) {
			++count;
		}
		
		if(lowResistance) {
			++count;
		}
		
		if(highBreakoutPrice) {
			++count;
		}
		
		if(lowSMAVolume) {
			++count;
		}
		
		if(intradayUp) {
			++count;
		}
		
		if(beforeUp) {
			++count;
		}
		
		if(aboveLowPriceLimit) {
			++count;
		}
		
		if(belowHighPriceLimit) {
			++count;
		}
		
		if(spikeVolume) {
			++count;
		}
		
		if(emaSmaCorelative) {
			++count;
		}
		return count;
	}

	public CompanyModel getCompany() {
		return company;
	}

	public void setCompany(CompanyModel company) {
		this.company = company;
	}

	public boolean isInsiderOwership() {
		return insiderOwership;
	}

	public void setInsiderOwership(boolean insiderOwership) {
		this.insiderOwership = insiderOwership;
	}

	public boolean isLowFloat() {
		return lowFloat;
	}

	public void setLowFloat(boolean lowFloat) {
		this.lowFloat = lowFloat;
	}

	public boolean isNoNews() {
		return noNews;
	}

	public void setNoNews(boolean noNews) {
		this.noNews = noNews;
	}

	public boolean isLowResistance() {
		return lowResistance;
	}

	public void setLowResistance(boolean lowResistance) {
		this.lowResistance = lowResistance;
	}

	public boolean isHighBreakoutPrice() {
		return highBreakoutPrice;
	}

	public void setHighBreakoutPrice(boolean highBreakoutPrice) {
		this.highBreakoutPrice = highBreakoutPrice;
	}

	public boolean isLowSMAVolume() {
		return lowSMAVolume;
	}

	public void setLowSMAVolume(boolean lowSMAVolume) {
		this.lowSMAVolume = lowSMAVolume;
	}

	public boolean isIntradayUp() {
		return intradayUp;
	}

	public void setIntradayUp(boolean intradayUp) {
		this.intradayUp = intradayUp;
	}

	public boolean isBeforeUp() {
		return beforeUp;
	}

	public void setBeforeUp(boolean beforeUp) {
		this.beforeUp = beforeUp;
	}

	public boolean isAboveLowPriceLimit() {
		return aboveLowPriceLimit;
	}

	public void setAboveLowPriceLimit(boolean aboveLowPriceLimit) {
		this.aboveLowPriceLimit = aboveLowPriceLimit;
	}

	public boolean isBelowHighPriceLimit() {
		return belowHighPriceLimit;
	}

	public void setBelowHighPriceLimit(boolean belowHighPriceLimit) {
		this.belowHighPriceLimit = belowHighPriceLimit;
	}

	public boolean isSpikeVolume() {
		return spikeVolume;
	}

	public void setSpikeVolume(boolean spikeVolume) {
		this.spikeVolume = spikeVolume;
	}

	public boolean isEmaSmaCorelative() {
		return emaSmaCorelative;
	}

	public void setEmaSmaCorelative(boolean emaSmaCorelative) {
		this.emaSmaCorelative = emaSmaCorelative;
	}

	public int getDayIndex() {
		return dayIndex;
	}

	public void setDayIndex(int dayIndex) {
		this.dayIndex = dayIndex;
	}
}
