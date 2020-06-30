package com.hyundaiautoever.ccs.metering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedRepository extends JpaRepository<Blocked, BlockedId> {

}
