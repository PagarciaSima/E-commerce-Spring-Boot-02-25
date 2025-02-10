package spring.ecommerce.service;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import spring.ecommerce.dao.UserDao;
import spring.ecommerce.entity.User;

@Service
@AllArgsConstructor
public class UserService {
	
	private final UserDao userDao;
	
	
	public User createNewUser(User user) {
		return userDao.save(user);
	}
}
