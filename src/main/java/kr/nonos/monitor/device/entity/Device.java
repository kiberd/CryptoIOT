package kr.nonos.monitor.device.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Device {
	@Id
	@GeneratedValue
	private int id;

	private String mac;

	private String ip;

	private String port;

	private String rack;
}
