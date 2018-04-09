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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.maple.profitsystem.Application;
import org.maple.profitsystem.CollectorContext;
import org.maple.profitsystem.ConfigProperties;
import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.CompanyStatisticsModel;
import org.maple.profitsystem.models.OIModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.CompanyStatisticsService;
import org.maple.profitsystem.services.OpenInterestService;
import org.maple.profitsystem.spiders.CompanySpider;
import org.maple.profitsystem.spiders.FINVIZSpider;
import org.maple.profitsystem.spiders.OISpider;
import org.maple.profitsystem.spiders.QuoteSpider;
import org.maple.profitsystem.spiders.impl.InvestopediaOISpider;
import org.maple.profitsystem.spiders.impl.InvestopediaQuoteSpider;
import org.maple.profitsystem.spiders.impl.NasdaqCompanySpider;
import org.maple.profitsystem.spiders.impl.NasdaqQuoteSpider;
import org.maple.profitsystem.utils.TradingDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Collect all information of company and add these into database by period specified properties.
 * 
 * @author SEELE
 *
 */
@Component
public class CompanyInfoCollector {
	
	private static Logger logger = Logger.getLogger(CompanyInfoCollector.class);
	
	private final static int THREAD_POOL_WAIT_MINUTES = 1;
	
	private static List<CompanySpider> companySpiders = new ArrayList<>();
	
	static {
		companySpiders.add(new NasdaqCompanySpider());
	}
	/**
	 * The context of the application. 
	 */
	@Autowired
	CollectorContext context;
	
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
		for(CompanySpider spider : companySpiders) {
			try {
				newestList = spider.fetchCompanyList();
				newestList.removeAll(targetList);
				Set<CompanyModel> set = new HashSet<>(newestList);
				newestList = new ArrayList<>(set);
			} catch (HttpException e) {
				logger.error(e.getMessage());
			}
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
		// reset fail count
		CompanyStatisticsUpdateTask.failCount = 0;
		
		Calendar nowDt = Calendar.getInstance();
		nowDt.add(Calendar.DAY_OF_MONTH, -properties.getStatisticsUpdatePeriod());
		
		ExecutorService executor = getNewThreadPool();
		
		for(CompanyModel company : context.getCompanyList()) {
			// check if the statistics need to update
			if(company.getStatistics().isEmpty() || company.getStatistics().getLastUpdateDt().before(nowDt.getTime())) {
				// update statistics
				executor.execute(new CompanyStatisticsUpdateTask(company));
			}
		}
		awaitThreadPool(executor);
		logger.info("Updated statistics of companies completed! Total: " + context.getCompanyList().size() + " Fail: " + CompanyStatisticsUpdateTask.failCount);
	}
	
	/**
	 * Update a list company quotes in pscontext.
	 */
	public void updateListCompanyQuotes() {
		logger.info("Updating stock quotes of companies...");
		// reset fail count
		CompanyQuotesUpdateTask.failCount = 0;
		
		ExecutorService executor = getNewThreadPool();
		
		for(CompanyModel company : context.getCompanyList()) {
			if(!isNewestQuotes(company)) {
				executor.execute(new CompanyQuotesUpdateTask(company)); 
			}
		}
		awaitThreadPool(executor);
		
		logger.info("Updated stock quotes of companies completed! Total: " + context.getCompanyList().size() + " Fail: " + CompanyQuotesUpdateTask.failCount);
		
		
	}
	
	/**
	 * Update a list open interest in pscontext.
	 */
	public void updateOpenInterest() {
		logger.info("Updating option open interest...");
		OpenInterestUpdateTask.failCount = 0;
		
		ExecutorService executor = getNewThreadPool();
		
		for(CompanyModel company : context.getCompanyList()) {
			executor.execute(new OpenInterestUpdateTask(company)); 

		}
		
		awaitThreadPool(executor);		
		
		logger.info("Updated option open interest of companies completed! Total: " + context.getCompanyList().size() + " Fail: " + OpenInterestUpdateTask.failCount);
		
	}
	
	/**
	 * Test if the quotes of this company is newest.
	 * @param company
	 * @return
	 */
	private boolean isNewestQuotes(CompanyModel company) {
		
		if(company.getLastQuoteDt() == 0) {
			return false;
		} else {
			Date lastDate = TradingDateUtil.convertNumDate2Date(company.getLastQuoteDt());
			Date nowDt = new Date();
			int gapDays = TradingDateUtil.betweenTradingDays(lastDate, nowDt);
			if(gapDays >= properties.getQuotesUpdatePeriod()) {
				return false;
			} else {
				return true;
			} 
		}
	}
	
	/**
	 * Get a new pool of threads.
	 *
	 * @return
	 */
	private ExecutorService getNewThreadPool() {
		return Executors.newFixedThreadPool(properties.getMaxThreads());
	}
	
	/**
	 * Wait for execution completed of thread in pool.
	 * 
	 * @param executor
	 */
	private void awaitThreadPool(ExecutorService executor) {
		try {
			executor.shutdown();
			while(!executor.awaitTermination(THREAD_POOL_WAIT_MINUTES, TimeUnit.MINUTES));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/**
 * Fetch a company statistics and update it to database.
 * 
 * @author SEELE
 *
 */
class CompanyStatisticsUpdateTask implements Runnable {

	// count the number of fail companies
	public static int failCount;
	
	private static CompanyStatisticsService companyStatisticsService;
	
	private static Logger logger = Logger.getLogger(CompanyStatisticsUpdateTask.class);
	
	private CompanyModel company;
	
	static {
		companyStatisticsService = Application.springContext.getBean(CompanyStatisticsService.class);
	}
	
	public CompanyStatisticsUpdateTask(CompanyModel company) {
		this.company = company;
	}
	
	@Override
	public void run() {
		if(company != null)
			updateCompanyStatistics();
	}
	
	private void updateCompanyStatistics() {
		
		CompanyStatisticsModel tmp;
		try {
			tmp = fetchCompanyStatisticsBySymbol(company.getSymbol());
			if(company.getStatistics().set(tmp) != 0) {
				// update statistics
				companyStatisticsService.updateCompanyStatistics(company.getStatistics());
			}
		} catch (Exception e) {
			logger.error(company.getSymbol() + " : " + e.getMessage());
			++failCount;
		}
	}
	
	/**
	 * Fetch the statistics info of the specified company.
	 * @param company
	 * @throws PSException 
	 */
	private CompanyStatisticsModel fetchCompanyStatisticsBySymbol(String symbol) throws PSException {
		CompanyStatisticsModel result = null;
		
		String errorMsg = "";
		try {
			// get company statistics info
			result = FINVIZSpider.fetchCompanyStatistics(symbol);
		} catch (HttpException e) {
			errorMsg += "FINVIZ Http failed | ";
		} catch (PSException e) {
			errorMsg += e.getMessage();
		}
		// use YAHOO to fetch ( yahoo is blocked now)
		// result = YAHOOSpider.fetchCompanyStatistics(symbol);
		
		
		if(result == null) {
			throw new PSException(errorMsg);
		}
		return result;
	}
	
}

/**
 * Fetch a company quotes and update it to database.
 * 
 * @author SEELE
 *
 */
class CompanyQuotesUpdateTask implements Runnable {
	
	private static List<QuoteSpider> spiders = new ArrayList<>();
	
	// count the number of fail companies
	public static int failCount;

	private static CompanyService companyService;
	
	private static Logger logger = Logger.getLogger(CompanyQuotesUpdateTask.class);
	
	private CompanyModel company;
	
	static {
		// add spiders
		spiders.add(new InvestopediaQuoteSpider());
		spiders.add(new NasdaqQuoteSpider());
		
		companyService = Application.springContext.getBean(CompanyService.class);
	}
	
	public CompanyQuotesUpdateTask(CompanyModel company) {
		this.company = company;
	}
	
	@Override
	public void run() {
		if(company != null)
			updateCompanyQuotes();
	}
	
	/**
	 * Update the newest quotes of the specified company.
	 * 
	 * @param company
	 * @return
	 */
	private void updateCompanyQuotes() {
		try{
			// get and set newest quotes
			company.setQuoteList(fetchNewestStockQuotes());
			companyService.updateCompanyWithQuotes(company);
			
			// free memory of quote list
			company.setQuoteList(null);
		} catch(Exception e) {
			logger.error(company.getSymbol() + ": " + e.getMessage());
			++failCount;
		}

	}
	
	/**
	 * Fetch the statistics info of the specified company.
	 * @param company
	 * @throws PSException 
	 */
	private List<StockQuoteModel> fetchNewestStockQuotes() throws Exception {
		List<StockQuoteModel> result = null;
		
		for(QuoteSpider spider : spiders) {
			try {
				result = spider.fetchQuotes(company.getSymbol(), company.getLastQuoteDt());
				if(result != null && result.size() > 0) {
					return result;
				}
			} catch (HttpException e) {
			}
		}
		throw new Exception("fetch quotes failed!");
	}
}

class OpenInterestUpdateTask implements Runnable {
	
	public static int failCount;

	private static Logger logger = Logger.getLogger(OpenInterestUpdateTask.class);
	
	private static List<OISpider> spiders = new ArrayList<>();
	
	private static OpenInterestService openInterestService;
	
	static {
		spiders.add(new InvestopediaOISpider());
		
		// get service bean from spring context
		openInterestService = Application.springContext.getBean(OpenInterestService.class);
	}
	
	private CompanyModel company;
	
	public OpenInterestUpdateTask(CompanyModel com) {
		this.company = com;
	}
	@Override
	public void run() {
		if(company != null) {
			try {
				List<OIModel> oiList = fetchOpenInterest();
				openInterestService.addListOIModel(oiList);
			} catch (PSException e) {
				logger.error(e.getMessage());
				++failCount;
			}
		}
	}

	
	private List<OIModel> fetchOpenInterest() throws PSException {
		for(OISpider spider : spiders) {
			try {
				return spider.fetchOpenInterest(company);
			} catch (PSException e) {
				logger.error("Fail fetching open interest - " + company.getSymbol() + " : " + e.getMessage());
			}
		}
		throw new PSException("Fail to fetch open interest");
	}
}