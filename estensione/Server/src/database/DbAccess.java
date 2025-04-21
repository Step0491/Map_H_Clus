package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Gestisce l'accesso al DB per la lettura dei dati di training
 */
public class DbAccess {

    private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private final String DBMS = "jdbc:mysql";
    private final String SERVER = "localhost";
    private final String DATABASE = "MapDB";
    private final int PORT = 3306;
    private final String USER_ID = "MapUser";
    private final String PASSWORD = "map";

    private Connection conn;

    /**
     * Inizializza la connessione al database.
     *
     * @throws DatabaseConnectionException Eccezione lanciata se la connessione al database fallisce.
     */
    public void initConnection() throws DatabaseConnectionException {
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            System.out.println("[!] Driver not found: " + e.getMessage());
            throw new DatabaseConnectionException(e.toString());
        }
        String connectionString = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE
                + "?user=" + USER_ID + "&password=" + PASSWORD + "&serverTimezone=UTC";

        try {
            conn = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e.toString());
        }
    }

    /**
     * Restituisce la connessione al database.
     *
     * @return Connessione al database.
     * @throws DatabaseConnectionException Eccezione lanciata se la connessione al database fallisce.
     */
    public Connection getConnection() throws DatabaseConnectionException {
        try {
			if (conn == null || conn.isClosed()) {
			    this.initConnection();
			}
		} catch (SQLException | DatabaseConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return conn;
    }

    /**
     * Chiude la connessione al database.
     *
     * @throws SQLException Eccezione lanciata se si verifica un errore durante la chiusura della connessione.
     */
    public void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    /**
     * getter per recuperare i nomi delle tabelle presenti nel database.
     *
     * @throws SQLException Eccezione lanciata se si verifica un errore durante la chiusura della connessione.
     */
    public String[] getTableNames() throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        ArrayList<String> tableNames = new ArrayList<>();
        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }
        tables.close();
        return tableNames.toArray(new String[0]);
    }

}