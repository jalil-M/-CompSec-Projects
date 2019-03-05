import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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

	// TODO proper exception handling and logging

	public String handleRequest(String clientMsg) throws IOException, URISyntaxException {
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
			log.unknownUsertypeEvent(username, usertype);
			return ("unknown usertype");
		}
	}

	private String patientHandler(String clientMsg) throws IOException {
		String[] cmdParts = clientMsg.split(" ");
		BufferedReader filereader = new BufferedReader(new FileReader(userfile));
		String firstLine = filereader.readLine();
		String[] permissions = buildPermissions(firstLine, filereader);
		filereader.close();

		switch (cmdParts[0]) {
		case "read":
			if (cmdParts.length != 2) {
				filereader.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: read \"filename\"";
			}
			String srcFile = cmdParts[1];
			for (String entry : permissions) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					filereader = new BufferedReader(new FileReader(record));
					String msg = filereader.readLine();
					filereader.close();
					log.recordAccessedEvent(true, username, srcFile);
					return msg;
				}
			}
			filereader.close();
			log.recordAccessedEvent(false, username, srcFile);
			return "No such file or access denied";

		case "ls":

			if (cmdParts.length != 1) {
				filereader.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: ls";
			}
			String output = "";

			for (String entry : permissions) {
				output.concat(entry + " ");
			}

			filereader.close();
			log.recordsListedEvent(username);
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

	private String nurseHandler(String clientMsg) throws IOException {
		String[] cmdParts = clientMsg.split(" ");
		BufferedReader fr = new BufferedReader(new FileReader(userfile));
		String firstLine = fr.readLine();
		String div = firstLine.split(";")[2];
		String[] permissions = buildPermissions(firstLine.split(";")[4], fr);
		fr.close();
		File divFile = new File(users.getAbsolutePath() + File.separator + div + ".txt");
		fr = new BufferedReader(new FileReader(divFile));
		String[] divFiles = fr.readLine().split(";")[2].split(",");

		switch (cmdParts[0]) {
		case "read":
			if (cmdParts.length != 2) {
				fr.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: read \"filename\"";
			}

			/*
			 * own files
			 */

			String srcFile = cmdParts[1];

			for (String entry : permissions) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					fr = new BufferedReader(new FileReader(record));
					String msg = fr.readLine();
					fr.close();
					log.recordAccessedEvent(true, username, srcFile);
					return msg;
				}
			}

			/*
			 * div files
			 */
			for (String entry : divFiles) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					fr = new BufferedReader(new FileReader(record));
					String msg = fr.readLine();
					fr.close();
					log.recordAccessedEvent(true, username, srcFile);
					return msg;
				}
			}

			fr.close();
			log.recordAccessedEvent(false, username, srcFile);
			return "No such file or access denied";

		case "ls":
			ArrayList<String> list = new ArrayList<String>();

			if (cmdParts.length != 1) {
				fr.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: ls";
			}

			for (String entry : permissions) {
				list.add(entry);
			}

			/*
			 * div files
			 */
			for (String entry : divFiles) {
				if (!list.contains(entry)) {
					list.add(div + ":" + entry);
				}
			}

			String output = "";
			for (String entry : list) {
				output = output.concat(entry + " ");
			}

			fr.close();
			log.recordsListedEvent(username);
			return output.trim();
		case "write":
			cmdParts = clientMsg.split(" ", 3);
			if (cmdParts.length != 3) {
				fr.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: write \"file\" \"data\"";
			}

			/*
			 * own files
			 */

			srcFile = cmdParts[1];

			for (String entry : permissions) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					fr = new BufferedReader(new FileReader(record));
					String oldData = fr.readLine().split(";")[2];
					FileWriter fw = new FileWriter(record, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw);
					String newData = cmdParts[2];
					out.print(" " + newData);
					out.close();
					fr.close();
					log.recordChangedEvent(username, oldData, newData, record.getName());
					return "update confirmed";
				}
			}

			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			fr.close();
			return "Access denied";
		case "delete":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			fr.close();
			return "Access denied";
		case "create":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			fr.close();
			return "Access denied";
		default:
			fr.close();
			return "Unknown cmd";
		}
	}

	private String doctorHandler(String clientMsg) throws IOException, URISyntaxException {
		String[] cmdParts = clientMsg.split(" ");
		BufferedReader fr = new BufferedReader(new FileReader(userfile));
		String firstLine = fr.readLine();
		String div = firstLine.split(";")[2];
		String[] permissions = buildPermissions(firstLine.split(";")[4], fr);
		fr.close();
		File divFile = new File(users.getAbsolutePath() + File.separator + div + ".txt");
		fr = new BufferedReader(new FileReader(divFile));
		String text = fr.lines().collect(Collectors.joining());
		text = text.replace("\n", "").replace("\r", "");
		Reader fr2 = new StringReader(text);
		fr = new BufferedReader(fr2);
		String[] divFiles = fr.readLine().split(";")[2].split(",");

		switch (cmdParts[0]) {
		case "read":
			if (cmdParts.length != 2) {
				fr.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: read \"filename\"";
			}

			/*
			 * own files
			 */

			String srcFile = cmdParts[1];

			for (String entry : permissions) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					fr = new BufferedReader(new FileReader(record));
					String msg = fr.readLine();
					fr.close();
					log.recordAccessedEvent(true, username, srcFile);
					return msg;
				}
			}

			/*
			 * div files
			 */
			for (String entry : divFiles) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					fr = new BufferedReader(new FileReader(record));
					String msg = fr.readLine();
					fr.close();
					log.recordAccessedEvent(true, username, srcFile);
					return msg;
				}
			}

			fr.close();
			log.recordAccessedEvent(false, username, srcFile);
			return "No such file or access denied";

		case "ls":
			ArrayList<String> list = new ArrayList<String>();

			if (cmdParts.length != 1) {
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: ls";
			}

			for (String entry : permissions) {
				list.add(entry);
			}

			/*
			 * div files
			 */
			for (String entry : divFiles) {
				if (!list.contains(entry)) {
					list.add(div + ":" + entry);
				}
			}

			String output = "";
			for (String entry : list) {
				output = output.concat(entry + " ");
			}

			log.recordsListedEvent(username);
			return output.trim();

		case "write":
			cmdParts = clientMsg.split(" ", 3);
			if (cmdParts.length != 3) {
				fr.close();
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: write \"file\" \"data\"";
			}

			/*
			 * own files
			 */

			srcFile = cmdParts[1];

			for (String entry : permissions) {
				if (srcFile.equals(entry)) {
					File record = new File(records.getAbsolutePath() + File.separator + srcFile);
					fr = new BufferedReader(new FileReader(record));
					String oldData = fr.readLine().split(";")[2];
					FileWriter fw = new FileWriter(record, true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw);
					String newData = cmdParts[2];
					out.print(" " + newData);
					out.close();
					fr.close();
					log.recordChangedEvent(username, oldData, newData, record.getName());
					return "update confirmed";
				}
			}

			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			fr.close();
			return "Access denied";
		case "delete":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			return "Access denied";
		case "create":
			cmdParts = clientMsg.split(" ", 2);

			if (cmdParts.length != 2) {
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: create patient;nurse;doctor;division;data";
			}

			String patient = cmdParts[1].split(";")[0];

			if (patientExists(patient)) {
				return createRecord(cmdParts[1], div);
			} else {
				return "No such patient";
			}

		default:
			return "Unknown cmd";
		}

	}

	private String gaHandler(String clientMsg) throws IOException {
		String[] cmdParts = clientMsg.split(" ");
		switch (cmdParts[0]) {
		case "read":
			if (cmdParts.length != 2) {
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: read \"file\"";
			}

			String name = "";
			String data = "";
			for (File file : records.listFiles()) {
				name = file.getName();
				if (name.equals(cmdParts[1])) {

					BufferedReader br = new BufferedReader(new FileReader(file));
					data = br.readLine();
					br.close();
					log.recordAccessedEvent(true, username, cmdParts[1]);
					return data;
				}
			}

			return "Unknown file";
		case "ls":
			ArrayList<String> list = new ArrayList<String>();

			if (cmdParts.length != 1) {
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: ls";
			}

			/*
			 * div files
			 */
			for (File file : records.listFiles()) {
				list.add(file.getName());
			}

			String output = "";
			for (String entry : list) {
				output = output.concat(entry + " ");
			}

			log.recordsListedEvent(username);
			return output.trim();

		case "write":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			return "Access denied";
		case "delete":
			if (cmdParts.length != 2) {
				log.unrecognisedInputFormat(cmdParts);
				return "wrong format, should be: delete \"file\"";
			}

			/*
			 * div files
			 */

			name = "";
			data = "";
			for (File file : records.listFiles()) {
				name = file.getName();
				if (name.equals(cmdParts[1])) {

					BufferedReader br = new BufferedReader(new FileReader(file));
					data = br.readLine();
					br.close();
					file.delete();
					System.out.println(file.delete());
					br.close();
					removeRecord(name, data);
					return name + " deleted";
				}
			}

			return "Could not delete " + name;
		case "create":
			log.unauthorisedActionAttemptedEvent(cmdParts[0], username);
			return "Access denied";
		default:
			return "Unknown cmd";
		}
	}

	private String createRecord(String input, String div2) throws IOException, URISyntaxException {
		String[] dataInput = input.split(";");
		String patient = dataInput[0];
		String nurse = dataInput[1];
		String doctor = username;
		String div = div2;

		String index = findIndex(patient);

		if (dataInput.length != 3 || !isNurse(nurse) || !isPatient(patient)) {
			return "Wrong format, try: patient;nurse;data";
		}

		String filedata = buildFile(dataInput, div);

		String newRecord = patient + "-" + index + ".records";

		File newFile = (Files.write(Paths.get(records.getAbsolutePath() + File.separator + newRecord),
				filedata.getBytes())).toFile();

		String filepath = newFile.getName();
		BufferedWriter pw;
		for (File file : users.listFiles()) {
			if (!file.isDirectory()) {

				String path = file.getName();
				String name = path.split("\\.")[0];

				pw = new BufferedWriter(new FileWriter(file, true));
				if (name.equals(patient) || name.equals(nurse) || name.equals(div) || name.equals(doctor)) {
					pw.write("," + filepath);
					pw.close();
				}
			}
		}
		log.createdRecordEvent(filepath, username);
		return "Created record " + newRecord;
	}

	private boolean isPatient(String patient) throws URISyntaxException {
		if (patient.charAt(0) == 'p') {
			File root = new File(Thread.currentThread().getContextClassLoader().getResource("").toURI());
			File userFolder = new File(root.getParent() + File.separator + "users");
			for (final File fileEntry : userFolder.listFiles()) {
				String[] fileParts = fileEntry.getName().split("\\.");
				if (fileParts.length > 0 && fileParts[0].equals(patient)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isNurse(String nurse) throws URISyntaxException {
		if (nurse.charAt(0) == 'n') {
			File root = new File(Thread.currentThread().getContextClassLoader().getResource("").toURI());
			File userFolder = new File(root.getParent() + File.separator + "users");
			for (final File fileEntry : userFolder.listFiles()) {
				String[] fileParts = fileEntry.getName().split("\\.");
				if (fileParts.length > 0 && fileParts[0].equals(nurse)) {
					return true;
				}
			}
		}
		return false;
	}

	private String findIndex(String patient) {
		ArrayList<String> list = new ArrayList<String>();
		for (File file : records.listFiles()) {
			String name = file.getName();
			String[] fileAttr = name.split("\\.")[0].split("-");
			if (fileAttr[0].equals(patient)) {
				list.add(fileAttr[1]);
			}
		}

		int highestIndex = 0;
		for (String index : list) {
			int intIndex = Integer.parseInt(index);
			if (highestIndex < intIndex) {
				highestIndex = intIndex;
			}
		}

		return String.format("%03d", highestIndex + 1);
	}

	private String buildFile(String[] dataInput, String div) throws ArrayIndexOutOfBoundsException {
		String nurse = dataInput[1];
		String doctor = username;
		String division = div;
		String data = dataInput[2];
		String output = nurse + ";" + doctor + ";" + division + ";" + data;
		return output;
	}

	private void removeRecord(String name, String data) throws IOException {
		log.deletedRecordEvent(username, name);
		String[] content = data.split(";");
		BufferedReader br;
		PrintWriter pw;
		for (int i = 0; i < 2; i++) {
			String user = content[i];
			File fileToUpdate = new File(users.getAbsolutePath() + File.separator + user + ".txt");
			br = new BufferedReader(new FileReader(fileToUpdate));
			String[] fileContent = br.readLine().split(";");
			pw = new PrintWriter(fileToUpdate);
			pw.print(fileContent[0] + ";");
			pw.print(fileContent[1] + ";");
			pw.print(fileContent[2] + ";");
			pw.print(fileContent[3] + ";");

			String[] recordlist = fileContent[4].split(",");
			for (int j = 0; j < recordlist.length; j++) {
				String entry = recordlist[j];
				if (!entry.equals(name)) {
					pw.print(entry);
					if (recordlist.length - j > 1) {
						pw.print(",");
					}
				}
			}
			br.close();
			pw.close();
		}

		String div = content[2];
		File fileToUpdate = new File(users.getAbsolutePath() + File.separator + div + ".txt");
		br = new BufferedReader(new FileReader(fileToUpdate));
		String[] fileContent = br.readLine().split(";");
		pw = new PrintWriter(fileToUpdate);
		pw.print(fileContent[0] + ";");
		pw.print(fileContent[1] + ";");

		String[] recordlist = fileContent[2].split(",");
		for (int j = 0; j < recordlist.length; j++) {
			String entry = recordlist[j];

			if (!entry.equals(name)) {
				pw.print(entry);
				if (recordlist.length - j > 1) {
					pw.print(",");
				}
			}
		}

		br.close();
		pw.close();

	}

	private boolean patientExists(String username) throws URISyntaxException {
		File root = new File(Thread.currentThread().getContextClassLoader().getResource("").toURI());
		File userFolder = new File(root.getParent() + File.separator + "users");
		for (final File fileEntry : userFolder.listFiles()) {
			String[] fileParts = fileEntry.getName().split("\\.");
			if (fileParts.length > 0 && fileParts[0].equals(username)) {
				return true;
			}
		}
		return false;
	}

	private String[] buildPermissions(String lineOfPermissions, BufferedReader fr) throws IOException {
		String[] permissions = lineOfPermissions.split(",");
		while (lineOfPermissions.endsWith("\n")) {
			lineOfPermissions = fr.readLine();
			String[] newPerms = lineOfPermissions.split(",");
			permissions = Arrays.copyOf(permissions, permissions.length + newPerms.length);
			System.arraycopy(newPerms, 0, permissions, permissions.length, newPerms.length);
		}

		return permissions;
	}
}
