package doug.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class holds the configuration data for all classes. This configuration
 * gets updated by user actions (i.e. set startup action). Since this is a place
 * for all configuration for ALL common data and methods it is implemented as a
 * singleton.
 * <p>
 * To use this class call it during main initialization:<br>
 * Common common = Common.getInstance();
 * <p>
 * Generally global is instantiated at startup.
 * 
 * @author Douglas Barrett
 *
 */

public class Common {

	private static Common common = new Common();

	// Diagnostic output level
	// 1 = Critical debug output
	// 2 = Severe debug output
	// 3 = Warning debug output
	// 4 = Information debug output
	// 5 = All debug output

	static int diagnosticLevelOutput = 5;

	static int defaultdiagnosticLevelOutput = 5;
	
	static String version = "1.1";
	// Version 1.0		20150305
	//					Base version
	//
	// Version 1.1		20150308
	//					Modified MergeRecords to accommodate
	//					and empty client database
	//

	static final String masterDbPath = "database/dc.db";

	static final String logLeader = "DC Server ";
	static final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
	static final SimpleDateFormat sdfTimeLog = new SimpleDateFormat(
			"HHmmss.SSS");
	static final SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");

	static final String filesFolder = "files/";
	static final String defaultCurrentClientDatabaseFilePath = new String(
			filesFolder + "CurrentClientDatabaseFile.db");
	
	static final String logFilePath = new String(filesFolder + "DCServer.log");
	
	static final String clientFilePrefix = "Client";
	static final String clientFileSerialNumFormat = "%06d";
	
	static final String configFileName = "files/config.properties";
	
	static String currentClient;
	static String masterClient;

	/*
	 * A private Constructor prevents any other class from instantiating.
	 */
	private Common() {
		Common.diagnosticLevelOutput = defaultdiagnosticLevelOutput;
	}

	/* Static 'instance' method */
	public static Common getInstance() {
		return common;
	}

	public static void logit(String inLogLine, String logFileStr) {
		Date d = new Date();
		String logDate = new String(Common.sdfDate.format(d) + "T"
				+ Common.sdfTimeLog.format(d));

		if (inLogLine != null) {
			String logOutLine = new String(logDate + " " + Common.logLeader
					+ inLogLine);
			System.out.println(logOutLine);
		}

		if (logFileStr != null) {
			logToFile(logDate, logFileStr);
		}
	}

	private static void logToFile(String inDateStr, String inStr) {
		FileWriter fw = null;
		File logFile = new File(logFilePath);
		boolean newFile = false;

		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
				newFile = true;
			}

			if (newFile) {
				fw = new FileWriter(logFile, false);
			} else {
				fw = new FileWriter(logFile, true);
			}

			fw.write(inDateStr + " " + inStr + "\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
