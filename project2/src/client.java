import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.cert.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.util.Scanner;

/*
 * This example shows how to set up a key manager to perform client
 * authentication.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class client {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		String usr = "";
		Scanner scanPass = new Scanner(System.in);
		String host = null;
		int port = -1;
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "] = " + args[i]);
		}
		if (args.length < 2) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}
		try { /* get input parameters */
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java client host port");
			System.exit(-1);
		}

		try { /* set up a key manager for client authentication */
			SSLSocketFactory factory = null;
			try {
				System.out.print("Keystore Password: ");
				char[] password = scanPass.nextLine().toCharArray();
				KeyStore ks = KeyStore.getInstance("JKS");
				KeyStore ts = KeyStore.getInstance("JKS");
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				SSLContext ctx = SSLContext.getInstance("TLS");
				ks.load(new FileInputStream("../TLS2/p2keystore"), password); // keystore password (storepass)
				ts.load(new FileInputStream("../TLS2/p2truststore"), password); // truststore password (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();

				String keystorepath = "../TLS2/p2keystore";
				usr = getCAInfo(extractCAInfo(keystorepath, password).replaceAll("\\s+", "")).get(0);

			} catch (Exception e) {
				// throw new IOException(e.getMessage());
				System.out.println("Client keystore password rejected.\n");

			}
			assert factory != null;
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			// System.out.println("\nsocket before handshake:\n" + socket + "\n");

			/*
			 * send http request
			 *
			 * See SSLSocketClient.java for more information about why there is a forced
			 * handshake here when using PrintWriters.
			 */
			socket.startHandshake();

			SSLSession session = socket.getSession();
			X509Certificate cert = session.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			String issuer = cert.getIssuerDN().getName();
			BigInteger serial = cert.getSerialNumber();
//			System.out.println(
//					"certificate name (subject DN field) on certificate received from server:\n" + subject + "\n");
//			System.out.println(
//					"certificate name (issuer DN field) on certificate received from server:\n" + issuer + "\n");
//			System.out.println("certificate serial number: " + serial + "\n");
//			System.out.println("socket after handshake:\n" + socket + "\n");
//			System.out.println("secure connection established\n\n");

			BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg;

			/* Send username to server */
			// System.out.println(usr);
			out.println(usr);
			out.flush();

			String input = in.readLine();
			System.out.println(input);
			if (input.equals("Provide password:")) {
				msg = read.readLine("enter password:");
				MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(msg.getBytes());
                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                String hash = sb.toString();
				out.println(hash);
				out.flush();
				//msg = "1BE00341082E25C4E251CA6713E767F7131A2823B0052CAF9C9B006EC512F6CB";// TODO change from hard-coded
				input = in.readLine();
				if (passwordExist(input)) {

					/* TODO openAppropriate GUI window */

					int usertype = Integer.parseInt(in.readLine()); // see DataHandler reference
					System.out.println(in.readLine());

					// for testing
					for (;;) {
						System.out.print(">");
						msg = read.readLine();
						if (msg.equalsIgnoreCase("quit")) {
							break;
						}
						System.out.print("sending '" + msg + "' to server...");
						out.println(msg);
						out.flush();
						System.out.println("done");
						System.out.println("received '" + in.readLine() + "' from server\n");
					}
					// for testing //

				} else {

					System.out.println("User not recognized, shutting down connection...");
					in.close();
					out.close();
					read.close();
					socket.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String extractCAInfo(String keystoreFileLocation, char[] password) {

		String caInfo = "";

		try {

			File file = new File(keystoreFileLocation);
			InputStream is = new FileInputStream(file);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			System.out.print("Accepting keystore password...\n");
			// System.out.println(password);
			keystore.load(is, password);

			Enumeration<String> enumeration = keystore.aliases();
			while (enumeration.hasMoreElements()) {
				String alias = enumeration.nextElement();
				if (alias.equals("mykey")) {
					Certificate certificate = keystore.getCertificate(alias);
					caInfo = certificate.toString();
				}
			}

			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return caInfo;

	}

	private static ArrayList<String> getCAInfo(String data) {

		int fromIndex = data.indexOf("CN");
		int toIndex = data.indexOf("Signature");

		StringBuilder moreData = new StringBuilder();
		moreData.append(data, fromIndex, toIndex).append(",");

		ArrayList<String> returnValue = new ArrayList<>(Arrays.asList(moreData.toString().split(",")));

		for (int i = 0; i < returnValue.size(); i++) {
			returnValue.set(i, removeChar(returnValue.get(i)));
		}

		return returnValue;

	}

	private static String removeChar(String string) {

		int fromIndex = string.indexOf("=") + 1;
		int toIndex = string.length();

		return string.substring(fromIndex, toIndex);
	}

	private static boolean passwordExist(String input) throws IOException {
		return input.equals("Authenticated!");
	}

}
