package ascelion.flyway.demo.cdi1;

import java.util.UUID;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import static java.util.UUID.randomUUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;

public class FlywayInit {

	static final String INJECTED_VALUE = "injectedValue";

	/**
	 * Producer for something to be injected in the CDI migration class.
	 */
	@Produces
	@Named(INJECTED_VALUE)
	UUID injectedValue = randomUUID();

	@Produces
	@Singleton
	Configuration configuration() {
		return Flyway.configure()
				.dataSource("jdbc:h2:mem:db1", "sa", "sa")
				.locations("db");
	}
}
