package ascelion.flyway.demo.cdi2;

import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import ascelion.flyway.api.FlywayMigration;

import static java.util.UUID.randomUUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;

@Dependent
public class FlywayInit {

	static public final String INJECTED_VALUE = "injectedValue";

	/**
	 * Producer for something to be injected in the CDI migration class.
	 */
	@Produces
	@Named(INJECTED_VALUE)
	UUID injectedValue = randomUUID();

	@Produces
	@Singleton
	@FlywayMigration(name = "flyway-1", packages = "ascelion.flyway.demo.cdi2.db1", dependsOn = "flyway-2")
	Configuration configuration1() {
		return Flyway.configure()
				.dataSource("jdbc:h2:mem:db1", "sa", "sa")
				.locations("db-1");
	}

	@Produces
	@Singleton
	@FlywayMigration(name = "flyway-2", packages = "ascelion.flyway.demo.cdi2.db2")
	Configuration configuration2() {
		return Flyway.configure()
				.dataSource("jdbc:h2:mem:db2", "sa", "sa")
				.locations("db-2");
	}
}
