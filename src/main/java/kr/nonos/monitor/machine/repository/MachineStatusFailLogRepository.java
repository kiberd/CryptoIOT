package kr.nonos.monitor.machine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.nonos.monitor.machine.entity.MachineStatusFailLog;

public interface MachineStatusFailLogRepository  extends JpaRepository<MachineStatusFailLog, Integer> {
	Page<MachineStatusFailLog> findByMachineId(int id, Pageable pageable);
}
