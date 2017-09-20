package org.maple.profitsystem.mappers;

import java.util.List;

import org.maple.profitsystem.models.CompanyModel;

public interface CompanyModelMapper {
    
    /**
     * Select all company model with base info
     * @return
     */
    List<CompanyModel> selectAll();
    
    List<CompanyModel> selectAllWithStatistics();
    
    List<CompanyModel> selectAllFull();
}