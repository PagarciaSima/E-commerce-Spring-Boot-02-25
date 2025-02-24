package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.RoleEntity;

@Repository
public interface RoleDao extends CrudRepository<RoleEntity, String>{

	RoleEntity findByRoleName(String roleName);

}
