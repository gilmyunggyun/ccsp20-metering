package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.BlockedTemp;
import com.hkmc.ccs.metering.models.entity.BlockedTempId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedTempRepository extends JpaRepository<BlockedTemp, BlockedTempId> {

}
