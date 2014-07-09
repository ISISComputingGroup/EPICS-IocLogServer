package org.isis.logserver.rdb;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;

/** Connection to RDB
 *
 *  TODO Maybe use context.xml to register data sources and use its connection pooling?
 *
 *  @author Kay Kasemir
 */
public class RDB
{
	/** RDB user for read-only access */
	final public static String DEFAULT_USER = "sns_reports";

	/** RDB password for read-only access */
	final public static String DEFAULT_PASSWORD = "sns";
	
	/** Check if this is running on ics-srv-epics1.ics.sns.gov
	 *  where Tomcat is under /usr/local/apache-tomcat-7.0.12 
	 */
    final private static String install_location = System.getProperty("catalina.home");
	private static boolean use_ics;
    static
    {
        // Uses LDAP on
        // * ics-web for /usr/local/java/reports-tomcat-7.0.12
        // * on Kay's development Mac
        use_ics = install_location != null &&
                   (install_location.startsWith("/usr/local/apache-tomcat-7.0.12 ")
                   );
    }

	/** Construct URL for SNS Oracle cluster, load-balanced read access
	 *  @param user
	 *  @param password
     *  @param schema Schema prefix for table names (including ".")
	 *  @return RDB
     *  @throws Exception on error
	 */
    final public static RDB connectToCluster(final String user, final String password,
            final String schema) throws Exception
    {
        final String ics_url =
        	"jdbc:oracle:thin:" + user + "/" + password + "@" +
                   "(DESCRIPTION=(ADDRESS_LIST=(LOAD_BALANCE=OFF)" +
           		   "(ADDRESS=(PROTOCOL=TCP)" +
                   "(HOST=172.31.75.138)(PORT=1521))" +
                   "(ADDRESS=(PROTOCOL=TCP)" +
                   "(HOST=172.31.75.141)(PORT=1521)))" +
                   "(CONNECT_DATA=(SERVICE_NAME=ics_prod_lba)))";
        /*final String office_url = "jdbc:oracle:thin:" + user + "/" + password + "@" +
        "(DESCRIPTION=(SOURCE_ROUTE=YES)" +
          "(ADDRESS_LIST=(LOAD_BALANCE=OFF)(FAILOVER=ON)" +
            "(ADDRESS=(PROTOCOL=TCP)(HOST=snsapp1a.sns.ornl.gov)(PORT=1610))" +
            "(ADDRESS=(PROTOCOL=TCP)(HOST=snsapp1b.sns.ornl.gov)(PORT=1610))" +
          ")" +
          "(ADDRESS_LIST=(LOAD_BALANCE=OFF)" +
            "(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.75.138)(PORT=1521))" +
            "(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.75.141)(PORT=1521))" +
            "(ADDRESS=(PROTOCOL=TCP)(HOST=172.31.73.93 )(PORT=1521))" +
          ")" +
          "(CONNECT_DATA=(SERVICE_NAME=ics_prod_lba))" +
        ")";*/
        
        final String office_url = "jdbc:mysql://localhost:3306/msg_log";
        
        
        
        if (use_ics)
        	return connect(ics_url, schema);
        else
        	return connect(office_url, schema); 
    }
    
    /** Construct  access to Devl
	 *  @param user
	 *  @param password
     *  @param schema Schema prefix for table names (including ".")
	 *  @return RDB
     *  @throws Exception on error
	 */
    final public static RDB connectToDevl(final String user, final String password,
            final String schema) throws Exception
    {
        final String url = "jdbc:oracle:thin:" + user + "/" + password + "@" +
                   "(DESCRIPTION=(ADDRESS_LIST=(LOAD_BALANCE=OFF)" +
           		   "(ADDRESS=(PROTOCOL=TCP)" +
                   "(HOST=snsdev3.sns.ornl.gov)(PORT=1521))" +
                   "(ADDRESS=(PROTOCOL=TCP)" +
                   "(HOST=snsdev3.sns.ornl.gov)(PORT=1521)))" +
                   "(CONNECT_DATA=(SERVICE_NAME=devl)))";
        return connect(url, schema);
    }

    /** Construct URL for SNS Oracle cluster, load-balanced read access
     *  @param user
     *  @param password
     *  @param schema Schema prefix for table names (including ".")
     *  @return RDB
     *  @throws Exception on error
     */
    final public static RDB connect(final String url, final String schema) throws Exception
    {
        return new RDB(url, schema);
    }

	/** <code>true</code> if this RDB is Oracle (else: MySQL) */
    final public boolean is_oracle;

    /** Table prefix for schema. May be "", or something like "epics." */
	final public String schema;

	/** RDB connection */
	final private Connection connection;

	/** Initialize
	 *  @param url RDB URL
	 *  @param schema Schema prefix for table names (including ".")
	 *  @return Connection to RDB
	 *  @throws Exception on error
	 */
	private RDB(final String url, final String schema) throws Exception
	{
	    // Identify Database by URL
	    is_oracle = url.toLowerCase().contains("oracle");

	    if (is_oracle)
	    {
	        // Get class loader to find the driver
	        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
	        // Connect such that Java float and double map to Oracle
	        // BINARY_FLOAT resp. BINARY_DOUBLE
	        final Properties info = new Properties();
	        info.put("SetFloatAndDoubleUseBinary", "true");
	        connection = DriverManager.getConnection(url, info);
	    }
	    else
	    {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        //connection = DriverManager.getConnection(url);
	        
	        // krw - connect to mysql database
	        connection = DriverManager.getConnection(url, "admin", "admin");
	    }
	    this.schema = schema;
	}

	/** Turn <code>null</code> strings into something readable.
     *  @param text Text or <code>null</code>
     *  @return Original text or ""
     */
    public static String nullString(final String text)
    {
        if (text == null)
            return "";
        return text;
    }

    /** @return JDBC connection */
	public Connection getConnection()
	{
	    return connection;
	}

	/** Must be called when RDB no longer used to release resources */
	public void close()
	{
	    try
	    {
	        connection.close();
	    }
	    catch (Exception e)
	    {
	        // Ignore, we're closing anyway
        }
	}

	/** Unload JDBC drivers.
     *
     *  When accessing JDBC, the Oracle or MySQL driver is loaded via
     *  <code>Class.forName()</code>, but it's never unloaded, which
     *  results in Tomcat warnings
     *  "A web application registered the JBDC driver ... but failed to unregister ..."
     *
     *  This method, meant to be called from the {@link ContextListener},
     *  attempts to unload all JDBC drivers.
     */
    public static void unloadJDBCDrivers()
    {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            final Driver driver = drivers.nextElement();
            System.out.println("Unloading " + driver.getClass().getName() + " " +
                    driver.getMajorVersion() + "." + driver.getMinorVersion());
            try
            {
                DriverManager.deregisterDriver(driver);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (DriverManager.getDrivers().hasMoreElements())
            System.out.println("Unload of JDBC drivers failed");
    }
}
