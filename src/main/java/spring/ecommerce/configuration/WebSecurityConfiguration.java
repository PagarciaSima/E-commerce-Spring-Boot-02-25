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
                            .requestMatchers("/api/v1/images/**").hasRole("AdminRole")
                            
                            .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/product/**").permitAll()
                            .requestMatchers("/api/v1/products/**").hasRole("AdminRole") 
                            .requestMatchers("/api/v1/product").hasRole("AdminRole")
                            .requestMatchers("/api/v1/placeOrder").hasRole("UserRole")
                            .requestMatchers("/api/v1/addToCart/**").hasRole("UserRole")

                            .anyRequest().authenticated()
                         /*auth -> 
                        auth.anyRequest().permitAll() */
        	    		
	    		)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); 
        		httpSecurity.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        
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
