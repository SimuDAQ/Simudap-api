package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiRiskFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiRiskFlagRepository extends JpaRepository<KospiRiskFlag, Long> {
    List<KospiRiskFlag> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
