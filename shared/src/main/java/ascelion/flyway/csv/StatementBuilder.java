package ascelion.flyway.csv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class StatementBuilder {
	private final Connection db;
	private final String table;
	private final LineProvider rd;
	private final CSVParser ps = new CSVParserBuilder()
			.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
			.build();

	public Statement createBatch() throws SQLException, IOException {
		final String[] columns = this.ps.parseLine(this.rd.nextLine());

		if (columns == null) {
			return null;
		}

		final int[] types = determineTypes(this.db, columns);

		final String sql1 = stream(columns)
				.map(String::trim)
				.collect(joining(",", "INSERT INTO " + this.table + "(", ")"));
		final String sql2 = IntStream.range(0, columns.length)
				.mapToObj(n -> "?")
				.collect(joining(",", "VALUES(", ")"));

		final PreparedStatement statement = this.db.prepareStatement(sql1 + sql2);
		String line;

		while ((line = this.rd.nextLine()) != null) {
			final String[] values = this.ps.parseLine(line);

			for (int col = 0; col < values.length; col++) {
				if (values[col] != null) {
					statement.setObject(col + 1, values[col].trim(), types[col]);
				} else {
					statement.setNull(col + 1, Types.NULL);
				}
			}
			for (int k = values.length; k < columns.length; k++) {
				statement.setNull(k + 1, Types.NULL);
			}

			statement.addBatch();
		}

		return statement;
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
