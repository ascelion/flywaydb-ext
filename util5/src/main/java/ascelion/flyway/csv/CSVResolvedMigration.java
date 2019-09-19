package ascelion.flyway.csv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.Getter;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.line.LineReader;
import org.flywaydb.core.internal.resource.LoadableResource;

final class CSVResolvedMigration implements ResolvedMigration {

	@Getter
	private final LoadableResource resource;
	@Getter
	private final MigrationVersion version;
	@Getter
	private final String table;
	@Getter
	private final String description;
	@Getter
	private final CSVMigrationExecutor executor;

	CSVResolvedMigration(LoadableResource resource, MigrationVersion version, String table, String description) {
		this.resource = resource;
		this.version = version;
		this.table = table;
		this.description = description;
		this.executor = new CSVMigrationExecutor(this);
	}

	@Override
	public String getScript() {
		return this.resource.getRelativePath();
	}

	@Override
	public Integer getChecksum() {
		return this.resource.checksum();
	}

	@Override
	public MigrationType getType() {
		return MigrationType.JDBC;
	}

	@Override
	public String getPhysicalLocation() {
		return this.resource.getAbsolutePathOnDisk();
	}

	Statement statement(Connection db) throws IOException, SQLException {
		try (final LineReader lrd = this.resource.loadAsString()) {
			final CSVParser ps = new CSVParserBuilder()
					.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
					.build();

			Line line = lrd.readLine();

			final String[] columns = ps.parseLine(line.getLine());

			if (columns == null) {
				return null;
			}

			final String sql1 = stream(columns).collect(joining(",", "INSERT INTO " + this.table + "(", ")"));
			final String sql2 = IntStream.range(0, columns.length).mapToObj(n -> "?").collect(joining(",", "VALUES(", ")"));

			final PreparedStatement statement = db.prepareStatement(sql1 + sql2);

			while ((line = lrd.readLine()) != null) {
				final String[] values = ps.parseLine(line.getLine());

				for (int k = 0; k < values.length; k++) {
					if (values[k] != null) {
						statement.setString(k + 1, values[k]);
					} else {
						statement.setNull(k + 1, Types.NULL);
					}
				}
				for (int k = values.length; k < columns.length; k++) {
					statement.setNull(k + 1, Types.NULL);
				}

				statement.addBatch();
			}

			return statement;
		}
	}
}
