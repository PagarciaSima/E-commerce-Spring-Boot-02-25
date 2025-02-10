package spring.ecommerce.service;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import spring.ecommerce.dao.RoleDao;
import spring.ecommerce.entity.Role;

@Service
@AllArgsConstructor
public class RoleService {

	private final RoleDao roleDao;
	
	
	public Role createNewRole(Role role) {
		return roleDao.save(role);
	}
}
