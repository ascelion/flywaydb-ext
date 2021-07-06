package ascelion.flyway.demo.cdi2;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.interceptor.Interceptor;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class Demo {

	public static void main(String[] args) {
//		stream(System.getProperty("java.class.path").split(":"))
//		.forEach(System.out::println);

		final SeContainerInitializer init = SeContainerInitializer.newInstance();

		try (SeContainer container = init.initialize()) {
			container.select(Demo.class)
					.get()
					.run();
		}
	}

	void run() {
		log.info("Running demo");
	}

	void init(
	//@formatter:off
			@Observes
			@Initialized(ApplicationScoped.class)
			@Priority(Interceptor.Priority.APPLICATION - 100)
			Object unused) {
	//@formatter:on

		log.info("Starting demo");
	}

	void done(
	//@formatter:off
			@Observes
			@Priority(Interceptor.Priority.APPLICATION - 100)
			@Destroyed(ApplicationScoped.class)
			Object unused) {
		//@formatter:on
		log.info("Finishing demo");
	}
}
