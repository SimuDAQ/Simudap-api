package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiFinancialInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiFinancialInfoRepository extends JpaRepository<KospiFinancialInfo, Long> {
    List<KospiFinancialInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
