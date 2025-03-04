package spring.ecommerce.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dao.UserDao;
import spring.ecommerce.entity.UserEntity;

@Service
@Slf4j
@AllArgsConstructor
public class CommonService {

	private final UserDao userDao;
	
    /**
     * Retrieves the authenticated user from the security context.
     * 
     * @return Authenticated {@link UserEntity}.
     */
    public UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Authenticated user retrieved: {}", username);

        return this.userDao.findById(username).orElseThrow(() -> {
            log.error("User with username '{}' not found", username);
            return new RuntimeException("Authenticated user not found");
        });
    }
}
