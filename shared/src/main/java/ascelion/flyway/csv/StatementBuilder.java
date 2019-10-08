package ascelion.flyway.csv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
final class StatementBuilder {
	static private final Logger L = LoggerFactory.getLogger(StatementBuilder.class);

	private final Connection db;
	private final String table;
	private final LineProvider lp;
	private final Map<String, List<String>> references;

	private final CSVParser parser = new CSVParserBuilder()
			.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
			.build();

	public Statement createBatch(String location) throws SQLException, IOException {
		final String[] columns = this.parser.parseLine(this.lp.nextLine());

		if (columns == null) {
			throw new FlywayException("Cannot parse header: " + location);
		}

		@SuppressWarnings("unchecked")
		final List<String>[] fkeys = new List[columns.length];

		for (int k = 0; k < columns.length; k++) {
			columns[k] = columns[k].trim();

			final int pipeIx = columns[k].indexOf('|');

			if (pipeIx < 0) {
				continue;
			}

			final String ftable = columns[k].substring(pipeIx + 1);

			fkeys[k] = this.references.get(ftable);

			if (fkeys[k] == null) {
				throw new FlywayException(format("Table %s has not been imported in this session", ftable));
			}

			columns[k] = columns[k].substring(0, pipeIx);
		}

		final int[] types = determineTypes(this.db, columns);

		final String insertSQL = stream(columns)
				.map(String::trim)
				.collect(joining(", ", "INSERT INTO " + this.table + "(", ")"));
		final String valuesSQL = IntStream.range(0, columns.length)
				.mapToObj(n -> "?")
				.collect(joining(", ", "VALUES(", ")"));

		L.debug("Generated SQL\n\n{}\n{}\n", insertSQL, valuesSQL);

		final PreparedStatement statement = this.db.prepareStatement(insertSQL + valuesSQL, RETURN_GENERATED_KEYS);
		String line;

		int row = 0;

		while ((line = this.lp.nextLine()) != null) {
			final String[] values = this.parser.parseLine(line.trim());
			final List<String> logged = new ArrayList<>();

			for (int col = 0; col < columns.length; col++) {
				final String value = columnValue(row, col < columns.length ? values[col] : null, fkeys[col]);

				logged.add(value);

				if (value != null) {
					statement.setObject(col + 1, value, types[col]);
				} else {
					statement.setNull(col + 1, Types.NULL);
				}
			}

			if (L.isTraceEnabled()) {
				L.trace("INSERT: {}", logged);
			}

			statement.addBatch();

			row++;
		}

		return statement;
	}

	private String columnValue(int row, String value, List<String> fkeys) {
		if (fkeys == null) {
			return value;
		}

		return value == null ? fkeys.get(row) : fkeys.get(Integer.parseInt(value));
	}

	private int[] determineTypes(Connection db, String[] columns) throws SQLException {
		final String sql = stream(columns).collect(joining(",", "SELECT ", " FROM " + this.table + " WHERE 0 = 1"));
		final ResultSet rs = db.createStatement().executeQuery(sql);
		final ResultSetMetaData md = rs.getMetaData();
		final int[] types = new int[columns.length];

		for (int col = 0; col < columns.length; col++) {
			types[col] = md.getColumnType(col + 1);
		}

		return types;
	}
}
