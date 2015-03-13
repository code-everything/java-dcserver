package doug.git;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class DCServer {

	static ServerSocket servsock = null;
	static Socket cmdSocket;
	static Socket fileInSocket;
	static Socket fileOutSocket;

	static DataInputStream cmdIn;
	static DataOutputStream cmdOut;

	static String thisTo;
	static String thisFrom;
	static String thisCmd;

	static String clientInLine;
	static SocketCmd clientSocketCmd;
	
	static String clientName;
	
	static String classReturnInCommand;
	static SocketCmd classReturnSocketCmd;

	public DCServer() {
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		long start = System.currentTimeMillis();
		int bytesRead;
		int current = 0;

		@SuppressWarnings("unused")
		boolean keepLooping = true;

		// create new socket
		try {
			servsock = new ServerSocket(1149);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Common.logit("************** Server Startup -- Version: " + Common.version,
				"STAT:    ***** Server Startup -- Version: " + Common.version);
		
		readConfig();
		Common.logit("            Read config file. master_client: " + Common.masterClient,
				"STAT:    ***** Read config file. master_client: " + Common.masterClient);

		// Loop servicing clients
		while (true) {
			
			Common.logit("           Waiting for new connection...", 
					"STAT:    ***** AWAITING NEW CONNECTION");

			try {
				cmdSocket = servsock.accept();
				fileInSocket = servsock.accept();
				fileOutSocket = servsock.accept();
				Common.logit(null, "STAT:    ***** OPENED NEW CONNECTION");

				cmdIn = new DataInputStream(cmdSocket.getInputStream());
				cmdOut = new DataOutputStream(cmdSocket.getOutputStream());

				Common.logit("           Connected", null);
				clientInLine = cmdIn.readUTF();
				Common.logit(null, "RECV: " + clientInLine);
				clientSocketCmd = new SocketCmd(clientInLine);

				thisTo = new String(clientSocketCmd.to);
				
				clientName = new String(clientSocketCmd.from);
				int pctPos = clientName.indexOf("%%");
				Common.currentClient = new String(clientName.substring(2, pctPos));
				
				thisCmd = new String(clientSocketCmd.cmd);

				if (thisTo.equals("^^Server%%") && thisCmd.equals("**ServerReceiveFile%%")) {
					SyncDBWithClient swc = new SyncDBWithClient(
							cmdSocket,
							fileInSocket,
							fileOutSocket,
							cmdIn,
							cmdOut,
							clientSocketCmd);
					classReturnInCommand = swc.clientSync();
				}
				
				if (thisTo.equals("^^Server%%") && thisCmd.equals("**ClientReceiveLogFile%%")) {
					SyncLogWithClient slf = new SyncLogWithClient(
							cmdSocket,
							fileInSocket,
							fileOutSocket,
							cmdIn,
							cmdOut,
							clientSocketCmd);
					classReturnInCommand = slf.clientSync();
				}
				
				classReturnSocketCmd = new SocketCmd(classReturnInCommand);
				
				if (classReturnSocketCmd.cmd.equals("**EndConnection%%")) {
					Common.logit("Receive: **EndConnection%%", null);
					cmdIn.close();
					cmdOut.close();
					cmdSocket.close();
					fileInSocket.close();
					fileOutSocket.close();
					Common.logit(null, "STAT:    ***** CLOSED CONNECTION");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (!keepLooping) break;
		}

	}
	
	private static void readConfig() {
		
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(Common.configFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String mc = prop.getProperty("master_client");
		
		Common.masterClient = new String(mc);
		
	}

}
