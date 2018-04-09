package org.maple.profitsystem.services;

import java.util.List;

import org.maple.profitsystem.models.OIModel;

public interface OpenInterestService {
	
	int addOIModel(OIModel oiModel);
	
	int addListOIModel(List<OIModel> oiModelList);
}
