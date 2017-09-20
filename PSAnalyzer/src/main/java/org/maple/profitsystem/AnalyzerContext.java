package org.maple.profitsystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
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
	private EVBBSystem evbbSystem;
	
	
	
	public void run() {
		postLoadData();
		
		// use EVBB System ot analyze 
		List<EVBBSystemResult> satisfiedResults = new ArrayList<>();
		for(CompanyModel company : companies) {
			List<EVBBSystemResult> tmpResults = evbbSystem.analyzeAll(company);
			satisfiedResults.addAll(tmpResults);
		}
		
		// output
		for(EVBBSystemResult result : satisfiedResults) {
			
			logger.info(String.format("%s|%s|%s", result.getCompany(), result.getCompany().getStatistics(), result.getCompany().getQuoteList().get(result.getDayIndex())));
		}
	}
	
	private void postLoadData() {
		companies = companyService.getAllCompaniesFull();
	}
}