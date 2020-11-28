package kr.nonos.monitor.login.service.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import kr.nonos.monitor.login.entity.Role;
import kr.nonos.monitor.login.entity.User;
import kr.nonos.monitor.login.repository.RoleRepository;
import kr.nonos.monitor.login.repository.UserRepository;
import kr.nonos.monitor.login.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User findUserByUserId(int id) {
		return userRepository.findOne(id);
	}

	@Override
	public void saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setActive(1);

		Role role = roleRepository.findByRole("USER");

		user.setRoles(new HashSet<Role>(Arrays.asList(role)));

		userRepository.save(user);
	}

	@Override
	public void deleteUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setActive(1);

		Role role = roleRepository.findByRole("");

		user.setRoles(new HashSet<Role>(Arrays.asList(role)));

		userRepository.delete(user);
	}
}