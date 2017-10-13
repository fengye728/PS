package org.maple.profitsystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.RoicModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.StockQuoteService;
import org.maple.profitsystem.systems.EVBBSystem;
import org.maple.profitsystem.systems.EVBBSystemResult;
import org.maple.profitsystem.utils.CSVUtil;
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
	public void run() {
		postLoadData();
		
		List<EVBBSystemResult> tmpResults = null;
		
		
		stockQuoteService.getAllStockQuotesByCompanyId(3114L);
		
		// use EVBB System ot analyze 
		List<EVBBSystemResult> satisfiedResults = new LinkedList<>();
		for(CompanyModel company : companies) {
//			if(company.getId() != 3114L) {
//				continue;
//			}
			// DO NOT PROCESS Health Care sector
			if("Health Care".compareTo(company.getSector()) == 0) {
				continue;
			}
			company.setQuoteList(stockQuoteService.getAllStockQuotesByCompanyId(company.getId()));
			tmpResults = evbbSystem.analyzeAll(company);
			
			
			if(tmpResults == null || tmpResults.size() == 0)
				continue;
			
			satisfiedResults.addAll(tmpResults);
		}
		
		// output
		int profitThreshold = 10;
		
		logger.info("Satisfied result number: " + satisfiedResults.size());
		
		List<RoicModel> roicList = new LinkedList<>();

		List<String> csvFile = new ArrayList<>();
		for(EVBBSystemResult result : satisfiedResults) {
//			if (result.getCompany().getQuoteList().get(result.getDayIndex()).getQuoteDate() < 20170101) {
//				continue;
//			}
			try {
				RoicModel roic = evbbSystem.evaluateByTDD(result);
				if(null == roic) {
					continue;
				}
				roicList.add(roic);
				
				double entryPrice = evbbSystem.entryPrice(result);
				// output csv for python analyze
				int entryIndex = roic.getEntryIndex();
				int exitIndex = roic.getDays() + entryIndex;
				List<StockQuoteModel> quoteList = result.getCompany().getQuoteList();
				double rateBeforeVol = (double)quoteList.get(result.getDayIndex()).getVolume() / TAUtil.MaxVolumeByIndex(quoteList, entryIndex - 1, 50);
				double isTDD = TAUtil.ThreeDayDifference(quoteList, result.getDayIndex());
				double rateEMAVol = (double)quoteList.get(result.getDayIndex()).getVolume() / TAUtil.EMAVolumeByIndex(quoteList, entryIndex - 1, 50);
				double rateSellVol;
				if(TAUtil.MaxSellVolumeByIndex(quoteList, exitIndex - 1, roic.getDays() - 1) <= 0) {
					rateSellVol = 0;
				} else {
					rateSellVol = (double)quoteList.get(result.getDayIndex()).getVolume() / TAUtil.MaxSellVolumeByIndex(quoteList, roic.getDays() + result.getDayIndex(), roic.getDays()); 
				}
				
				if(TAUtil.MaxHighPriceByIndex(quoteList, exitIndex - 1, roic.getDays()) == 0){
					System.out.println("sd");
				}				
				double minRoic = TAUtil.LowestPriceByIndex(quoteList, exitIndex - 1, roic.getDays()) * 100 / entryPrice - 100;
				double maxRoic = TAUtil.MaxHighPriceByIndex(quoteList, exitIndex - 1, roic.getDays()) * 100 / entryPrice - 100;
				
				String item = rateBeforeVol + ","
						+ isTDD + ","
						+ rateEMAVol + ","
						+ rateSellVol + ","
						+ minRoic + ","
						+ maxRoic + ","
						+ roic.getRoic() * 100;
				csvFile.add(item);
			} catch (PSException e) {
				e.printStackTrace();
			}
		}
		CSVUtil.writeList2CSV("output", csvFile);
		
		double totalCount = roicList.size();
		
		long gainRoicCount = roicList.stream().filter( roic -> roic.getRoic() >= 0).count();
		double gainRoicTotal = roicList.stream().filter( roic -> roic.getRoic() >= 0).map( roic -> roic.getRoic()).reduce( (r1, r2) -> r1 + r2).get() * 100;
		int gainRoicDays = roicList.stream().filter( roic -> roic.getRoic() >= 0).map( roic -> roic.getDays()).reduce( (r1, r2) -> r1 + r2).get();
		
		long lossRoicCount = roicList.stream().filter( roic -> roic.getRoic() < 0).count();
		double lossRoicTotal = roicList.stream().filter( roic -> roic.getRoic() < 0).map( roic -> roic.getRoic()).reduce( (r1, r2) -> r1 + r2).get() * 100;
		int lossRoicDays = roicList.stream().filter( roic -> roic.getRoic() < 0).map( roic -> roic.getDays()).reduce( (r1, r2) -> r1 + r2).get();
		
		String gainStr = String.format("Gain - Total ROIC:%.2f%%, Probability:%.2f%%, ROIC / DAY: %.2f%%", gainRoicTotal, gainRoicCount / totalCount, gainRoicTotal / gainRoicDays);
		String lossStr = String.format("Loss - Total ROIC:%.2f%%, Probability:%.2f%%, ROIC / DAY: %.2f%%", lossRoicTotal, lossRoicCount / totalCount, lossRoicTotal / lossRoicDays);
		
		logger.info("Satisfied result number: " + satisfiedResults.size());
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
