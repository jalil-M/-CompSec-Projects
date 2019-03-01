import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.math.BigInteger;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private static final ServerLog log = new ServerLog();
	private static DataHandler dh = null;

	private static final int PATIENT_USER = 0;
	private static final int NURSE_USER = 1;
	private static final int DOCTOR_USER = 2;
	private static final int GA_USER = 3;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
	}

	public void run() {
		try {
			SSLSocket socket = (SSLSocket) serverSocket.accept();
			newListener();
			SSLSession session = socket.getSession();
			X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			String issuer = cert.getIssuerDN().getName();
			BigInteger serial = cert.getSerialNumber();
			numConnectedClients++;

			/**
			 * this paragraph should be filed to the log
			 */
			log.addClientEvent(subject, issuer, serial, numConnectedClients);

			/**
			 * handles input from client
			 */
			PrintWriter out = null;
			BufferedReader in = null;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			File userfile = null;

			/* instead of disconnect, maybe use failed authorisation for logging purposes */
			if (authoriseUser(in, out, userfile)) {
				if (checkPW(in, out, userfile)) {
				} else {
					in.close();
					out.close();
					socket.close();
					numConnectedClients--;
					log.disconnectEvent(numConnectedClients);
					return;
				}
			} else {
				in.close();
				out.close();
				socket.close();
				numConnectedClients--;
				log.disconnectEvent(numConnectedClients);
				return;
			}

			if (userfile == null) {
				in.close();
				out.close();
				socket.close();
				numConnectedClients--;
				log.disconnectEvent(numConnectedClients);
				return;
			}

			/*
			 * handle commands within while loop
			 */
			// TODO include uid
			DataHandler dh = new DataHandler(userfile);
			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {

				dh.handleRequest(clientMsg, out);

				System.out.println("done\n");
			}

			/*
			 * close connection
			 */
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			log.disconnectEvent(numConnectedClients);
		} catch (IOException e) {
			log.caughtExceptionEvent("Client died: ", e);
			return;
		}
	}

	private boolean authoriseUser(BufferedReader in, PrintWriter out, File userfile) throws IOException {
		String username = in.readLine();
		File userFolder = new File("../users/");
		System.out.println(userFolder.getAbsolutePath());
		for (final File fileEntry : userFolder.listFiles()) {
			String[] fileParts = fileEntry.getName().split(".");
			if (fileParts.length > 0 && fileParts[0].equals(username)) {
				userfile = fileEntry;
				out.println("Provide password:");
				out.flush();
				return true;
			}
		}
		return false;
	}

	private boolean checkPW(BufferedReader in, PrintWriter out, File userfile) throws IOException {
		String inputHash = in.readLine();
		BufferedReader filereader = new BufferedReader(new FileReader(userfile));
		String storedHash = filereader.readLine().split(";")[3];
		filereader.close();
		if (storedHash.equals(inputHash)) {
			return true;
		}
		return false;
	}

	private void newListener() {
		(new Thread(this)).start();
	} // calls run()

	public static void main(String args[]) {
		System.out.println("\nServer Started\n");
		int port = -1;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		String type = "TLS";
		try {
			ServerSocketFactory ssf = getServerSocketFactory(type);
			ServerSocket ss = ssf.createServerSocket(port);
			((SSLServerSocket) ss).setNeedClientAuth(true); // enables client authentication
			new server(ss);
		} catch (IOException e) {
			System.out.println("Unable to start Server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try { // set up key manager to perform server authentication
				SSLContext ctx = SSLContext.getInstance("TLS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				char[] password = "password".toCharArray();

				ks.load(new FileInputStream("../TLS2/serverkeystore"), password); // keystore password (storepass)
				ts.load(new FileInputStream("../TLS2/servertruststore"), password); // truststore password (storepass)
				kmf.init(ks, password); // certificate password (keypass)
				tmf.init(ts); // possible to use keystore as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}

}
