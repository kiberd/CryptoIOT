package kr.nonos.monitor.login.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import kr.nonos.monitor.login.entity.User;
import kr.nonos.monitor.login.repository.UserRepository;
import kr.nonos.monitor.login.service.UserService;

@Controller
public class LoginController {
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
	public String login() {
		return "login/login";
	}

	@RequestMapping("/admin/listUser")
	public String list(Model model, HttpServletRequest request) throws Exception {
		List<User> userList = userRepository.findAll();

		model.addAttribute("userList", userList);

		return "admin/list_user";
	}

	@RequestMapping(value = "/admin/userRegistration", method = RequestMethod.GET)
	public String registration(Model model) {
		User user = new User();

		model.addAttribute("user", user);

		return "admin/form_registration";
	}

	@RequestMapping(value = "/admin/userRegistration", method = RequestMethod.POST)
	public String registeration(@Valid User user, BindingResult bindingResult, Model model) {
		User userExists = userService.findUserByEmail(user.getEmail());

		if (user.getId() == 0 && userExists != null) {
			bindingResult.rejectValue("email", "error.user", "이미 등록된 사용자입니다.");
		}

		if (!bindingResult.hasErrors()) {
			userService.saveUser(user);
		}

		return "redirect:/admin/listUser";
	}

	@RequestMapping("/admin/{id}/deleteUser")
	public String delete(@PathVariable int id, Model model) {
		User userExists = userService.findUserByUserId(id);

		userService.deleteUser(userExists);

		return "redirect:/admin/listUser";
	}

	@RequestMapping("/admin/{id}/modifyUser")
	public String modify(@PathVariable int id, Model model) {
		User user = userRepository.findOne(id);

		model.addAttribute("user", user);

		return "admin/form_registration";
	}

	@RequestMapping(value = "/user/userPassword", method = RequestMethod.GET)
	public String userPassword() {
		return "user/form_password";
	}

	@RequestMapping(value = "/user/userPassword", method = RequestMethod.POST)
	public String userPassword(@RequestParam Map<String, String> params, Authentication authentication) {
		User user = userService.findUserByEmail(authentication.getName());

		user.setPassword(params.get("password"));

		if (user != null) {
			userService.saveUser(user);
		}

		return "redirect:/user/userPassword";
	}
}