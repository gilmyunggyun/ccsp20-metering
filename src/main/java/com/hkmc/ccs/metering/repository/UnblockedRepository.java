package com.hkmc.ccs.metering.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkmc.ccs.metering.models.entity.BlockedId;
import com.hkmc.ccs.metering.models.entity.Unblocked;

@Repository
public interface UnblockedRepository extends JpaRepository<Unblocked, BlockedId> {

}
