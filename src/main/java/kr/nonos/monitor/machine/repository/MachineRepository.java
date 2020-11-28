package kr.nonos.monitor.machine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.nonos.monitor.machine.entity.Machine;

public interface MachineRepository extends JpaRepository<Machine, Integer> {
}
