package org.maple.profitsystem;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.RoicModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.StockQuoteService;
import org.maple.profitsystem.systems.EVBBSystem;
import org.maple.profitsystem.systems.EVBBSystemResult;
import org.maple.profitsystem.utils.TAUtil;
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
	
	public void printDebug(List<EVBBSystemResult> companyResultList) throws PSException {
	
		for(EVBBSystemResult result : companyResultList) {
			CompanyModel company = result.getCompany();
			
			String info = String.format("%s,%s | 50SMAVolume:%d, 50EMAVolume:%d - MaxResistanceVolume:%d", company.getSymbol(), company.getSector(),
					TAUtil.SMAVolumeByIndex(company.getQuoteList(), result.getDayIndex() - 1, 50),
					TAUtil.EMAVolumeByIndex(company.getQuoteList(), result.getDayIndex() - 1, 50),
					TAUtil.MaxResistanceVolumeByIndex(company.getQuoteList(), result.getDayIndex(), 50));
			
			System.out.println(info);
			try {
				evbbSystem.evaluateByTDD(result);
			} catch (PSException e) {
				e.printStackTrace();
			}
		}
	}
	
	
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
			try {
				printDebug(tmpResults);
			} catch (PSException e) {
				e.printStackTrace();
			}
			//logger.info(company.getSymbol() + ":" + tmpResults.size());
		}
		
		// output
		int profitThreshold = 10;
		
		logger.info("Satisfied result number: " + satisfiedResults.size());
		
		List<RoicModel> roicList = new LinkedList<>();

		int bigNum = 0;
		int lessNum = 0;
		
		for(EVBBSystemResult result : satisfiedResults) {
			if (result.getCompany().getQuoteList().get(result.getDayIndex()).getQuoteDate() < 20170101) {
				continue;
			}
			try {
				RoicModel roic;
				roic = evbbSystem.evaluateByTDD(result);
				roicList.add(roic);
			} catch (PSException e) {
				e.printStackTrace();
			}
		}
		double totalCount = roicList.size();
		
		long gainRoicCount = roicList.stream().filter( roic -> roic.getRoic() >= 0).count();
		double gainRoicTotal = roicList.stream().filter( roic -> roic.getRoic() >= 0).map( roic -> roic.getRoic()).reduce( (r1, r2) -> r1 + r2).get() * 100;
		int gainRoicDays = roicList.stream().filter( roic -> roic.getRoic() >= 0).map( roic -> roic.getDays()).reduce( (r1, r2) -> r1 + r2).get();
		
		long lossRoicCount = roicList.stream().filter( roic -> roic.getRoic() < 0).count();
		double lossRoicTotal = roicList.stream().filter( roic -> roic.getRoic() < 0).map( roic -> roic.getRoic()).reduce( (r1, r2) -> r1 + r2).get() * 100;
		int lossRoicDays = roicList.stream().filter( roic -> roic.getRoic() < 0).map( roic -> roic.getDays()).reduce( (r1, r2) -> r1 + r2).get();
		
		String gainStr = String.format("Gain - Total ROIC:%.2f%%, Probability:%.2f%%, ROIC / DAY: %.2f%%", gainRoicTotal, gainRoicCount / totalCount, gainRoicTotal / gainRoicDays);
		String lossStr = String.format("Loss - Total ROIC:%.2f%%, Probability:%.2f%%, ROIC / DAY: %.2f%%", lossRoicTotal, lossRoicCount / totalCount, lossRoicTotal / lossRoicDays);
		
		logger.info(gainStr);
		logger.info(lossStr);
	}
	
	/**
	 * Get company base and statistics
	 */
	private void postLoadData() {
		companies = companyService.getAllCompaniesWithStatistics();
	}
}
