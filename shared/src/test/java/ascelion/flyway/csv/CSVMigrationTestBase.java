package ascelion.flyway.csv;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.junit.Test;

@RequiredArgsConstructor
public abstract class CSVMigrationTestBase {
	private final MigrationResolver resolver;

	@Test
	public void csv_imported() throws SQLException {
		final FluentConfiguration cf = Flyway.configure()
				.locations(getClass().getPackage().getName())
				.dataSource("jdbc:h2:mem:.", "sa", "sa")
				.resolvers(this.resolver);
		final DataSource ds = cf.getDataSource();
		final Connection db = ds.getConnection("sa", "sa");
		final Flyway fw = new Flyway(cf);
		final int count = fw.migrate();

		assertThat(count, equalTo(4));

		final ResultSet users = db.createStatement()
				.executeQuery("SELECT * FROM AUTHZ_USERS");

		assertCount(users, 3);

		final ResultSet roles = db.createStatement()
				.executeQuery("SELECT * FROM AUTHZ_ROLES");

		assertCount(roles, 3);

		final ResultSet users_roles = db.createStatement()
				.executeQuery("SELECT * FROM AUTHZ_USERS_ROLES");

		assertCount(users_roles, 4);
	}

	static void assertCount(ResultSet results, int count) throws SQLException {
		assertThat(results.first(), is(true));

		for (int next = 0; next < count - 1; next++) {
			assertThat("ResultSet::next failed at " + next, results.next(), is(true));
		}

		assertThat(results.next(), is(false));
	}

}
