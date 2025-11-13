package com.simudap.repository;

import com.simudap.model.KospiMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface KospiMasterRepository extends JpaRepository<KospiMaster, Long> {
    void deleteByIds(Collection<Long> ids);
}
