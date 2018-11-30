package util;

import il.ac.colman.cs.ExtractedLink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;

/**
 * Abstraction layer for database access
 */
public class DataStorage {
	private Connection conn;

	public DataStorage() throws SQLException {
		this.conn = getRemoteConnection();
	}

	public DataStorage(String database) throws SQLException {
		String url = "jdbc:sqlite:" + database;
		conn = DriverManager.getConnection(url);
	}

	private static Connection getRemoteConnection() {
	      try {
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      String dbName = System.getProperty("RDS_DB_NAME");
	      String userName = System.getProperty("RDS_USERNAME"); 
	      String password = System.getProperty("RDS_PASSWORD");
	      String hostname = System.getProperty("RDS_HOSTNAME");
	      String port = System.getProperty("RDS_PORT");
	      String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/"+dbName+"?user=" + userName + "&password=" + password;	
	      Connection con = DriverManager.getConnection(jdbcUrl);
	      createTable(con);
	      return con;
	    }
	    catch (SQLException e) 
	      { System.out.println(e.getMessage());}
	    catch (ClassNotFoundException e)
	      {e.printStackTrace(); System.out.println(e.getMessage());}
		return null;
	}
	
	private static void createTable(Connection con) throws SQLException {
		if(con != null)
		{			
			Statement stmt = con.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS tweets (" 
			+ "id int NOT NULL AUTO_INCREMENT," 
			+ "link TEXT,"
			+ "track TEXT," 
			+ "content TEXT," 
			+ "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
			+ " PRIMARY KEY (id));";
			stmt.execute(sql);
		}
	}
	
	/**
	 * Add link to the database
	 */
	public void addLink(ExtractedLink link, String track) {
		/*
		 * This is where we'll add our link
		 */
		IsDBFullThenDelete();
		try {
			Statement stmt = conn.createStatement();
			String sqlQuery ="INSERT INTO tweets (link,track,content,description,screenshot,title)"
					+ " values ("+"'"+link.getUrl()+"'"+","+"'"+track+"'"+","+"'"+link.getContent()+"'"+","+"'"+link.getDescription()+"'"+","+"'"+link.getScreenshotURL()+"'"+","+"'"+link.getTitle()+"'"+")";
			stmt.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search for a link
	 * 
	 * @param query
	 *            The query to search
	 */
	public List<ExtractedLink> search(String query) {
		/*
		 * Search for query in the database and return the results
		 */
		ResultSet rs;
		List<ExtractedLink> list = new ArrayList<ExtractedLink>();
		try {
			Statement stmt = conn.createStatement();
			if (query != null) {
				rs = stmt.executeQuery("SELECT * FROM tweets where track =" + "'" + query + "'");
			} else {
				rs = stmt.executeQuery("SELECT * FROM tweets");
			}
			while (rs.next()) {
				ExtractedLink el = new ExtractedLink(rs.getString("link"), rs.getString("content"),
						rs.getString("title"), rs.getString("description"), rs.getString("screenshot"));
				list.add(el);
			}
			return list;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return null;
	}

	/*
	 * Check if the database contain more than 1000 rows and delete the oldest row and the screenshot from s3
	 * return true if contains more the 1000 rows
	 */
	private void IsDBFullThenDelete() {
		int numberRow = 0;
		ResultSet rs;
		try {
			Statement stmt = conn.createStatement();
			String sqlQuery = "SELECT count(*) FROM tweets";
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				numberRow = rs.getInt("count(*)");
			}
			if (numberRow > 1000) {
				String res = "";
				sqlQuery = "SELECT * FROM tweets ORDER BY id LIMIT 1";
				rs = stmt.executeQuery(sqlQuery);
				while (rs.next()) {
					 res = rs.getString("screenshot");	
					}
				
				String[] screenshotKey = res.split("https://s3-eu-west-1.amazonaws.com/screenshotsrg/");
				AmazonS3 s3 = AmazonServices.getS3();
				s3.deleteObject("screenshotsrg",screenshotKey[1]);
				sqlQuery = "DELETE FROM tweets ORDER BY id LIMIT 1";
				stmt.executeUpdate(sqlQuery);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
