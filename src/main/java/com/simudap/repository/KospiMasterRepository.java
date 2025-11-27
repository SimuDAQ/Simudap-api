package com.simudap.repository;

import com.simudap.model.KospiMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KospiMasterRepository extends JpaRepository<KospiMaster, Long> {
}
