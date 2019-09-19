package ascelion.flyway.java;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.JavaMigration;

@RequiredArgsConstructor
public final class JavaMigrationExecutor implements MigrationExecutor {

	private final JavaMigration migration;

	@Override
	public void execute(Context context) throws SQLException {
		try {
			this.migration.migrate(new org.flywaydb.core.api.migration.Context() {

				@Override
				public Connection getConnection() {
					return context.getConnection();
				}

				@Override
				public Configuration getConfiguration() {
					return context.getConfiguration();
				}
			});
		} catch (final SQLException e) {
			throw e;
		} catch (final Exception e) {
			throw new FlywayException("Migration failed !", e);
		}
	}

	@Override
	public boolean canExecuteInTransaction() {
		return this.migration.canExecuteInTransaction();
	}

}
