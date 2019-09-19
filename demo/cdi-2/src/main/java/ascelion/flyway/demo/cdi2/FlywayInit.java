package ascelion.flyway.demo.cdi2;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import ascelion.flyway.api.FlywayMigration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;

@Dependent
public class FlywayInit {

	@Produces
	@Named("database.importFile")
	String importFile = "data.csv";

	@Produces
	@Singleton
	@FlywayMigration(name = "flyway-1", packages = "ascelion.flyway.demo.cdi2.db1")
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
