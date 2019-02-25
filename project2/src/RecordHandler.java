import java.io.File;
import java.io.IOException;
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
 * @author Martin_2
 */
public class RecordHandler {

	private ArrayList<Record> records = new ArrayList<Record>();
	private static File recordFolder = null;
	private int UID = -1;
	AccessControlMatrix acm = new AccessControlMatrix();

	public RecordHandler(String directoryPath, int UID) {
		this.recordFolder = new File(directoryPath);
		this.UID = UID;
	}

	private String read(String file) {
		// TODO Auto-generated method stub
		return "";
	}

	private void createRecordFor(String patient, String nurse, String doctor, String division, String data) {
		// TODO Auto-generated method stub

	}

	public void putRequest(String[] clientCmd) {
		// TODO Auto-generated method stub
		switch (clientCmd[0]) {
		case "read":
			if (acm.accessGranted(UID)) {

			}
			break;
		case "write":
			break;
		case "delete":

			break;
		case "ls":
			break;
		case "create":
			break;
		}

	}

	private boolean delete(String file) {
		try {
			return Files.deleteIfExists(Paths.get("file" + ".record"));
		} catch (IOException e) {
			return false;
		}
	}

	private synchronized void write(String patientID, String nurseID, String doctorID, String div, String data) {
		// TODO Auto-generated method stub

	}

	private void list() {
		// TODO Auto-generated method stub

	}

	private String[] parseFile() {
		// TODO Auto-generated method stub
		return null;
	}

	private static class AccessControlMatrix {
		private String[][] acl;
		private HashMap<String, Integer> uidMap; // uid gives index x
		private HashMap<String, Integer> fileMap; // filename gives index y
		private int fileCounter;
		private int userCounter;

		public AccessControlMatrix() {
			createACM();
		}

		public boolean accessGranted(int uID) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean putRequest(String cmd, String file, int UID) {
			// if(acl too large) {allocMatrix();}
			return false;
		}

		private boolean createACM() {
			for (final File recordEntry : recordFolder.listFiles()) {
				if()
			}
			return false;
		}

		private void allocMatrix() {
			return;
		}
	}

	private static class Record {
		private String id;
		private String patient;
		private String nurse;
		private String doctor;
		private String division;
	}

	private class User {
		// store readable files when creating user when creating recordhandler
		// store writeable files
		int UID;

	}
}
