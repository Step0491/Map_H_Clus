package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import data.Example;

public class TableData {
	private DbAccess db;

	public TableData(DbAccess db) {
		this.db = db;
	}

	public List<Example> getDistinctTransazioni(String table)
			throws SQLException, EmptySetException, MissingNumberException, DatabaseConnectionException {
		List<Example> examples = new ArrayList<>();
		String query = "SELECT DISTINCT * FROM " + table;

		try (Statement stmt = db.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(query)) {

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			if (!rs.isBeforeFirst()) { // Verifica se il ResultSet è vuoto
				throw new EmptySetException("La tabella è vuota.");
			}

			while (rs.next()) {
				Example example = new Example();
				for (int i = 1; i <= columnCount; i++) {
					Object value = rs.getObject(i);
					if (!(value instanceof Number)) { // Verifica se l'attributo non è numerico
						throw new MissingNumberException("Attributo non numerico presente nella tabella.");
					}
					example.add(((Number) value).doubleValue());
				}
				examples.add(example);
			}
		}

		return examples;
	}
}
