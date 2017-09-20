package org.maple.profitsystem.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.maple.profitsystem.constants.CommonConstants;

public class CompanyModel {
	
	private CompanyStatisticsModel statistics = new CompanyStatisticsModel();
	
	private List<StockQuoteModel> quoteList = new ArrayList<>();
	
	public int getQuoteIndex(int targetDt) {
		int result = -1;
		for(int i = quoteList.size() - 1; i >= 0; --i) {
			if(targetDt > quoteList.get(i).getQuoteDate()) {
				break;
			} else if(targetDt == quoteList.get(i).getQuoteDate()) {
				result = i;
				break;
			}
		}
		return result;
	}


	@Override
	public String toString() {	
		return CommonConstants.CSV_SURROUNDER_OF_FIELD + this.symbol + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.name + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			//this.amountShares + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.ipoYear + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.sector + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.industry + CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD + 
			this.lastQuoteDt + CommonConstants.CSV_SURROUNDER_OF_FIELD;
	}
	
	@Override
	public boolean equals(Object o) {
		if(null == o || !(o instanceof CompanyModel)) {
			return false;
		} else {
			return this.symbol.equals(((CompanyModel)o).symbol);
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
	
    private Long id;

    private String symbol;

    private String name;

    private Integer ipoYear;

    private String sector;

    private String industry;

    private Integer lastQuoteDt = 0;

    private Date createDt;

    private Date lastUpdateDt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.statistics.setCompanyId(id);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.symbol
     *
     * @return the value of company.symbol
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column company.symbol
     *
     * @param symbol the value for company.symbol
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol == null ? null : symbol.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.name
     *
     * @return the value of company.name
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column company.name
     *
     * @param name the value for company.name
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public CompanyStatisticsModel getStatistics() {
		return statistics;
	}

	public void setStatistics(CompanyStatisticsModel statistics) {
		this.statistics = statistics;
	}

	public List<StockQuoteModel> getQuoteList() {
		return quoteList;
	}

	public void setQuoteList(List<StockQuoteModel> quoteList) {
		if(null != quoteList && !quoteList.isEmpty()) {
			for(StockQuoteModel quote : quoteList) {
				quote.setCompanyId(this.id);
			}
			this.quoteList = quoteList;
			Collections.sort(this.quoteList);
			this.lastQuoteDt = this.quoteList.get(this.quoteList.size() - 1).getQuoteDate();
		}
	}

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.ipo_year
     *
     * @return the value of company.ipo_year
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public Integer getIpoYear() {
        return ipoYear;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column company.ipo_year
     *
     * @param ipoYear the value for company.ipo_year
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public void setIpoYear(Integer ipoYear) {
        this.ipoYear = ipoYear;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.sector
     *
     * @return the value of company.sector
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public String getSector() {
        return sector;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column company.sector
     *
     * @param sector the value for company.sector
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public void setSector(String sector) {
        this.sector = sector == null ? null : sector.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.industry
     *
     * @return the value of company.industry
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public String getIndustry() {
        return industry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column company.industry
     *
     * @param industry the value for company.industry
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public void setIndustry(String industry) {
        this.industry = industry == null ? null : industry.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.last_quote_dt
     *
     * @return the value of company.last_quote_dt
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public Integer getLastQuoteDt() {
        return lastQuoteDt;
    }

    public void setLastQuoteDt(Integer lastQuoteDt) {
    	if(null == lastQuoteDt)
    		this.lastQuoteDt = 0;
    	else
    		this.lastQuoteDt = lastQuoteDt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column company.create_dt
     *
     * @return the value of company.create_dt
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public Date getCreateDt() {
        return createDt;
    }

    public void setCreateDt(Date createDt) {
        this.createDt = createDt;
    }

    public Date getLastUpdateDt() {
        return lastUpdateDt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column company.last_update_dt
     *
     * @param lastUpdateDt the value for company.last_update_dt
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    public void setLastUpdateDt(Date lastUpdateDt) {
        this.lastUpdateDt = lastUpdateDt;
    }
}