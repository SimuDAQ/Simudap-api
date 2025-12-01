package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KospiMasterRepository extends JpaRepository<KospiMaster, Long> {
}
