package spring.ecommerce.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.AllArgsConstructor;
import static spring.ecommerce.constants.ConstantsEcommerce.*;
import spring.ecommerce.jwt.JwtAuthorizationFilter;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfiguration implements WebMvcConfigurer{
	
	@Lazy
	private JwtAuthorizationFilter jwtAuthorizationFilter;
	
	@Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
        		.cors(
    				cors -> cors.configurationSource(
    					request -> {
    						CorsConfiguration configuration = new CorsConfiguration();
    						configuration.setAllowedOrigins(Arrays.asList("*"));
    						configuration.setAllowedMethods(Arrays.asList("*"));
    						configuration.setAllowedHeaders(Arrays.asList("*"));
    						return configuration;
    					}
					)
				)
        		.csrf(csrf -> csrf.disable())
        	    .authorizeHttpRequests(
        	    		auth -> 
                        auth
                            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/actuator/**").permitAll()
                            .requestMatchers("/images/**").permitAll()
                            .requestMatchers("/api/v1/user/register").permitAll()
                            .requestMatchers("/api/v1/authenticate").permitAll()
                            .requestMatchers("/api/v1/images/**").hasRole(ADMIN_ROLE)
                            
                            .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/product/**").permitAll()
                            .requestMatchers("/api/v1/products/**").hasRole(ADMIN_ROLE) 
                            .requestMatchers("/api/v1/product").hasRole(ADMIN_ROLE)
                            // 🔹 Especificamos primero la restricción para el endpoint de AdminRole
                            .requestMatchers("/api/v1/order/getAllOrderDetailsPaginated/**").hasRole(ADMIN_ROLE)
                            .requestMatchers("/api/v1/order/markOrderAsDelivered/**").hasRole(ADMIN_ROLE)

                            // 🔹 Luego, permitimos que UserRole acceda a los demás endpoints de order
                            .requestMatchers("/api/v1/order/**").hasRole(USER_ROLE)                            
                            .requestMatchers("/api/v1/cart/**").hasRole(USER_ROLE)
                            .anyRequest().authenticated()
                         /*auth -> 
                        auth.anyRequest().permitAll() */
        	    		
	    		)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); 
        		httpSecurity.addFilterBefore(this.jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        
        return httpSecurity.build();
    }
	
	@Bean
	PasswordEncoder passwordEncoder () {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
