package com.simudap.repository;

import com.simudap.model.KisToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KisTokenRepository extends JpaRepository<KisToken, Long> {
    Optional<KisToken> findOneByOrderByTokenExpiredDesc();
}
