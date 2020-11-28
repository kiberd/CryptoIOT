package kr.nonos.monitor.machine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EnableAutoConfiguration
public class MachineStatusLog {
	@Id
	@GeneratedValue
	@Column(length = 11)
	private int id;

	@Column(length = 11, nullable = false)
	private int machineId;

	@Column(length = 255)
	private String version;

	@Column(length = 8)
	private String runningTime;

	@Column(length = 20)
	private String ethereumStats;

	@Column(length = 64)
	private String ethereumSpeed;

	@Column(length = 20)
	private String decredStats;

	@Column(length = 64)
	private String decredSpeed;

	@Column(length = 15)
	private String gpuTemperature;

	@Column(length = 255)
	private String pool;

	@Column(length = 64)
	private String question;

	@Column(length = 24)
	private String updateDatetime;

	@Transient
	private String rack;

	@Transient
	private String stack;

	@Transient
	private String ip;
}
