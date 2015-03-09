package doug.git;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class SyncDBWithClient {
	
	static Socket cmdSocket;
	static Socket fileInSocket;
	static Socket fileOutSocket;

	static DataInputStream cmdIn;
	static DataOutputStream cmdOut;

	static String thisTo;
	static String thisFrom;
	static String thisCmd;
	static String param1;
	static String param2;
	static String param3;

	static String clientInLine1;
	static String clientInLine2;
	static String clientInLine3;
	static String clientInLine4;
	static String clientInLine5;
	static SocketCmd clientSocketCmd1;
	static SocketCmd clientSocketCmd2;
	static SocketCmd clientSocketCmd3;
	static SocketCmd clientSocketCmd4;
	static SocketCmd clientSocketCmd5;
	static SocketCmd svrSocketCmd1;
	static SocketCmd svrSocketCmd2;
	static SocketCmd svrSocketCmd3;
	static SocketCmd svrSocketCmd4;
	static SocketCmd svrSocketCmd5;
	
	static String clientName;

	static long fSize;

	public SyncDBWithClient(Socket cmdSock, Socket fiSock, Socket foSock,
			DataInputStream commandIn, DataOutputStream commandOut,
			SocketCmd firstCmd) {
		this.cmdSocket = cmdSock;
		this.fileInSocket = fiSock;
		this.fileOutSocket = foSock;
		
		this.cmdIn = commandIn;
		this.cmdOut = commandOut;
		
		this.clientSocketCmd1 = firstCmd;
		
	}
	
	public String clientSync() {
		
		String retn = null;

		try {
			this.thisTo = new String(clientSocketCmd1.to);
			int pctPos = clientSocketCmd1.from.indexOf("%%");
			
			this.clientName = new String(clientSocketCmd1.from.substring(2, pctPos));
			this.thisCmd = new String(clientSocketCmd1.cmd);
			

			if (thisTo.equals("^^Server%%") && thisCmd.equals("**ServerReceiveFile%%")) {
				this.fSize = clientSocketCmd1.fsize;
				Common.logit("Receive: **ServerReceiveFile%%", null);
				
				String nextClientFname = getNextClientFileName();

				receiveFileFromSocket(nextClientFname, (int) fSize);
				
				// Copy the file to the standard name for merging
				// this file will be overwritten
				File src = new File(nextClientFname);
				File dest = new File(Common.defaultCurrentClientDatabaseFilePath);
				
				Common.logit("           Copy received file (" +
						src.getName() + ") to " + dest.getName(),
						"COPY:    File: " + src.getAbsolutePath() + 
						" to " + dest.getAbsolutePath());
				// Delete the file if it exists
				//if (dest.exists()) {
				//	dest.delete();
				//}
				Files.copy(src.toPath(), dest.toPath(), REPLACE_EXISTING);
				
				//Merge the records with the master database
				MergeRecords mr = new MergeRecords(clientName);
				AddUpdate au = mr.mergeRecs();
				
				String mergeMsg = new String(
						"Merged records from: " +
						clientName + "; added: " + au.added +
						", updated: " + au.updated);
				
				Common.logit("           " + mergeMsg, "STAT:    " + mergeMsg);

				svrSocketCmd2 = new SocketCmd(clientName, "Server", 
						"ServerReceiveFileComplete", "OK");
				String svrCmd2x = new String(svrSocketCmd2.wholeCmdStr);

				Common.logit("Send   : **ServerReceiveFileComplete%%", "SEND: " + svrCmd2x);
				cmdOut.writeUTF(svrCmd2x);

				//System.out.println("After receive complete: Reading cmdIn...");
				clientInLine2 = cmdIn.readUTF();
				Common.logit(null, "RECV: " + clientInLine2);
				clientSocketCmd2 = new SocketCmd(clientInLine2);

				if (clientSocketCmd2.cmd.equals("**ClientReady%%")) {
					Common.logit("Receive: **ClientReady%%", null);
					
					String thisFileName = new String("database/dc.db");
					File thisFileFile = new File(thisFileName);
					long thisFileSize = thisFileFile.length();
					long thisFileModed = thisFileFile.lastModified();
					Date thisFileDate = new Date(thisFileModed);
					svrSocketCmd3 = new SocketCmd(clientName, "Server",
							"ClientReceiveFile", thisFileFile.getName(),
							thisFileSize, thisFileDate);
					String svrCmd3b = new String(svrSocketCmd3.wholeCmdStr);

					Common.logit("Send   : **ClientReceiveFile%%", "SEND: " + svrCmd3b);
					cmdOut.writeUTF(svrCmd3b);
					
					//System.out.println("Awaiting ClientReadyToReceive...");
					clientInLine3 = cmdIn.readUTF();
					Common.logit(null, "RECV: " + clientInLine3);
					clientSocketCmd3 = new SocketCmd(clientInLine3);
					
					if (clientSocketCmd3.cmd.equals("**ClientReadyToReceiveFile%%")) {
						Common.logit("Receive: **ClientReadyToReceiveFile%%", null);
						sendFileToSocket();
					}
					
					svrSocketCmd5 = new SocketCmd(clientName, "Server",
							"NoMoreTransactions");
					String svrCmd5b = new String(svrSocketCmd5.wholeCmdStr);
					Common.logit("Send   : **NoMoreTransactions%%", "SEND: " + svrCmd5b);
					cmdOut.writeUTF(svrCmd5b);
					
					clientInLine5 = cmdIn.readUTF();
					Common.logit(null, "RECV: " + clientInLine5);
					
					// new return and let Main handle last message
					retn = new String(clientInLine5);
					
				}  // if (clientSocketCmd2.cmd.equals("**ClientReady%%"))
			} // if (thisTo.equals("^^Server%%") && thisCmd.equals("**ServerReceiveFile%%"))

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return retn;
		
	}

	private static void receiveFileFromSocket(String fName, int fSize)
			throws IOException {

		//String cmdInLine = null;

		long start = System.currentTimeMillis();
		int bytesRead;
		int current = 0;

		start = System.currentTimeMillis();

		// receive file
		byte[] mybytearray = new byte[fSize + 100];
		InputStream is = fileInSocket.getInputStream();
		FileOutputStream fos = new FileOutputStream(fName); // destination
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		// Tell Client we are Ready
		Common.logit("Send   : **ServerReadyToReceiveFile%%", null);
		svrSocketCmd1 = new SocketCmd(clientName, "Server",
				"ServerReadyToReceiveFile",
				clientSocketCmd1.param1,
				clientSocketCmd1.param2,
				clientSocketCmd1.param3);
		String svrCmd1x = new String(svrSocketCmd1.wholeCmdStr);

		Common.logit(null, "SEND: " + svrCmd1x);
		cmdOut.writeUTF(svrCmd1x);

		Common.logit("           Receiving file...", null);

		bytesRead = is.read(mybytearray, 0, mybytearray.length);
		//System.out.println("First is.read bytesRead: " + bytesRead);
		current = bytesRead;

		int readCount = 1;
		if (bytesRead < fSize) {
			do {
				
				bytesRead = is.read(mybytearray, current,
						(mybytearray.length - current));
				if (bytesRead >= 0) current += bytesRead;
				
				// If the requisite number of bytes has been received then
				//      break out before EOF has been received
				if (current >= fSize) {
					Common.logit("           Breaking read loop on # bytes (" +
							String.valueOf(current) + ") received >= " +
							" fSize (" + String.valueOf(fSize) + ")", null);
					break;
				}
			} while (bytesRead > -1);
		}

		//fos.close();

		bos.write(mybytearray, 0, current);
		bos.flush();
		long end = System.currentTimeMillis();
		Common.logit("           File receive complete - Time to transfer file: " 
				+ String.valueOf(end - start) + " (ms)", null);
		bos.close();
	}
	
	private static void sendFileToSocket() throws IOException {
		// Send File
		File myFile = new File("database/dc.db");
		Common.logit("           File: " + myFile.getAbsolutePath(), null);
		byte[] mybytearray = new byte[(int) myFile.length()];

		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(mybytearray, 0, mybytearray.length);

		OutputStream os = fileOutSocket.getOutputStream();
		Common.logit("           Sending file...", null);
		os.write(mybytearray, 0, mybytearray.length);
		os.flush();

		//System.out.println("Awaiting receive complete...");
		clientInLine4 = cmdIn.readUTF();
		Common.logit(null, "RECV: " + clientInLine4);

		clientSocketCmd4 = new SocketCmd(clientInLine4);
		if (clientSocketCmd4.cmd.equals("**ClientReceiveFileComplete%%")) {
			Common.logit("Receive: **ClientReceiveFileComplete%% -- " +
					" Send File Complete - status: " + 
					clientSocketCmd4.status, null);

			fis.close();
			bis.close();
			os.close();
		}
	}
	
	private String getNextClientFileName() {
		String retn = null; 
		
		String masterDbName = new String(new File(Common.masterDbPath).getName());
		// Start with 1 and find the next available file name of
		//    the form ClientPrefix000001_masterDbName (i.e. Client000001_dc.db)
		int testSerialNum = 1;
		String testSerialNumStr = String.format(Common.clientFileSerialNumFormat, 
				testSerialNum);
		String proposedFilePath = new String(Common.filesFolder + 
				Common.clientFilePrefix + testSerialNumStr + "_" + masterDbName);
		boolean done = false;
		while (!done) {
			File testFile = new File(proposedFilePath);
			if (!testFile.exists()) {
				try {
					testFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				retn = new String(proposedFilePath);
				done = true;
			} else {
				// Increment testSerialNum, ensure it does not reach 1,000,000
				//      and form the new proposed file name
				testSerialNum++;
				if (testSerialNum < 1000000) {
					testSerialNumStr = new String(String.format(Common.clientFileSerialNumFormat, 
							testSerialNum));
					proposedFilePath = new String(Common.filesFolder + 
							Common.clientFilePrefix + testSerialNumStr + "_" + masterDbName);
				} else {
					Common.logit("***** ERROR: Max Client files exceeded in " + Common.filesFolder, 
							"ERRO: ERROR: Max Client files (1000000) exceeded " +
							"in folder: " + Common.filesFolder);
					retn = null;
				}
			}
		}
		return retn;
	}

}
