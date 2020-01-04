package ascelion.flyway.demo.sb2;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

	@Bean
	UUID injectedValue() {
		return UUID.randomUUID();
	}
}


