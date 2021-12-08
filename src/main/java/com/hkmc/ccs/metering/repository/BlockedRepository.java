package com.hkmc.ccs.metering.repository;

import java.util.List;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hkmc.ccs.metering.models.entity.Blocked;
import com.hkmc.ccs.metering.models.entity.BlockedId;

@Repository
public interface BlockedRepository extends JpaRepository<Blocked, BlockedId> {

  List<Blocked> findAllByCarId(@Param("carId") String carId);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from blocked \n" +
                                       "where car_Id = :carId"
  )
  int deleteByCarId(@Param("carId") String carId);

}
