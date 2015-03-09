package doug.git;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SocketCmd {
/**
 * Sample Command string:
 * ##:^^Server%%:$$TestClient%%:**ServerReceiveFile%%:dc.db:20480bytes:20150228T175821:##
 * 
 * Server/Client	Command						NumParams	Params
 * Client			**ServerReceiveFile%%			3		filename, filesize, datetime
 * Server			**ServerReadyToReceiveFile%%	3		filename, filesize, datetime
 * Client			**ClientSendingFile%%			0
 * Server			**ServerReceiveFileComplete%%	1		status (OK, ERROR)
 * Client			**ClientReady%%					0
 * Server			**NoClientReceiveFile%%			0
 * Client			**EndConnection%%				0
 * Server			**ClientReceiveFile%%			3		filename, filesize, datetime
 * Client			**ClientReadyToReceiveFile%%	3		filename, filesize, datetime
 * Client			**ClientReceiveFileComplete%%	1		status (OK, ERROR)
 * Server			**NoMoreTransactions%%			0
 *
 * Client			**ClientReceiveLogFile%%		0
 * Server			**ServerSendLogFile%%			3
 * Client			**ClientReadyToReceiveFile%%	3		filename, filesize, datetime
 * Client			**ClientReceiveFileComplete%%	1		status (OK, ERROR)
 * Server			**NoMoreTransactions%%			0
 * Client			**EndConnection%%				0
 * 
 */
	static private boolean isServer = true;
	String wholeCmdStr;
	String start;
	String to;
	String from;
	String cmd;
	String param1;	// File name or status
	String param2;  // File Size
	String param3;  // File modification date/time (format: YYYYMMDDTHHMMSS)
	String finish;
	String status;
	String fname;
	long fsize;
	int numParams;
	String datetime;
	String dateStr;
	String timeStr;
	// String commandType;
	boolean valid;
	
	private static String[][] thisCmdType;

	public SocketCmd(String wholeCmdStr) {
		this.wholeCmdStr = wholeCmdStr;
			
		initializeThisCmdType();
		parseInput();
	}
		
	// 0 Param command
	public SocketCmd(String to, String from, String cmd) {
		this.start = new String("##");
		this.to = new String("^^" + to + "%%");
		this.from = new String("$$" + from + "%%");
		this.cmd = new String("**" + cmd + "%%");
		this.finish = new String("##");
		
		this.wholeCmdStr = new String(this.start + ":" + 
				this.to + ":" +
				this.from + ":" +
				this.cmd + ":" +
				this.finish);
		this.valid = true;
		
		this.param1 = null;
		this.param2 = null;
		this.param3 = null;
		this.status = null;
		this.fname = null;
		this.fsize = 0;
		this.numParams = 0;
		this.datetime = null;
		this.dateStr = null;
		this.timeStr = null;
		// this.commandType = null;
	}
	
	// 1 Param command
	public SocketCmd(String to, String from, String cmd,
			String status) {
		
		this.start = new String("##");
		this.to = new String("^^" + to + "%%");
		this.from = new String("$$" + from + "%%");
		this.cmd = new String("**" + cmd + "%%");
		
		this.param1 = new String(status);
		this.status = new String(param1);
		this.finish = new String("##");

		this.wholeCmdStr = new String(this.start + ":" + 
				this.to + ":" +
				this.from + ":" +
				this.cmd + ":" +
				this.param1 + ":" +
				this.finish);
		this.numParams = 1;
		this.valid = true;
		
		this.param2 = null;
		this.param3 = null;
		this.finish = null;
		this.fname = null;
		this.fsize = 0;
		this.datetime = null;
		this.dateStr = null;
		this.timeStr = null;
		// this.commandType = null;
	}
		
	// 3 Param command with String fname, long size, and Date for file date
	public SocketCmd(String to, String from, String cmd,
			String fname, long fsize, Date inDate) {
			
		this.start = new String("##");
		this.to = new String("^^" + to + "%%");
		this.from = new String("$$" + from + "%%");
		this.cmd = new String("**" + cmd + "%%");
			
		this.param1 = new String(fname);
		this.fname = new String(param1);
		
		this.param2 = new String(String.valueOf(fsize) + "bytes");
		this.fsize = fsize;
		
		this.param3 = new String(formatDate(inDate));
		this.datetime = new String(param3);
		this.dateStr = new String(param3.substring(0, 8));
		this.timeStr = new String(param3.substring(9));
		
		this.finish = new String("##");
		
		this.wholeCmdStr = new String(this.start + ":" + 
				this.to + ":" +
				this.from + ":" +
				this.cmd + ":" +
				this.param1 + ":" +
				this.param2 + ":" +
				this.param3 + ":" +
				this.finish);
		this.numParams = 3;
		this.valid = true;
		
		status = null;
		// this.commandType = null;
	}
	
	// 3 Param command with String fname, String size, and String for datetime
	public SocketCmd(String to, String from, String cmd,
		String fname, String strSize, String datetime) {
		
		this.start = new String("##");
		this.to = new String("^^" + to + "%%");
		this.from = new String("$$" + from + "%%");
		this.cmd = new String("**" + cmd + "%%");
		
		this.param1 = new String(fname);
		this.fname = new String(param1);
	
		this.param2 = new String(strSize);
		String nBytesStr = new String(param2.substring(0, param2.length() - 5));
		this.fsize = Long.parseLong(nBytesStr);
	
		this.param3 = new String(datetime);
		this.datetime = new String(param3);
		this.dateStr = new String(param3.substring(0, 8));
		this.timeStr = new String(param3.substring(9));
	
		this.finish = new String("##");
	
		this.wholeCmdStr = new String(this.start + ":" + 
			this.to + ":" +
			this.from + ":" +
			this.cmd + ":" +
			this.param1 + ":" +
			this.param2 + ":" +
			this.param3 + ":" +
			this.finish);
		this.numParams = 3;
		this.valid = true;
	
		status = null;
		// this.commandType = null;
	}
		
	private void parseInput() {
		
		final int intClient0Param = 1;
		final int intServer0Param = 2;
		final int intClient1Param = 3;
		final int intServer1Param = 4;
		final int intClient3Param = 5;
		final int intServer3Param = 6;
		
		String parts[] = this.wholeCmdStr.split(":");
		int nParts = parts.length;
			
		this.start = null;
		this.to = null;
		this.from = null;
		this.cmd = null;
		this.param1 = null;
		this.param2 = null;
		this.param3 = null;
		this.finish = null;
		this.status = null;
		this.fname = null;
		this.fsize = 0;
		this.numParams = 0;
		this.datetime = null;
		this.dateStr = null;
		this.timeStr = null;
		// this.commandType = null;
		this.valid = false;
			
		if (nParts >= 5) {
			this.start = new String(parts[0]);
			this.to = new String(parts[1]);
			this.from = new String(parts[2]);
			this.cmd = new String(parts[3]);
			
			int switchCmd = returnThisCmdType(this.cmd);
			
			switch (switchCmd) {
				case intClient0Param:
					if (nParts == 5) {
						this.finish = new String(parts[4]);
						this.valid = true;
						this.numParams = 0;
						// this.commandType = new String("client");
					}
					break;
				case intServer0Param:
					if (nParts == 5) {
						this.finish = new String(parts[4]);
						this.valid = true;
						this.numParams = 0;
						// this.commandType = new String("server");
					}
					break;
				case intClient1Param:
					if (nParts == 6) {
						this.param1 = new String(parts[4]);
						this.status = new String(param1);
						
						this.finish = new String(parts[5]);
						
						this.numParams = 1;
						// this.commandType = new String("client");
					}
					break;
				case intServer1Param:
					if (nParts == 6) {
						this.param1 = new String(parts[4]);
						this.status = new String(param1);
						
						this.finish = new String(parts[5]);
						
						this.numParams = 1;
						// this.commandType = new String("server");
					}
					break;
				case intClient3Param:
					if (nParts == 8) {
						this.param1 = new String(parts[4]);
						this.fname = new String(param1);
						
						this.param2 = new String(parts[5]);
						String nBytesStr = new String(param2.substring(0, param2.length() - 5));
						this.fsize = Long.parseLong(nBytesStr);
						
						this.param3 = new String(parts[6]);
						this.datetime = new String(param3);
						this.dateStr = new String(param3.substring(0, 8));
						this.timeStr = new String(param3.substring(9));
						
						this.finish = new String(parts[7]);
						
						this.numParams = 3;
						// this.commandType = new String("client");
						this.valid = true;
					}
					break;
				case intServer3Param:
					if (nParts == 8) {
						this.param1 = new String(parts[4]);
						this.fname = new String(param1);
						
						this.param2 = new String(parts[5]);
						String nBytesStr = new String(param2.substring(0, param2.length() - 5));
						this.fsize = Long.parseLong(nBytesStr);
						
						this.param3 = new String(parts[6]);
						this.datetime = new String(param3);
						this.dateStr = new String(param3.substring(0, 8));
						this.timeStr = new String(param3.substring(9));
						
						this.finish = new String(parts[7]);
						
						this.numParams = 3;
						// this.commandType = new String("server");
						this.valid = true;
					}
					break;
				default:
					this.valid = false;
					if (isServer) {
						Common.logit("***** ERROR Received Invalid Command: " + 
								this.wholeCmdStr, 
								"ERRO: ***** ERROR: INVALID COMMAND: " + this.wholeCmdStr);
					} else {
						Common.logit("***** ERROR Received Invalid Command: " + 
								this.wholeCmdStr, null); 
					}
			}
		}
			
	}
		
	private String formatDate(Date inDate) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
		String outStr = new String(sdfDate.format(inDate) + "T" + sdfTime.format(inDate));
		return outStr;
	}
	
	private void initializeThisCmdType() {
		
		final String strClient0Param = "1";
		final String strServer0Param = "2";
		final String strClient1Param = "3";
		final String strServer1Param = "4";
		final String strClient3Param = "5";
		final String strServer3Param = "6";
		
		thisCmdType = new String[][] {
				{ "**ClientSendingFile%%", new String(strClient0Param) },
				{ "**ClientReady%%", new String(strClient0Param) },
				{ "**EndConnection%%", new String(strClient0Param) },
				{ "**NoClientReceiveFile%%", new String(strServer0Param) },
				{ "**NoMoreTransactions%%", new String(strServer0Param) },
				{ "**ClientReceiveFileComplete%%", new String(strClient1Param) },
				{ "**ServerReceiveFileComplete%%", new String(strServer1Param) },
				{ "**ServerReceiveFile%%", new String(strClient3Param) },
				{ "**ClientReadyToReceiveFile%%", new String(strClient3Param) },
				{ "**ServerReadyToReceiveFile%%", new String(strServer3Param) },
				{ "**ClientReceiveFile%%", new String(strServer3Param) },
				{ "**ClientReceiveLogFile%%", new String(strClient0Param) },
				{ "**ServerSendLogFile%%", new String (strServer3Param) }
		};
		
	}
	
	private int returnThisCmdType(String inThisCmd) {
		boolean done = false;
		int retn = -1;
		
		int idx = 0;
		while (!done) {
			// Next command gets NullPointerException
			if (inThisCmd.equals(thisCmdType[idx][0])) {
				done = true;
				retn = Integer.parseInt(thisCmdType[idx][1]);
			}
			idx++;
			if (idx >= thisCmdType.length) {
				done = true;
			}
		}
		return retn;
	}

}
