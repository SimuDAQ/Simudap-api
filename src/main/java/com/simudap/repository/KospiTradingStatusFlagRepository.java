package com.simudap.repository;

import com.simudap.model.KospiTradingStatusFlag;
import com.simudap.model.KospiTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiTradingStatusFlagRepository extends JpaRepository<KospiTradingStatusFlag, Long> {
    List<KospiTradingStatusFlag> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
