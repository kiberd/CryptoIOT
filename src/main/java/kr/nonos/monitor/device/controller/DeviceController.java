package kr.nonos.monitor.device.controller;

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

import kr.nonos.monitor.device.entity.Device;
import kr.nonos.monitor.device.repository.DeviceRepository;

@Controller
public class DeviceController {
	@Autowired
	private DeviceRepository deviceRepository;

	@RequestMapping("/device/list")
	public String list(Model model, HttpServletRequest request) throws Exception {
		List<Device> deviceList = deviceRepository.findAll();

		model.addAttribute("deviceList", deviceList);

		return "device/list_device";
	}

	@RequestMapping("/device/{id}/delete")
	public String delete(@PathVariable int id) {
		deviceRepository.delete(id);

		return "redirect:/device/list";
	}

	@RequestMapping("/device/add")
	public String add(Model model) {
		Device device = new Device();

		model.addAttribute("device", device);

		return "device/form_device";
	}

	@RequestMapping("/device/{id}/modify")
	public String modifyWithId(@PathVariable int id, Model model) {
		Device device = deviceRepository.findOne(id);

		model.addAttribute("device", device);

		return "device/form_device";
	}

	@RequestMapping(value = "/device/modify", method = RequestMethod.POST)
	public String modify(@RequestParam Map<String, String> params) {
		Device device = new Device();

		if (!StringUtils.isEmpty(params.get("id"))) {
			device.setId(Integer.valueOf(params.get("id")));
		}

		device.setIp(params.get("ip"));
		device.setMac(params.get("mac"));
		device.setPort(params.get("port"));
		device.setRack(params.get("rack"));

		deviceRepository.save(device);

		return "redirect:/device/list";
	}
}