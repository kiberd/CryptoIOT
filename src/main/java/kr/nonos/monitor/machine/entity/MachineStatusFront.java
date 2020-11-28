package kr.nonos.monitor.machine.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MachineStatusFront {
	@Id
	private int machineId;

	private String version;

	private String runningTime;

	private String ethereumStats;

	private String ethereumSpeed;

	private String decredStats;

	private String decredSpeed;

	private String gpuTemperature;

	private String pool;

	private String question;

	private String updateDatetime;

	@Transient
	private String rack;

	@Transient
	private String stack;

	@Transient
	private String ip;

	@Transient
	private String mac;
}
