package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiTypeInfoRepository extends JpaRepository<KospiTypeInfo, Long> {
    List<KospiTypeInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
