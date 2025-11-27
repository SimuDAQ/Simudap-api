package com.simudap.repository;

import com.simudap.model.KospiRiskFlag;
import com.simudap.model.KospiSectorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiRiskFlagRepository extends JpaRepository<KospiRiskFlag, Long> {
    List<KospiRiskFlag> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
