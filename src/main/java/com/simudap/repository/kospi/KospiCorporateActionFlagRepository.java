package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiCorporateActionFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface KospiCorporateActionFlagRepository extends JpaRepository<KospiCorporateActionFlag, Long> {
    List<KospiCorporateActionFlag> findAllByKospiMasterIdIn(Collection<Long> kospiMasterIds);
}
