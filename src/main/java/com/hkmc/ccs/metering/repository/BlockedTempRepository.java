package com.hkmc.ccs.metering.repository;

//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkmc.ccs.metering.models.entity.BlockedTemp;
import com.hkmc.ccs.metering.models.entity.BlockedTempId;

@Repository
public interface BlockedTempRepository {

    void save(BlockedTemp build);
}
