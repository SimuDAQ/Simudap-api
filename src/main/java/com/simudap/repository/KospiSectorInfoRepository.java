package com.simudap.repository;

import com.simudap.model.KospiSectorInfo;
import com.simudap.model.KospiTradingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiSectorInfoRepository extends JpaRepository<KospiSectorInfo, Long> {
    List<KospiSectorInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
