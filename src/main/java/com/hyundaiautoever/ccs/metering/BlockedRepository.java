package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.models.entity.Blocked;
import com.hyundaiautoever.ccs.metering.models.entity.BlockedId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedRepository extends JpaRepository<Blocked, BlockedId> {

}
