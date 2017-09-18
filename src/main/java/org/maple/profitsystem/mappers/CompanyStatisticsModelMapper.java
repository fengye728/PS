package org.maple.profitsystem.mappers;

import org.maple.profitsystem.models.CompanyStatisticsModel;

public interface CompanyStatisticsModelMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table company_statistics
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table company_statistics
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    int insert(CompanyStatisticsModel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table company_statistics
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    int insertSelective(CompanyStatisticsModel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table company_statistics
     *
     * @mbg.generated Fri Sep 15 15:30:45 CST 2017
     */
    CompanyStatisticsModel selectByPrimaryKey(Long id);
    
    CompanyStatisticsModel selectByCompanyId(long companyId);
    
    int updateByCompanyIdSelective(CompanyStatisticsModel record);
}