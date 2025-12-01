package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiTradingStatusFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiTradingStatusFlagRepository extends JpaRepository<KospiTradingStatusFlag, Long> {
    List<KospiTradingStatusFlag> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
