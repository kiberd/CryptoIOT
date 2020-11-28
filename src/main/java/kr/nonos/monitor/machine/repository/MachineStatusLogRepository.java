package kr.nonos.monitor.machine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.nonos.monitor.machine.entity.MachineStatusLog;

public interface MachineStatusLogRepository extends JpaRepository<MachineStatusLog, Integer> {
}