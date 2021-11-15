package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.BlockedId;
import com.hkmc.ccs.metering.models.entity.Unblocked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnblockedRepository extends JpaRepository<Unblocked, BlockedId> {

}
