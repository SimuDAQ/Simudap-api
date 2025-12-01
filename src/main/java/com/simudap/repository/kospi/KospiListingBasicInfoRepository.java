package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiListingBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiListingBasicInfoRepository extends JpaRepository<KospiListingBasicInfo, Long> {
    List<KospiListingBasicInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
