package doug.git;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Dc_DbHelper {

	private String databaseName;

	private static final String TABLE_ANDROID = "android_metadata";

	private static final String CAT_KEY_LOCALE = "locale";
	private static final String LOCALE_DEFAULT = "en_US";

	private static final String TABLE_DC = "dc";

	private static String driverClass = "org.sqlite.JDBC";
	private static String connStrBase = "jdbc:sqlite:";
	private String connStr;
	private Connection con = null;

	public static final String CAT_KEY_ID = "_id";
	public static final String CAT_KEY_ACTIVE = "active";
	public static final String CAT_KEY_DATETIME = "datetime";
	public static final String CAT_KEY_DEVICE = "device";
	public static final String CAT_KEY_COMMENT = "comment";

	private boolean fakeit;

	private ArrayList<DcRec> dc;

	public Dc_DbHelper(String databaseName, boolean fake) {

		// Prepare for a JDBC connection
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		this.databaseName = databaseName;
		File dbFile = new File(databaseName);
		connStr = new String(connStrBase + dbFile.getAbsolutePath());
	}

	// Base methods for use by working methods

	/**
	 * Get a SQLite Database connection for use in further database operations.
	 * The connection is opened, the operation performed and then the connection
	 * is closed. Note that if the database file does not exist, it is created.
	 * 
	 * @return SQLite Database Connection
	 * @throws SQLException if the connection fails.
	 */
	public Connection getConnection() throws SQLException {
		// Prepare for a JDBC connection
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return con = DriverManager.getConnection(connStr);
	}

	/**
	 * The method is called at the start of a database transaction. In this
	 * method AutoCommit is turned off so that failed transactions are not
	 * automatically committed to the database.
	 * 
	 * @throws SQLException internally if this fails.
	 */
	private void beginTransaction() {
		if (con != null) {
			try {
				con.setAutoCommit(false);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Method used to commit the changes to the database. Throws internal
	 * exception if SQLException occurs.
	 */
	private void commit() {
		if (con != null) {
			try {
				con.commit();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Method that is called if the transaction fails. This rolls back any
	 * changes that may have occurred during the transaction processing. Throws
	 * internal exception if SQLException occurs.
	 */
	public void rollback() {
		if (con != null) {
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Method used to close the database and any intermediate objects that may
	 * have been created (result sets, statements, and the connection). At the
	 * end of this method the database is closed. If further transactions are
	 * desired, re-open the database connection. Any close action could result
	 * in a SQLException that is handed here with print stack trace.
	 * 
	 * @param rs	Any open result set (if it is not null).
	 * @param stmt	Any open statement object (if it is not null).
	 * @param con	The open connection (if it is not null).
	 */
	private void closeDB(ResultSet rs, Statement stmt, Connection con) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		try {
			if (con != null) {
				con.close();
				con = null;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	public int createAndroidMetadataTable() throws SQLException {

		Connection c = null;

		if (fakeit) {
			return 0;
		}
		
		int retn = 0;

		String sql = "CREATE TABLE " + TABLE_ANDROID + "(" + CAT_KEY_LOCALE
				+ " TEXT);";
		
		con = getConnection();
		Statement stmt = con.createStatement();
		
		retn = stmt.executeUpdate(sql);
		closeDB(null, stmt, con);

		return retn;
	}

	public int reCreateAndroidMetadataTable() throws SQLException {
		
		int retn = 0;
		
		if (fakeit) {
			return 0;
		}
		
		// Drop table if exists so that a new table can be created
		String sql = "DROP TABLE IF EXISTS " + TABLE_ANDROID + ";";
		
		con = getConnection();
		Statement stmt = con.createStatement();
		
		if (retn == 0) {
			createAndroidMetadataTable();
		}
		return retn;

	}

	public int createDcRecTable() throws SQLException {

		int retn = 0;

		String sql = "CREATE TABLE " + TABLE_DC + "(" + 
				CAT_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				CAT_KEY_ACTIVE + " TEXT, " + 
				CAT_KEY_DATETIME + " TEXT, " + 
				CAT_KEY_DEVICE + " TEXT, " +
				CAT_KEY_COMMENT + " TEXT" + 
				");";

		con = getConnection();
		Statement stmt = con.createStatement();
		
		retn = stmt.executeUpdate(sql);
		if (retn == 0) {
			System.out.println("createCategoryRecTable was successful");
		}
		closeDB(null, stmt, con);

		return retn;
	}

	public int reCreateCategoryRecTable() throws SQLException {
		
		int retn = 0;
		// Drop table if exists so that a new table can be created
		String sql = "DROP TABLE IF EXISTS " + TABLE_DC + ";";

		con = getConnection();
		Statement stmt = con.createStatement();
		
		retn = stmt.executeUpdate(sql);
		if (retn == 0) {
			createDcRecTable();
		}
		return 0;
	}

	/**
	 * All CRUD (Create -- Add, Read, Update, Delete) Operations For Accounts
	 * Table.
	 * 
	 * @throws SQLException
	 */

	// Add default android_metadata locale record
	public int addDefaultAndroidMetadataLocaleRec() throws Exception {

		// The account record id (_id) is an auto generated field
		// The record id is not included in the record creation

		if (fakeit) {
			return 0;
		}

		String sql = "INSERT INTO " + 
				TABLE_ANDROID + "( " + 
				CAT_KEY_LOCALE + 
				" ) VALUES (?);";

		con = getConnection();
		PreparedStatement pstmt = con.prepareStatement(sql);

		pstmt.setString(1, LOCALE_DEFAULT);

		beginTransaction();
		int result = pstmt.executeUpdate();
		if (result == 1) {
			commit();
		} else {
			rollback();
		}
		closeDB(null, pstmt, con);
		return result;

	}

	// Adding new account record
	public int addDcRec(DcRec dr) throws Exception {

		// The account record id (_id) is an auto generated field
		// The record id is not included in the record creation

		String sql = "INSERT INTO " + 
				TABLE_DC + "(" + 
				CAT_KEY_ACTIVE + ", " + 
				CAT_KEY_DATETIME + ", " + 
				CAT_KEY_DEVICE + ", " + 
				CAT_KEY_COMMENT + 
				" ) VALUES (?, ?, ?, ?);";

		con = getConnection();
		PreparedStatement pstmt = con.prepareStatement(sql);

		pstmt.setString(1, dr.getActive());
		pstmt.setString(2, dr.getDatetime());
		pstmt.setString(3, dr.getDevice());
		pstmt.setString(4, dr.getComment());

		beginTransaction();
		int result = pstmt.executeUpdate();
		if (result != 0) {
			commit();
		} else {
			rollback();
		}
		closeDB(null, pstmt, con);
		return result;
	}

	// Getting single Dc Rec
	public DcRec getDcRec(int id) throws Exception {

		String sql = "SELECT * FROM " + TABLE_DC + " WHERE _id = ?;";

		con = getConnection();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, id);

		ResultSet rs = pstmt.executeQuery(sql);
		beginTransaction();
		DcRec dr = null;

		if (rs.next()) {
			dr = new DcRec();
			dr.set_id(id);
			dr.setActive(rs.getString(1));
			dr.setDatetime(rs.getString(2));
			dr.setDevice(rs.getString(3));
			dr.setComment(rs.getString(4));
			commit();
		} else {
			rollback();
		}
		closeDB(rs, pstmt, con);
		return dr;
	}

	// Getting All account records to ArrayList
	public ArrayList<DcRec> getAllDcList() throws Exception {

		DcRec dr;
		ArrayList<DcRec> dcList = new ArrayList<DcRec>();

		String sql = "SELECT * FROM " + TABLE_DC+ ";";

		con = getConnection();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		beginTransaction();

		// looping through all rows and adding to list
		if (rs.next()) {
			do {
				dr = new DcRec();
				dr.set_id(rs.getInt(1));
				dr.setActive(rs.getString(2));
				dr.setDatetime(rs.getString(3));
				dr.setDevice(rs.getString(4));
				dr.setComment(rs.getString(5));
				dcList.add(dr);

			} while (rs.next());
		} else {
			rollback();
		}
		closeDB(rs, stmt, con);
		return dcList;
	}

	// Updating single Transaction
	public int updateDcRec(DcRec dr) throws Exception {

		String sql = "UPDATE " + TABLE_DC + " SET " + 
				CAT_KEY_ACTIVE + "=?, " + 
				CAT_KEY_DATETIME + "=?, " + 
				CAT_KEY_DEVICE + "=?, " + 
				CAT_KEY_COMMENT + "=? " + 
				"WHERE " + CAT_KEY_ID + " =?;";

		con = getConnection();

		PreparedStatement pstmt = con.prepareStatement(sql);

		pstmt.setString(1, dr.getActive());
		pstmt.setString(2, dr.getDatetime());
		pstmt.setString(3, dr.getDevice());
		pstmt.setString(4, dr.getComment());

		pstmt.setInt(5, dr.get_id());

		beginTransaction();
		int result = pstmt.executeUpdate();
		if (result != 0) {
			commit();
		} else {
			rollback();
		}
		closeDB(null, pstmt, con);
		return result;
	}

	// Deleting single transaction
	public int deleteCategoryRec(int categoryId) throws Exception {

		String sql = "DELETE FROM " + TABLE_DC+ " WHERE " + CAT_KEY_ID + "=?;";
		con = getConnection();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, categoryId);
		beginTransaction();
		int res = pstmt.executeUpdate();
		if (res != 0) {
			commit();
		} else {
			rollback();
		}
		closeDB(null, pstmt, con);
		return res;
	}

	// Getting Dc Rec Count
	public int getDcRecCount() throws Exception {

		String sql = "SELECT COUNT(*) FROM " + TABLE_DC + ";";

		con = getConnection();
		PreparedStatement pstmt = con.prepareStatement(sql);

		ResultSet rs = pstmt.executeQuery(sql);
		rs.next();
		int thisCount = rs.getInt(1);
		closeDB(rs, pstmt, con);

		// return count
		return thisCount;
	}
}
