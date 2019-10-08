package ascelion.flyway.csv;

import java.io.IOException;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;

@RequiredArgsConstructor
public final class CSVMigrationExecutor implements MigrationExecutor {
	private final CSVResolvedMigrationBase<?> migration;

	@Override
	public void execute(Context context) throws SQLException {
		try {
			this.migration.executBatch(context.getConnection());
		} catch (final IOException e) {
			throw new FlywayException(this.migration.getPhysicalLocation(), e);
		}
	}

	@Override
	public boolean canExecuteInTransaction() {
		return true;
	}
}
