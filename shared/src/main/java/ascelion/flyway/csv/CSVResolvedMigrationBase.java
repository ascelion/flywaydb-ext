package ascelion.flyway.csv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.Getter;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resource.Resource;

public abstract class CSVResolvedMigrationBase<R extends Resource> implements ResolvedMigration {

	@Getter
	private final R resource;
	@Getter
	private final MigrationVersion version;
	@Getter
	private final String table;
	@Getter
	private final String description;
	private final int checksum;
	@Getter
	private final CSVMigrationExecutor executor;

	CSVResolvedMigrationBase(R resource, MigrationVersion version, String table, String description, int checksum) {
		this.resource = resource;
		this.version = version;
		this.table = table;
		this.description = description;
		this.checksum = checksum;
		this.executor = new CSVMigrationExecutor(this);
	}

	@Override
	public final String getScript() {
		return this.resource.getRelativePath();
	}

	@Override
	public final Integer getChecksum() {
		return this.checksum;
	}

	@Override
	public final MigrationType getType() {
		return MigrationType.JDBC;
	}

	@Override
	public final String getPhysicalLocation() {
		return this.resource.getAbsolutePathOnDisk();
	}

	protected final Statement statement(Connection db) throws SQLException, IOException {
		try (LineProvider rd = openResource()) {
			final StatementBuilder sb = new StatementBuilder(db, this.table, rd);

			return sb.createBatch();
		}
	}

	protected abstract LineProvider openResource() throws IOException;
}
