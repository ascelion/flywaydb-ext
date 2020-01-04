package ascelion.flyway.demo.sb2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class Demo implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Demo.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Running demo");
	}

	@PostConstruct
	void init() {
		log.info("Starting demo");
	}

	@PreDestroy
	void done() {
		log.info("Finishing demo");
	}
}
