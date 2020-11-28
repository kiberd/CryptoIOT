package kr.nonos.monitor.machine.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import kr.nonos.monitor.machine.entity.Machine;
import kr.nonos.monitor.machine.repository.MachineRepository;
import kr.nonos.monitor.machine.repository.MachineStatusFrontRepository;

@Controller
public class MachineController {
	@Autowired
	private MachineRepository machineRepository;

	@Autowired
	private MachineStatusFrontRepository machineStatusFrontRepository;

	@RequestMapping("/machine/list")
	public String list(Model model, HttpServletRequest request) throws Exception {
		List<Machine> machineList = machineRepository.findAll();

		model.addAttribute("machineList", machineList);

		return "machine/list_machine";
	}

	@RequestMapping("/machine/{id}/delete")
	public String delete(@PathVariable int id) {
		machineRepository.delete(id);
		machineStatusFrontRepository.delete(id);

		return "redirect:/machine/list";
	}

	@RequestMapping("/machine/add")
	public String add(Model model) {
		Machine machine = new Machine();

		model.addAttribute("machine", machine);

		return "machine/form_machine";
	}

	@RequestMapping("/machine/{id}/modify")
	public String modifyWithId(@PathVariable int id, Model model) {
		Machine machine = machineRepository.findOne(id);

		model.addAttribute("machine", machine);

		return "machine/form_machine";
	}

	@RequestMapping(value = "/machine/modify", method = RequestMethod.POST)
	public String modify(@RequestParam Map<String, String> params) {
		Machine machine = new Machine();

		if (!StringUtils.isEmpty(params.get("id"))) {
			machine.setId(Integer.valueOf(params.get("id")));
		}

		machine.setIp(params.get("ip"));
		machine.setMac(params.get("mac"));
		machine.setPort(params.get("port"));
		machine.setRack(params.get("rack"));
		machine.setStack(params.get("stack"));
		machine.setDeviceId(params.get("deviceId"));
		machine.setRelayNo(params.get("relayNo"));
		machine.setUserId(Integer.valueOf(params.get("userId")));

		machineRepository.save(machine);

		return "redirect:/machine/list";
	}
}