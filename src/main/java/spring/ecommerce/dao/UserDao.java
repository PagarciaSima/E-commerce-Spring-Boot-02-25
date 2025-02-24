package spring.ecommerce.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import spring.ecommerce.entity.UserEntity;

@Repository
public interface UserDao extends CrudRepository<UserEntity, String>{

	UserEntity findByUserName(String username);

}
