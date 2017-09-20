package org.maple.profitsystem.mappers;

import org.maple.profitsystem.models.CompanyStatisticsModel;

public interface CompanyStatisticsModelMapper {

    CompanyStatisticsModel selectByPrimaryKey(Long id);
    
    CompanyStatisticsModel selectByCompanyId(long companyId);
}