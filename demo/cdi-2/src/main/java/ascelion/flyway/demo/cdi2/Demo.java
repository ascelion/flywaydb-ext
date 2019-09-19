package ascelion.flyway.demo.cdi2;

import java.io.IOException;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Demo {
	static private final Logger L = LoggerFactory.getLogger(Demo.class);

	public static void main(String[] args) throws IOException {
		final SeContainerInitializer init = SeContainerInitializer.newInstance();

		try (SeContainer container = init.initialize()) {
		}
	}

	void init(@Observes @Priority(Interceptor.Priority.APPLICATION - 100) @Initialized(ApplicationScoped.class) Object unused) throws Exception {
		L.info("Starting demo");
	}

	void done(@Observes @Priority(Interceptor.Priority.APPLICATION - 100) @Destroyed(ApplicationScoped.class) Object unused) throws Exception {
		L.info("Finishing demo");
	}
}
