import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.math.BigInteger;

public class server implements Runnable {
	private ServerSocket serverSocket = null;
	private static int numConnectedClients = 0;
	private static final ServerLog log = new ServerLog();

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
			log.clientConnectsEvent(subject, issuer, serial, numConnectedClients);

			/**
			 * handles input from client
			 */
			PrintWriter out = null;
			BufferedReader in = null;
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			/* instead of disconnect, maybe use failed authorisation for logging purposes */
			File userfile = null;
			String username = in.readLine();
			int usertype = getUnitType(username);

			if ((userfile = authoriseUser(username)) != null) {

				out.println("Provide password:");
				out.flush();

				String hash = in.readLine();
				if (checkPW(hash, userfile, usertype)) {

					log.authenticationAttemptSucceeded(username);
					out.println("Authenticated!");
					out.flush();

				} else {

					in.close();
					out.close();
					socket.close();
					numConnectedClients--;
					log.authenticationAttemptFailed(username);
					return;
				}
			} else {
				in.close();
				out.close();
				socket.close();
				numConnectedClients--;
				log.authenticationAttemptFailed(username);
				return;
			}

			/*
			 * handle commands within while loop
			 */
			// TODO include uid

			DataHandler dh = new DataHandler(username, userfile, usertype, log);
			out.println(usertype);
			out.println("Enter command:");
			out.flush();
			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {

				dh.handleRequest(clientMsg, out);

				System.out.println("Handled \"" + clientMsg + "\" from " + username);
			}

			/*
			 * close connection
			 */
			in.close();
			out.close();
			socket.close();
			numConnectedClients--;
			log.disconnectEvent(numConnectedClients);
		} catch (IOException | URISyntaxException e) {
			log.caughtExceptionEvent("Client died: ", e);
			return;
		}
	}

	private int getUnitType(String username) {
		switch (username.charAt(0)) {
		case 'p':
			return DataHandler.PATIENT_USER;
		case 'd':
			return DataHandler.DOCTOR_USER;
		case 'n':
			return DataHandler.NURSE_USER;
		case 'G':
			return DataHandler.GA_USER;
		default:
			return -1;
		}
	}

	private File authoriseUser(String username) throws URISyntaxException {
		File root = new File(Thread.currentThread().getContextClassLoader().getResource("").toURI());
		File userFolder = new File(root.getParent() + File.separator + "users");
		for (final File fileEntry : userFolder.listFiles()) {
			String[] fileParts = fileEntry.getName().split("\\.");
			if (fileParts.length > 0 && fileParts[0].equals(username)) {
				return fileEntry;
			}
		}
		return null;
	}

	private boolean checkPW(String inputHash, File userfile, int unitType) throws IOException {
		BufferedReader filereader = new BufferedReader(new FileReader(userfile));
		String[] credentials = filereader.readLine().split(";");
		String storedHash;

		switch (unitType) {
		case DataHandler.PATIENT_USER:
			storedHash = credentials[2];
			break;
		case DataHandler.DOCTOR_USER:
			storedHash = credentials[3];
			break;
		case DataHandler.NURSE_USER:
			storedHash = credentials[3];
			break;
		case DataHandler.GA_USER:
			storedHash = credentials[2];
			break;
		default:
			storedHash = null;
			break;
		}

		filereader.close();

		if (storedHash != null && storedHash.equals(inputHash)) {
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
