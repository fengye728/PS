/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.collector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.maple.profitsystem.ConfigProperties;
import org.maple.profitsystem.PSContext;
import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.CompanyStatisticsModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.CompanyStatisticsService;
import org.maple.profitsystem.spiders.FINVIZSpider;
import org.maple.profitsystem.spiders.NASDAQSpider;
import org.maple.profitsystem.spiders.YAHOOSpider;
import org.maple.profitsystem.utils.TradingDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Collect all information of company and add these into database.
 * 
 * @author SEELE
 *
 */
@Component
public class CompanyInfoCollector {
	
	private static Logger logger = Logger.getLogger(CompanyInfoCollector.class);
	
	/**
	 * The context of the application. 
	 */
	@Autowired
	PSContext context;
	
	@Autowired
	private ConfigProperties properties;	
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private CompanyStatisticsService companyStatisticsService;
	
	
	/**
	 * Add the new companies from network.
	 * @param targetList
	 * @return
	 */
	public void addListNewCompaniesBaseInfo(){
		logger.info("Updating base information of companies...");
		List<CompanyModel> targetList = context.getCompanyList();
		
		List<CompanyModel> newestList = new ArrayList<CompanyModel>();
		
		// get new companies
		try {
			newestList = NASDAQSpider.fetchCompanyListWithBaseInfo();
			
			newestList.removeAll(targetList);
			
			Set<CompanyModel> set = new HashSet<>(newestList);
			newestList = new ArrayList<>(set);
		} catch (HttpException e) {
			// TODO new web site
			
			logger.error("Updated base information of companies failed: " + e.getMessage());
		}
		
		// add new companies to db
		int count = 0;
		for(CompanyModel company : newestList) {
			count += companyService.addCompanyWithStatistics(company);
			companyStatisticsService.updateCompanyStatistics(company.getStatistics());
		}
		targetList.addAll(newestList);
		logger.info("Updated base information of companies success. Count: " + count);
	}
	
	/**
	 * Update the statistics info of the companies in context.
	 * 
	 * @param company
	 * @throws PSException
	 */
	public void updateListCompanyStatistics() {
		logger.info("Updating statistics of companies...");
		CompanyStatisticsModel tmp = null;
		int invalidCount = 0;
		
		Calendar nowDt = Calendar.getInstance();
		nowDt.add(Calendar.DAY_OF_MONTH, Integer.valueOf(properties.getStatisticsUpdatePeriod()));
		
		for(CompanyModel company : context.getCompanyList()) {
			// check if the statistics need to update
			if(!company.getStatistics().isEmpty() && company.getStatistics().getLastUpdateDt().before(nowDt.getTime())) {
				continue;
			}
			tmp = fetchCompanyStatisticsBySymbol(company.getSymbol());
			if(company.getStatistics().set(tmp) != 0) {
				// update statistics
				invalidCount += 1 - companyStatisticsService.updateCompanyStatistics(company.getStatistics());
			}
		}
		logger.info("Updated statistics of companies completed! Count of falied records: " + invalidCount);
	}
	
	public void updateListCompanyQuotes() {
		logger.info("Updating stock quotes of companies...");
		int invalidCount = 0;
		
		Calendar nowDt = Calendar.getInstance();
		nowDt.add(Calendar.DAY_OF_MONTH, Integer.valueOf(properties.getQuotesUpdatePeriod()));
		
		for(CompanyModel company : context.getCompanyList()) {
			Date lastDate = TradingDateUtil.convertNumDate2Date(company.getLastQuoteDt());
			if(lastDate != null && lastDate.before(nowDt.getTime())) {
				continue;
			}
			 
			invalidCount += 1 - updateCompanyQuotesByCompany(company);
		}
		logger.info("Updated stock quotes of companies completed! Count of failed records: " + invalidCount);
	}
	
	/**
	 * Update the newest quotes of the specified company.
	 * 
	 * @param company
	 * @return
	 */
	private int updateCompanyQuotesByCompany(CompanyModel company) {
		try {
			// get and set newest quotes
			company.setQuoteList(NASDAQSpider.fetchNewestStockQuotesByCompany(company));
			companyService.updateCompanyWithQuotes(company);
			return 1;
		} catch (PSException e) {
			logger.error(e.getMessage());
			return 0;
		} catch (HttpException e) {
			// TODO Using new web site
			
			logger.error(e.getMessage());
			return 0;
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
			// ues YAHOO to fetch
			try {
				result = YAHOOSpider.fetchCompanyStatistics(symbol);
			} catch (Exception e1) {
				// can not get info to do this
				logger.error(e.getMessage());
			}
		}
		return result;
	}
}