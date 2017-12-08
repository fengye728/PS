/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.contexts;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.AnalyzerContext;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.systems.EVBBSystem;
import org.maple.profitsystem.systems.EVBBSystemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EVBBSystemContext {
	
	private static Logger logger = Logger.getLogger(EVBBSystemContext.class);
	
	@Autowired
	private AnalyzerContext analyzerContext;
	
	@Autowired
	private EVBBSystem evbbSystem;
	
	public List<EVBBSystemResult> getWatchList(List<CompanyModel> companies) {
		List<EVBBSystemResult> watchList = new LinkedList<>();
		List<EVBBSystemResult> tmpResults = null;
		logger.info("Get watch list...");
		for(CompanyModel company : companies) {
			// DO NOT PROCESS Health Care sector
			if("Health Care".compareTo(company.getSector()) == 0) {
				continue;
			}
			analyzerContext.loadCompanyQuotes(company);
			tmpResults = evbbSystem.analyzeLast(company);
			
			
			if(tmpResults == null || tmpResults.size() == 0)
				continue;
			
			watchList.addAll(tmpResults);
			
			for(EVBBSystemResult result : tmpResults) {
				if(company.getQuoteList().get(result.getDayIndex()).getQuoteDate() < 20170000) {
					continue;
				}
				logger.info(company.getSymbol() + " " + company.getQuoteList().get(result.getDayIndex()).getQuoteDate());
			}
		}
		logger.info("Got watchlist completely!");
		return watchList;
	}
}