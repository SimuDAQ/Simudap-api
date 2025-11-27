package com.simudap.repository;

import com.simudap.model.KospiIndustryInfo;
import com.simudap.model.KospiListingBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiIndustryInfoRepository extends JpaRepository<KospiIndustryInfo, Long> {
    List<KospiIndustryInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
