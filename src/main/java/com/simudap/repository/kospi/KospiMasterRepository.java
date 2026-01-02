package com.simudap.repository.kospi;

import com.simudap.model.kospi.KospiMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KospiMasterRepository extends JpaRepository<KospiMaster, Long> {
    Optional<KospiMaster> findByShortCode(String shortCode);

    List<KospiMaster> findAllByNameKrContainingAndIsDeListed(String nameKr, boolean isDeListed);

    List<KospiMaster> findAllByIdInAndIsDeListed(Collection<Long> ids, boolean isDeListed);
}
