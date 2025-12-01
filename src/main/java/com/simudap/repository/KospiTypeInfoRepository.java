package com.simudap.repository;

import com.simudap.model.KospiTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiTypeInfoRepository extends JpaRepository<KospiTypeInfo, Long> {
    List<KospiTypeInfo> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
