package org.maple.profitsystem;

import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.contexts.EVBBSystemContext;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.StockQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnalyzerContext {
	
	private static Logger logger = Logger.getLogger(AnalyzerContext.class);

	// ---------------- Properties -----------------
	private List<CompanyModel> companies;
	
	// ---------------- Service Beans --------------
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private StockQuoteService stockQuoteService;
	
	@Autowired
	private EVBBSystemContext evbbContext;
	
	public void run() {
		postLoadData();
		
		evbbContext.getWatchList(companies);
	}
	
	public void loadCompanyQuotes(CompanyModel company) {
		if(company != null && company.getQuoteList().size() == 0)
			company.setQuoteList(stockQuoteService.getAllStockQuotesByCompanyId(company.getId()));
	}
	
	public CompanyModel getCompanyBySymbol(String symbol) {
		for(CompanyModel company : companies){
			if(company.getSymbol().equals(symbol)) {
				return company;
			}
		}
		return null;
	}
	
	/**
	 * Get company base and statistics and quotes
	 */
	private void postLoadData() {
		logger.info("Loading Data...");
		companies = companyService.getAllCompaniesWithStatistics();
		logger.info("Loaded Data completely!");
	}
	
}
