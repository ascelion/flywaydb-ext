package ascelion.flyway.csv;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.Test;

public class CSVMigrationTest {

	@Test
	public void csv_imported() throws SQLException {
		final FluentConfiguration cf = Flyway.configure()
				.locations(CSVMigrationTest.class.getPackage().getName())
				.dataSource("jdbc:h2:mem:.", "sa", "sa")
				.resolvers(new CSVMigrationResolver());
		final DataSource ds = cf.getDataSource();
		final Connection db = ds.getConnection("sa", "sa");
		final Flyway fw = new Flyway(cf);
		final int count = fw.migrate();

		assertThat(count, equalTo(2));

		final ResultSet results = db.createStatement()
				.executeQuery("SELECT * FROM USERS");

		assertThat(results.next(), is(true));
		assertThat(results.next(), is(true));
		assertThat(results.next(), is(false));
	}

}
