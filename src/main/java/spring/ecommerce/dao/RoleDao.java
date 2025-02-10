package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.Role;

@Repository
public interface RoleDao extends CrudRepository<Role, String>{

}
