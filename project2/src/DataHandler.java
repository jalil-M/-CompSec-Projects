import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Handles record management Read, write, create, store, delete
 * 
 */
public class DataHandler {
	private HashMap<String, File> writePerm = new HashMap<String, File>();
	private HashMap<String, File> readPerm = new HashMap<String, File>();

	private String username = "";
	private int usertype = -1;
	private File userfile;
	private final ServerLog log;

	private static File records;
	private static File users;

	protected static final int PATIENT_USER = 0;
	protected static final int NURSE_USER = 1;
	protected static final int DOCTOR_USER = 2;
	protected static final int GA_USER = 3;

	public DataHandler(String username, File userfile, int usertype, ServerLog log) throws URISyntaxException {
		this.username = username;
		this.userfile = userfile;
		this.usertype = usertype;
		this.log = log;
		File root = new File(Thread.currentThread().getContextClassLoader().getResource("").toURI());
		records = new File(root.getParent() + File.separator + "records");
		users = new File(root.getParent() + File.separator + "users");
	}

	public void handleRequest(String clientMsg, PrintWriter out) throws IOException {

		switch (usertype) {
		case PATIENT_USER:
			out.println(patientHandler(clientMsg));
			break;
		case DOCTOR_USER:
			out.println(doctorHandler(clientMsg));
			break;
		case NURSE_USER:
			out.println(nurseHandler(clientMsg));
			break;
		case GA_USER:
			out.println(gaHandler(clientMsg));
			break;
		default:
			out.println("unknown usertype");
			log.unknownUsertypeEvent();
			return;
		}
	}

	private String patientHandler(String clientMsg) throws IOException {
		// TODO Auto-generated method stub
		String[] cmdParts = clientMsg.split(" ");
		StringBuilder sb = new StringBuilder();
		BufferedReader filereader = new BufferedReader(new FileReader(userfile));
		String[] credentials = filereader.readLine().split(";");
		filereader.close();

		switch (cmdParts[0]) {
		case "read":
			break;
		case "ls":
			String[] files = credentials[3].split(",");
			for (String record : files) {
				sb.append(record + "\n");
			}
			break;
		case "write":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			return "Access denied";
		case "delete":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			return "Access denied";
		case "create":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			return "Access denied";
		default:
			sb.append("Unknown cmd");
		}

		return sb.toString();
	}

	private String nurseHandler(String clientMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	private String doctorHandler(String clientMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	private String gaHandler(String clientMsg) {
		// TODO Auto-generated method stub
		return null;
	}

}
