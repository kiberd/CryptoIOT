package kr.nonos.monitor.machine.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Machine {
	@Id
	@GeneratedValue
	private int id;

	private String mac;

	private String ip;

	private String port;

	private String rack;

	private String stack;

	private String deviceId;

	private String relayNo;

	private int userId;

	@Transient
	private String monitoringJson;
}
