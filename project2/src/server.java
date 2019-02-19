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
	private static RecordHandler rh = null;

	public server(ServerSocket ss) throws IOException {
		serverSocket = ss;
		newListener();
	}

	@SuppressWarnings("deprecation")
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

			/*
			 * handle commands within while loop
			 */
			// TODO include uid
			String clientMsg = null;
			while ((clientMsg = in.readLine()) != null) {

				String[] clientCmd = clientMsg.trim().split(" ");

				if (clientCmd.length == 1) {
					if (clientCmd[0].equals("ls")) {
						rh.list();
						log.recordsListedEvent();
					}
				}

				String cmd = clientCmd[0];
				String option = clientCmd[1];

				if (cmd.equals("read")) {
					log.recordAccessedEvent();
					out.println(rh.read(option));
				} else if (cmd.equals("edit")) {

					rh.edit(option);
					log.editedRecordEvent();
				}

				if (cmd.equals("delete"))
					rh.delete(option);

				rh.createRecordFor(option, new String("Nurse"), new String("Doc"), new String("div"));

				rh.list();

				String rev = new StringBuilder(clientMsg).reverse().toString();
				System.out.println("received '" + clientMsg + "' from client");
				System.out.print("sending '" + rev + "' to client...");
				out.println(rev);
				out.flush();
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
			System.out.println("Client died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
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

				ks.load(new FileInputStream("serverkeystore"), password); // keystore password (storepass)
				ts.load(new FileInputStream("servertruststore"), password); // truststore password (storepass)
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
