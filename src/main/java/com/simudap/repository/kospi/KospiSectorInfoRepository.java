package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiSectorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiSectorInfoRepository extends JpaRepository<KospiSectorInfo, Long> {
    List<KospiSectorInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
