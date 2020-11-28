package kr.nonos.monitor.machine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.nonos.monitor.machine.entity.MachineStatusWatchdogLog;

public interface MachineStatusWatchdogLogRepository extends JpaRepository<MachineStatusWatchdogLog, Integer> {
	Page<MachineStatusWatchdogLog> findByMachineId(int id, Pageable pageable);
}
