package kr.nonos.monitor.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.nonos.monitor.device.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {
}
