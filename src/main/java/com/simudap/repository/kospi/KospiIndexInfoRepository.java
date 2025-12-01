package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiIndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiIndexInfoRepository extends JpaRepository<KospiIndexInfo, Long> {
    List<KospiIndexInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
