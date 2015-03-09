package doug.git;

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
import java.util.Date;

public class SyncLogWithClient {

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
	static SocketCmd clientSocketCmd1;
	static SocketCmd clientSocketCmd2;
	static SocketCmd clientSocketCmd3;
	static SocketCmd clientSocketCmd4;
	static SocketCmd svrSocketCmd1;
	static SocketCmd svrSocketCmd2;

	static String clientName;

	static long fSize;

	public SyncLogWithClient(Socket cmdSock, Socket fiSock, Socket foSock,
			DataInputStream commandIn, DataOutputStream commandOut,
			SocketCmd firstCmd) {
		this.cmdSocket = cmdSock;
		this.fileInSocket = fiSock;
		this.fileOutSocket = foSock;

		this.cmdIn = commandIn;
		this.cmdOut = commandOut;

		this.clientInLine1 = new String(firstCmd.wholeCmdStr);
		this.clientSocketCmd1 = firstCmd;

	}

	public String clientSync() {

		String retn = null;

		try {
			thisTo = new String(clientSocketCmd1.to);
			clientName = new String(clientSocketCmd1.from);
			thisCmd = new String(clientSocketCmd1.cmd);

			if (thisTo.equals("^^Server%%")
					&& thisCmd.equals("**ClientReceiveLogFile%%")) {
				Common.logit("Receive: **ClientReceiveLogFile%%", null);

				File logFile = new File(Common.logFilePath);
				fSize = logFile.length();
				Date fDate = new Date(logFile.lastModified());

				svrSocketCmd1 = new SocketCmd(clientName, "Server",
						"ServerSendLogFile", logFile.getName(), fSize, fDate);
				String svrCmd1 = new String(svrSocketCmd1.cmd);
				Common.logit("Receive: **ClientReceiveLogFile%%", "SEND: "
						+ svrSocketCmd1.wholeCmdStr);
				cmdOut.writeUTF(svrSocketCmd1.wholeCmdStr);

				// System.out.println("Awaiting ClientReadyToReceive...");
				clientInLine2 = cmdIn.readUTF();
				Common.logit(null, "RECV: " + clientInLine2);
				clientSocketCmd2 = new SocketCmd(clientInLine2);

				if (clientSocketCmd2.cmd.equals("**ClientReadyToReceiveFile%%")) {
					Common.logit("Receive: **ClientReadyToReceiveFile%%", null);
					sendFileToSocket(logFile);
				}

				svrSocketCmd2 = new SocketCmd(clientName, "Server",
						"NoMoreTransactions");
				String svrCmd2 = new String(svrSocketCmd2.wholeCmdStr);
				Common.logit("Send   : **NoMoreTransactions%%", "SEND: "
						+ svrCmd2);
				cmdOut.writeUTF(svrCmd2);

				clientInLine4 = cmdIn.readUTF();
				Common.logit(null, "RECV: " + clientInLine4);

				// new return and let caller handle last message
				retn = new String(clientInLine4);

			} // if (thisTo.equals("^^Server%%") && thisCmd.equals("**ServerReceiveFile%%"))

		} catch (IOException e) {
			e.printStackTrace();
		}

		return retn;

	}

	private static void sendFileToSocket(File myFile) throws IOException {
		// Send File
		Common.logit("           File: " + myFile.getAbsolutePath(), null);
		byte[] mybytearray = new byte[(int) myFile.length()];

		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(mybytearray, 0, mybytearray.length);

		OutputStream os = fileOutSocket.getOutputStream();
		Common.logit("           Sending file...", null);
		os.write(mybytearray, 0, mybytearray.length);
		os.flush();

		// System.out.println("Awaiting receive complete...");
		clientInLine3 = cmdIn.readUTF();
		Common.logit(null, "RECV: " + clientInLine3);

		clientSocketCmd3 = new SocketCmd(clientInLine3);
		if (clientSocketCmd3.cmd.equals("**ClientReceiveFileComplete%%")) {
			Common.logit("Receive: **ClientReceiveFileComplete%% -- "
					+ " Send File Complete - status: "
					+ clientSocketCmd3.status, null);

			fis.close();
			bis.close();
			os.close();
		}
	}
}
