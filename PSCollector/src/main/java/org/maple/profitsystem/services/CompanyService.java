package org.maple.profitsystem.services;

import java.util.List;

import org.maple.profitsystem.models.CompanyModel;

public interface CompanyService {
	
	// ------------------ Normal Operations ----------------------
	
	// ------------------ Disk Operations -------------------------
	List<CompanyModel> loadCompanyWithFullInfoListFromDisk();
	
	int persistCompanyWithFullInfoToDisk(CompanyModel record);
	
	int persistCompanyWithFullInfoListToDisk(List<CompanyModel> records);
	
	// ------------------ Database Operations ---------------------
	List<CompanyModel> getAllCompanies();
	
	List<CompanyModel> getAllCompaniesWithStatistics();
	
	List<CompanyModel> getAllCompaniesFull();
	
	int addCompanyFullInfo(CompanyModel record);
	
	int addListCompaniesFullInfo(List<CompanyModel> records);
	
	int addCompanyWithStatistics(CompanyModel record);
	
	int addListCompaniesWithStatistics(List<CompanyModel> records);
	
	int updateCompany(CompanyModel record);

	int updateCompanyWithQuotes(CompanyModel record);
}
