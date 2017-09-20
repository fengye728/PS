package org.maple.profitsystem.services;

import java.util.List;

import org.maple.profitsystem.models.CompanyModel;

public interface CompanyService {
	
	// ------------------ Database Operations ---------------------
	List<CompanyModel> getAllCompanies();
	
	List<CompanyModel> getAllCompaniesWithStatistics();
	
	List<CompanyModel> getAllCompaniesFull();
}
