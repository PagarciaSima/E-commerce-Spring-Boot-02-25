package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.model.User;

@Repository
public interface UserDao extends CrudRepository<User, String>{

	User findByUserName(String username);

}
