package spring.ecommerce.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import spring.ecommerce.entity.UserEntity;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService{
	
	private final UserService userService;

	/**
	 * Loads a user by their username and converts their roles to Spring Security authorities.
	 *
	 * @param userName The username of the user to be retrieved.
	 * @return A {@link UserDetails} object containing user information and authorities.
	 * @throws UsernameNotFoundException If the user is not found in the system.
	 */
	@Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserEntity user = userService.findByUserName(userName);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + userName);
        }

        // Convert roles to GrantedAuthority
        Set<GrantedAuthority> authorities = user.getRole().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUserName())
                .password(user.getUserPassword())
                .authorities(authorities) 
                .build();
    }

}
