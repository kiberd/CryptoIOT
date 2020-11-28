package kr.nonos.monitor.machine.controller;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.nonos.monitor.device.entity.Device;
import kr.nonos.monitor.device.repository.DeviceRepository;
import kr.nonos.monitor.login.entity.User;
import kr.nonos.monitor.login.repository.UserRepository;
import kr.nonos.monitor.login.service.UserService;
import kr.nonos.monitor.machine.entity.Machine;
import kr.nonos.monitor.machine.entity.MachineStatusFailLog;
import kr.nonos.monitor.machine.entity.MachineStatusFront;
import kr.nonos.monitor.machine.entity.MachineStatusWatchdogLog;
import kr.nonos.monitor.machine.repository.MachineRepository;
import kr.nonos.monitor.machine.repository.MachineStatusFailLogRepository;
import kr.nonos.monitor.machine.repository.MachineStatusFrontRepository;
import kr.nonos.monitor.machine.repository.MachineStatusWatchdogLogRepository;

@Controller
public class MachineStatusController {
	@Autowired
	private MachineRepository machineRepository;

	@Autowired
	private MachineStatusFrontRepository machineStatusFrontRepository;

	@Autowired
	private MachineStatusFailLogRepository machineStatusFailLogRepository;

	@Autowired
	private MachineStatusWatchdogLogRepository machineStatusWatchdogLogRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@RequestMapping("/machineStatus/list")
	public String list(Model model, HttpServletRequest request, Authentication authentication) throws Exception {
		User user = userRepository.findByEmail(authentication.getName());

		List<MachineStatusFront> machineStatusFrontList;

		if (isAdmin(authentication)) {
			machineStatusFrontList = machineStatusFrontRepository.findAll();
		} else {
			machineStatusFrontList = machineStatusFrontRepository.findAllByUser(user.getId());
		}

		for (MachineStatusFront machineStatusFront : machineStatusFrontList) {
			Machine machine = machineRepository.findOne(machineStatusFront.getMachineId());

			machineStatusFront.setRack(machine.getRack());
			machineStatusFront.setStack(machine.getStack());
			machineStatusFront.setIp(machine.getIp());
			machineStatusFront.setMac(machine.getMac());

			machineStatusFront = parseMachineStatusFront(machineStatusFront);
		}

		model.addAttribute("machineStatusFrontList", machineStatusFrontList);

		return "machineStatus/list_machine_status";
	}

	@RequestMapping("/machineStatus/listOn")
	public String listOn(Model model, HttpServletRequest request, Authentication authentication) throws Exception {
		User user = userRepository.findByEmail(authentication.getName());

		List<MachineStatusFront> machineStatusFrontList;

		if (isAdmin(authentication)) {
			machineStatusFrontList = machineStatusFrontRepository.findAllOn();
		} else {
			machineStatusFrontList = machineStatusFrontRepository.findOnByUser(user.getId());
		}

		for (MachineStatusFront machineStatusFront : machineStatusFrontList) {
			Machine machine = machineRepository.findOne(machineStatusFront.getMachineId());

			machineStatusFront.setRack(machine.getRack());
			machineStatusFront.setStack(machine.getStack());
			machineStatusFront.setIp(machine.getIp());
			machineStatusFront.setMac(machine.getMac());

			machineStatusFront = parseMachineStatusFront(machineStatusFront);
		}

		model.addAttribute("machineStatusFrontList", machineStatusFrontList);

		return "machineStatus/list_machine_status";
	}

	@RequestMapping("/machineStatus/listOff")
	public String listOff(Model model, HttpServletRequest request, Authentication authentication) throws Exception {
		User user = userRepository.findByEmail(authentication.getName());

		List<MachineStatusFront> machineStatusFrontList;

		if (isAdmin(authentication)) {
			machineStatusFrontList = machineStatusFrontRepository.findAllOff();
		} else {
			machineStatusFrontList = machineStatusFrontRepository.findOffByUser(user.getId());
		}

		for (MachineStatusFront machineStatusFront : machineStatusFrontList) {
			Machine machine = machineRepository.findOne(machineStatusFront.getMachineId());

			machineStatusFront.setRack(machine.getRack());
			machineStatusFront.setStack(machine.getStack());
			machineStatusFront.setIp(machine.getIp());
			machineStatusFront.setMac(machine.getMac());

			machineStatusFront = parseMachineStatusFront(machineStatusFront);
		}

		model.addAttribute("machineStatusFrontList", machineStatusFrontList);

		return "machineStatus/list_machine_status";
	}

	@RequestMapping("/machineStatus/{id}/failLogList")
	public String stopLogList(@PathVariable int id, Model model) throws Exception {
		Page<MachineStatusFailLog> machineStatusFailLogPage = machineStatusFailLogRepository.findByMachineId(id, new PageRequest(0, 30, Sort.Direction.DESC, "id"));

		List<MachineStatusFailLog> machineStatusFailLogList = machineStatusFailLogPage.getContent();

		model.addAttribute("machineStatusFailLogList", machineStatusFailLogList);

		return "machineStatus/list_machine_status_fail_log";
	}

	@RequestMapping("/machineStatus/{id}/watchdogLogList")
	public String watchdogLogList(@PathVariable int id, Model model) throws Exception {
		Page<MachineStatusWatchdogLog> machineStatusWatchdogLogPage = machineStatusWatchdogLogRepository.findByMachineId(id, new PageRequest(0, 30, Sort.Direction.DESC, "id"));

		List<MachineStatusWatchdogLog> machineStatusWatchdogLogList = machineStatusWatchdogLogPage.getContent();

		model.addAttribute("machineStatusWatchdogLogList", machineStatusWatchdogLogList);

		return "machineStatus/list_machine_status_watchdog_log";
	}

	@RequestMapping("/machineStatus/{id}/powerOn")
	public String powerOn(@PathVariable int id, Authentication authentication) throws Exception {
		Machine machine = machineRepository.findOne(id);

		if(!isAdmin(authentication) && !isOwnMachine(machine.getUserId())) {
			return null;
		}

		Device device = deviceRepository.findOne(Integer.valueOf(machine.getDeviceId()));

		sendData(device.getIp(), device.getPort(), Integer.valueOf(machine.getRelayNo()));

		Thread.sleep(2000);

		sendData(device.getIp(), device.getPort(), Integer.valueOf(machine.getRelayNo()));

		return "redirect:/machineStatus/list";
	}

	@RequestMapping("/machineStatus/{id}/powerOff")
	public String powerOff(@PathVariable int id, Authentication authentication) throws Exception {
		Machine machine = machineRepository.findOne(id);

		if(!isAdmin(authentication) && !isOwnMachine(machine.getUserId())) {
			return null;
		}

		Device device = deviceRepository.findOne(Integer.valueOf(machine.getDeviceId()));

		sendData(device.getIp(), device.getPort(), Integer.valueOf(machine.getRelayNo()));

		Thread.sleep(5000);

		sendData(device.getIp(), device.getPort(), Integer.valueOf(machine.getRelayNo()));

		return "redirect:/machineStatus/list";
	}

	private MachineStatusFront parseMachineStatusFront(MachineStatusFront machineStatusFront) {
		// Ethereum Running Time
		if (machineStatusFront.getRunningTime() != null) {
			machineStatusFront.setRunningTime(String.format("%02d:%02d", Integer.valueOf(machineStatusFront.getRunningTime()) / 60, Integer.valueOf(machineStatusFront.getRunningTime()) % 60));
		}

		// Ethereum Stats
		if (machineStatusFront.getEthereumStats() != null) {
			String[] ethereumStats = machineStatusFront.getEthereumStats().split(";");

			machineStatusFront.setEthereumStats((Integer.valueOf(ethereumStats[0]) / 1000.0f) + ";" + ethereumStats[1] + ";" + ethereumStats[2]);
		}

		// Ethereum Speed
		if (machineStatusFront.getEthereumSpeed() != null) {
			String[] ethereumSpeed = machineStatusFront.getEthereumSpeed().split(";");

			String ethereumSpeedResult = "";

			for (int i = 0; i < ethereumSpeed.length; i++) {
				if (ethereumSpeed[i].matches("-?\\d+(\\.\\d+)?")) {
					ethereumSpeedResult += (Integer.valueOf(ethereumSpeed[i]) / 1000.0f) + ((i == (ethereumSpeed.length - 1)) ? "" : ";");
				} else {
					ethereumSpeedResult += ethereumSpeed[i] + ((i == (ethereumSpeed.length - 1)) ? "" : ";");
				}
			}

			machineStatusFront.setEthereumSpeed(ethereumSpeedResult);
		}

		// Decred Stats
		if (machineStatusFront.getDecredStats() != null) {
			String[] decredStats = machineStatusFront.getDecredStats().split(";");

			machineStatusFront.setDecredStats((Integer.valueOf(decredStats[0]) / 1000.0f) + ";" + decredStats[1] + ";" + decredStats[2]);
		}

		// Decred Speed
		if (machineStatusFront.getDecredSpeed() != null) {
			String[] decredSpeed = machineStatusFront.getDecredSpeed().split(";");

			String decredSpeedResult = "";

			for (int i = 0; i < decredSpeed.length; i++) {
				if (decredSpeed[i].matches("-?\\d+(\\.\\d+)?")) {
					decredSpeedResult += (Integer.valueOf(decredSpeed[i]) / 1000.0f) + ((i == (decredSpeed.length - 1)) ? "" : ";");
				} else {
					decredSpeedResult += decredSpeed[i] + ((i == (decredSpeed.length - 1)) ? "" : ";");
				}
			}

			machineStatusFront.setDecredSpeed(decredSpeedResult);
		}

		return machineStatusFront;
	}

	private void sendData(String ip, String port, int relay) throws Exception {
		Socket socket = new Socket();

		InetSocketAddress sa = new InetSocketAddress(ip, Integer.valueOf(port));

		socket.setKeepAlive(true);
		socket.setSoTimeout(5000);

		socket.connect(sa, 500);

		OutputStream os = socket.getOutputStream();

		os.write(("admin" + "\r\n").getBytes("utf-8"));

		os.write(makeCommand(relay));
		os.flush();

		os.close();

		socket.close();
	}

	private byte[] makeCommand(int relay) throws Exception {
		byte[] data = new byte[8];

		data[0] = 85;
		data[1] = -86;
		data[2] = 0;
		data[3] = 3;
		data[4] = 0;
		data[5] = 3;
		data[6] = (byte) relay;
		data[7] = (byte) (relay + 6);

		return data;
	}

	private boolean isAdmin(Authentication authentication) {
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		boolean isAdmin = false;

		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals("ADMIN")) {	
				isAdmin = true;

			}
		}

		return isAdmin;
	}


	private boolean isOwnMachine(int id) {
		User user = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

		if (id == user.getId()) {
			return true;
		}

		return false;
	}
}