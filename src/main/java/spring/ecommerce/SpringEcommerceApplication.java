package spring.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SpringEcommerceApplication {

	public static void main(String[] args) {
		Dotenv.configure().load();
		SpringApplication.run(SpringEcommerceApplication.class, args);
	}

}
