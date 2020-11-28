package kr.nonos.monitor.machine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EnableAutoConfiguration
public class MachineStatusWatchdogLog {
	@Id
	@GeneratedValue
	@Column(length = 11)
	private int id;

	@Column(length = 11, nullable = false)
	private int machineId;

	@Column(length = 2)
	private String watchdogStatus;

	@Column(length = 1)
	private String gpuNumber;

	@Column(length = 24)
	private String updateDatetime;
}
