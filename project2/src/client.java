import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
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

				// TODO search function that finds the path of the keystore and truststore
				// stored in the USB

				ks.load(new FileInputStream("../USB/keystore"), password); // keystore password (storepass)
				ts.load(new FileInputStream("../USB/truststore"), password); // truststore password (storepass);
				kmf.init(ks, password); // user password (keypass)
				tmf.init(ts); // keystore can be used as truststore here
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				factory = ctx.getSocketFactory();

				String keystorepath = "../USB/keystore";
				usr = getCAInfo(extractCAInfo(keystorepath, password).replaceAll("\\s+", "")).get(0);

			} catch (Exception e) {
				// throw new IOException(e.getMessage());
				System.out.println("Client keystore password rejected.\n");

			}
			assert factory != null;
			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

			/*
			 * send http request
			 *
			 * See SSLSocketClient.java for more information about why there is a forced
			 * handshake here when using PrintWriters.
			 */
			socket.startHandshake();

			SSLSession session = socket.getSession();
			X509Certificate cert = session.getPeerCertificateChain()[0];

			BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg;

			/* Send username to server */
			// System.out.println(usr);
			out.println(usr);
			out.flush();

			String input = in.readLine();
			if (input.equals("Provide password:")) {
				while (!passwordExist(input)) {
					String pw = readPassword(input);
					MessageDigest md = MessageDigest.getInstance("SHA-256");
					md.update(pw.getBytes());
					byte[] digest = md.digest();
					StringBuilder sb = new StringBuilder();
					for (byte b : digest) {
						sb.append(String.format("%02x", b & 0xff));
					}
					String hash = sb.toString().toUpperCase();
					out.println(hash);
					out.flush();
					input = in.readLine();
					if (input.equals("failed")) {
						System.out.println("Authentication failed");
						in.close();
						out.close();
						read.close();
						socket.close();
						return;
					}
				}

				/* TODO openAppropriate GUI window */

				int usertype = Integer.parseInt(in.readLine()); // see DataHandler reference
				System.out.println(in.readLine());

				for (;;) {
					System.out.print(PersonAccess.userCommands(usertype));
					System.out.print(">");
					msg = read.readLine();
					if (msg.equalsIgnoreCase("quit")) {
						break;
					}
					out.println(msg);
					out.flush();
					String ans = in.readLine();
					// TODO perhaps we should handle different commands in different ways, as with
					// ls. write/create with ';' in data should either ignore this char for format
					// purposes (maybe replace with ',') or give an error message.
					String output = "recieved: \n";
					String[] inputArray = msg.split(" ");

					if (msg.equalsIgnoreCase("ls")) {
						String[] files = ans.split(" ");
						for (String file : files) {
							output = output.concat(file + "\n");
						}
					} else if (inputArray.length == 2 && inputArray[0].equalsIgnoreCase("read")) {
						String[] fileData = ans.split(";");
						String nurse = fileData[0];
						String doctor = fileData[1];
						String division = fileData[2];
						String data = fileData[3];
						output = output.concat("Nurse: " + nurse + "\n");
						output = output.concat("Doctor: " + doctor + "\n");
						output = output.concat("Division: " + division + "\n");
						output = output.concat("Record: " + data + "\n");
					} else {
						output = output.concat(ans);
					}

					System.out.println(output);
				}

			} else {

				System.out.println("User not recognized, shutting down connection...");
				in.close();
				out.close();
				read.close();
				socket.close();
			}

		} catch (

		Exception e) {
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

	public static String readPassword(String prompt) {
		EraserThread et = new EraserThread(prompt);
		Thread mask = new Thread(et);
		mask.start();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String password = "";

		try {
			password = in.readLine();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// stop masking
		et.stopMasking();
		// return the password entered by the user
		return password;
	}

}

class EraserThread implements Runnable {
	private boolean stop;

	/**
	 * @param prompt
	 *            displayed to the user
	 */
	public EraserThread(String prompt) {
		System.out.print(prompt);
	}

	/**
	 * Begin masking...display asterisks (*)
	 */
	public void run() {
		stop = true;
		while (stop) {
			System.out.print("\010*");
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Instruct the thread to stop masking
	 */
	public void stopMasking() {
		this.stop = false;
	}

}
