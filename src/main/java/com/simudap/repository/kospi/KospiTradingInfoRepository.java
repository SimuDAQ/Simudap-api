package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiTradingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiTradingInfoRepository extends JpaRepository<KospiTradingInfo, Long> {
    List<KospiTradingInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
