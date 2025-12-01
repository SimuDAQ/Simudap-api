package com.simudap.repository;

import com.simudap.model.KospiTradingInfo;
import com.simudap.model.KospiTradingStatusFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiTradingInfoRepository extends JpaRepository<KospiTradingInfo, Long> {
    List<KospiTradingInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
