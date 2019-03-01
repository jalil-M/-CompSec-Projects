import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

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

	public String handleRequest(String clientMsg) throws IOException {

		switch (usertype) {
		case PATIENT_USER:
			return patientHandler(clientMsg);
		case DOCTOR_USER:
			return doctorHandler(clientMsg);
		case NURSE_USER:
			return nurseHandler(clientMsg);
		case GA_USER:
			return gaHandler(clientMsg);
		default:
			log.unknownUsertypeEvent();
			return ("unknown usertype");
		}
	}

	private String patientHandler(String clientMsg) throws IOException {
		String[] cmdParts = clientMsg.split(" ");
		BufferedReader filereader = new BufferedReader(new FileReader(userfile));
		String[] credentials = filereader.readLine().split(";");
		String[] permissions = credentials[3].split(",");
		filereader.close();

		switch (cmdParts[0]) {
		case "read":
			if (cmdParts.length != 2) {
				filereader.close();
				return "wrong format, should be: read \"filename\"";
			}
			String srcFile = cmdParts[1];
			for (String entry : permissions) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					filereader = new BufferedReader(new FileReader(record));
					String msg = filereader.readLine();
					filereader.close();
					return msg;
				}
			}
			filereader.close();
			return "No such file or access denied";

		case "ls":
			String output = "";
			for (String entry : permissions) {
				output = output.concat(entry + "\n");
			}
			filereader.close();
			return output;
		case "write":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			filereader.close();
			return "Access denied";
		case "delete":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			filereader.close();
			return "Access denied";
		case "create":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			filereader.close();
			return "Access denied";
		default:
			filereader.close();
			return "Unknown cmd";
		}
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
