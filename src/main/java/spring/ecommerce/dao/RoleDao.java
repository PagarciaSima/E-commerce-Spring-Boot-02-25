package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.model.Role;

@Repository
public interface RoleDao extends CrudRepository<Role, String>{

	Role findByRoleName(String roleName);

}
