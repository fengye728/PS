package org.maple.profitsystem;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.StockQuoteService;
import org.maple.profitsystem.systems.EVBBSystem;
import org.maple.profitsystem.systems.EVBBSystemResult;
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
	private EVBBSystem evbbSystem;
	
	
	public void run() {
		postLoadData();
		
		List<EVBBSystemResult> tmpResults = null;
		// use EVBB System ot analyze 
		List<EVBBSystemResult> satisfiedResults = new LinkedList<>();
		for(CompanyModel company : companies) {
			company.setQuoteList(stockQuoteService.getAllStockQuotesByCompanyId(company.getId()));
			tmpResults = evbbSystem.analyzeAll(company);
			
			if(tmpResults == null || tmpResults.size() == 0)
				continue;
			
			satisfiedResults.addAll(tmpResults);
			//logger.info(company.getSymbol() + ":" + tmpResults.size());
		}
		
		// output
		int profitThreshold = 10;
		
		logger.info("Satisfied result number: " + satisfiedResults.size());
		
		Double roic = null;

		int bigNum = 0;
		int lessNum = 0;
		int unkonwNum = 0;
		double totoalRoic = 0;
		double gainRoic = 0;
		
		for(EVBBSystemResult result : satisfiedResults) {
//				if (result.getCompany().getQuoteList().get(result.getDayIndex()).getQuoteDate() < 20170101) {
//					continue;
//				}
			roic = evbbSystem.evaluateByTDD(result);
			if(roic == null) {
				unkonwNum++;
			} else if(roic < 0) {
				lessNum++;
				totoalRoic += roic;
			} else {
				bigNum++;
				gainRoic += roic;
				totoalRoic += roic;
			}
			
		}
		double perc = (double)bigNum / (bigNum + lessNum);
		logger.info(String.format("Total: %.2f | Gain: %.2f | Perc : %.2f - %d", totoalRoic, gainRoic, perc, bigNum));
	}
	
	/**
	 * Get company base and statistics
	 */
	private void postLoadData() {
		companies = companyService.getAllCompaniesWithStatistics();
	}
}
