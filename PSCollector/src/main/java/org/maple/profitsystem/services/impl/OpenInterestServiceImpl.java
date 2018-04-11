package org.maple.profitsystem.services.impl;

import java.util.List;

import org.maple.profitsystem.mappers.OIModelMapper;
import org.maple.profitsystem.models.OIModel;
import org.maple.profitsystem.services.OpenInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenInterestServiceImpl implements OpenInterestService {
	
	@Autowired
	private OIModelMapper mapper;

	@Override
	public int upsertListOIModel(List<OIModel> oiModelList) {
		int count = 0;
		if (oiModelList != null) {
			for(OIModel oiModel : oiModelList) {
				count += upsertOIModel(oiModel);
			}
		}
		return count;

	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	@Override
	public int upsertOIModel(OIModel oiModel) {
		if(check(oiModel)) {
			try {
				return mapper.upsert(oiModel);
			} catch(Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	private boolean check(OIModel oiModel) {
		if(oiModel != null && oiModel.getCompanyId() != null && oiModel.getCallPut() != null && oiModel.getStrike() != null && oiModel.getExpiration() != null
				&& oiModel.getOiDate() != null) {
			return true;
		} else {
			return false;
		}
	}

}
