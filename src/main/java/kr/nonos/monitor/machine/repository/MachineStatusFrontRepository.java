package kr.nonos.monitor.machine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.nonos.monitor.machine.entity.MachineStatusFront;

public interface MachineStatusFrontRepository extends JpaRepository<MachineStatusFront, Integer> {
	@Query(value = "select * from machine_status_front t where running_time is not null", nativeQuery = true)
	List<MachineStatusFront> findAllOn();

	@Query(value = "select * from machine_status_front t where running_time is null", nativeQuery = true)
	List<MachineStatusFront> findAllOff();

	@Query(value = "select * from machine_status_front a, machine b where a.machine_id = b.id and b.user_id = (:userId)", nativeQuery = true)
	List<MachineStatusFront> findAllByUser(@Param("userId") int userId);

	@Query(value = "select * from machine_status_front a, machine b where a.running_time is not null and a.machine_id = b.id and b.user_id = (:userId)", nativeQuery = true)
	List<MachineStatusFront> findOnByUser(@Param("userId") int userId);

	@Query(value = "select * from machine_status_front a, machine b where a.running_time is null and a.machine_id = b.id and b.user_id = (:userId)", nativeQuery = true)
	List<MachineStatusFront> findOffByUser(@Param("userId") int userId);
}