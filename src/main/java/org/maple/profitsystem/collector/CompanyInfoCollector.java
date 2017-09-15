/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.collector;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.PSContext;
import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.CompanyStatisticsModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.CompanyStatisticsService;
import org.maple.profitsystem.spiders.FINVIZSpider;
import org.maple.profitsystem.spiders.NASDAQSpider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyInfoCollector {
	
	private static Logger logger = Logger.getLogger(CompanyInfoCollector.class);
	
	/**
	 * The context of the application. 
	 */
	@Autowired
	PSContext context;
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private CompanyStatisticsService companyStatisticsService;
	
	
	/**
	 * Add the new companies from network.
	 * @param targetList
	 * @return
	 */
	public void addNewCompaniesBaseInfo(){
		List<CompanyModel> targetList = context.getCompanyList();
		
		List<CompanyModel> newestList = new ArrayList<CompanyModel>();
		
		// get new companies
		try {
			newestList = NASDAQSpider.fetchCompanyListWithBaseInfo();
			newestList.removeAll(targetList);
			
			logger.info("Update company base info success");
		} catch (HttpException e) {
			logger.error("Update company base info fail: " + e.getMessage());
			// TODO new web site
		}
		// add new companies to db
		for(CompanyModel company : newestList) {
			companyService.addCompanyWithStatistics(company);
		}
		targetList.addAll(newestList);
		
	}
	
	/**
	 * Update the statistics info of the companies in context.
	 * 
	 * @param company
	 * @throws PSException
	 */
	public void updateCompanyStatistics() throws PSException {
		CompanyStatisticsModel tmp = null;
		for(CompanyModel company : context.getCompanyList()) {
			tmp = fetchCompanyStatisticsBySymbol(company.getSymbol());
			if(company.getStatistics().set(tmp) != 0) {
				// update statistics
				companyStatisticsService.updateCompanyStatistics(company.getStatistics());
			}
		}
	}
	
	/**
	 * Fetch the statistics info of the specified company.
	 * @param company
	 */
	private CompanyStatisticsModel fetchCompanyStatisticsBySymbol(String symbol) {
		CompanyStatisticsModel result = null;
		try {
			// get company statistics info
			result = FINVIZSpider.fetchCompanyStatistics(symbol);
		} catch (Exception e) {
			// TODO Use alternative web site
			
			// can not get info to do this
			logger.error(e.getMessage());
		}
		return result;
	}
}