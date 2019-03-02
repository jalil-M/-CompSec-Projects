import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Handles logging of events on the server to a log file.
 * 
 * @author Martin_2
 */
public class ServerLog {

	private final Path logPath = Paths.get("..\\logs\\server.log");

	public ServerLog() {
	}

	private synchronized void writeToLog(String logEntry) {
		String timestamp = getTimestamp();

		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		try {
			fw = new FileWriter(logPath.toString(), true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			out.println(logEntry + "\n" + timestamp);
			out.close();
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	private String getTimestamp() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
		return sdf.format(cal.getTime());
	}

	public void clientConnectsEvent(String subject, String issuer, BigInteger serial, int numConnectedClients) {
		String logEntry = "Client connected \n";
		logEntry = logEntry.concat("Client certificate subject DN field: " + subject + "\n");
		logEntry = logEntry.concat("Client certificate issuer DN field: " + issuer + "\n");
		logEntry = logEntry.concat("Certificate serial number: " + serial + "\n");
		logEntry = logEntry.concat(numConnectedClients + " concurrent connection(s) \n");

		writeToLog(logEntry);
	}

	public void disconnectEvent(int numConnectedClients) {
		String logEntry = "Client disconnected \n" + numConnectedClients + " concurrent connection(s)\n";
		writeToLog(logEntry);
	}

	public void createdRecordEvent(String filepath, String username) {
		String logEntry = "Record " + filepath + ", created by " + username;
		writeToLog(logEntry);
	}

	public void recordAccessedEvent(boolean permited, String username, String filename) {
		String logEntry = "User " + username + "attempted to read " + filename;
		if (permited) {
			logEntry = logEntry.concat(", action was permitted");
		} else {
			logEntry = logEntry.concat(", action was denied");
		}
		writeToLog(logEntry);
	}

	public void deletedRecordEvent(String username, String name) {
		String logEntry = "User " + username + " deleted file " + name;
		writeToLog(logEntry);
	}

	public void recordsListedEvent(String username) {
		String logEntry = "User " + username + " listed their available files.";
		writeToLog(logEntry);
	}

	public void caughtExceptionEvent(String string, Exception e) {
		// TODO to be called when exceptions occur
		String logEntry = string + e.getMessage();
		writeToLog(logEntry);
	}

	public void authenticationAttemptSucceeded(String subject, String username) {
		String logEntry = "Client subject: \n" + subject + "\nauthenticated as user: " + username;
		writeToLog(logEntry);
	}

	public void authenticationAttemptFailed(String subject, String username) {
		String logEntry = "Client subject: \n" + subject + "\nfailed to authenticate as user: " + username;
		writeToLog(logEntry);
	}

	public void unknownUsertypeEvent(String username, int usertype) {
		String logEntry = "An unknown usertype " + "\"" + usertype + "\"" + " for user " + username;
		writeToLog(logEntry);
	}

	public void unauthorisedActionAttemptedEvent(String action, String username) {
		String logEntry = "User " + username + " attempted an unauthorised action " + action;
		writeToLog(logEntry);
	}

	public void unrecognisedInputFormat(String[] input) {
		String logEntry = "Input: ";

		for (String entry : input) {
			logEntry = logEntry.concat(entry);
		}

		logEntry = logEntry.concat(" was not recognised");

		writeToLog(logEntry);
	}

	public void recordChangedEvent(String username, String oldData, String newData, String record) {
		String logEntry = "User " + username + " changed record " + record + " from " + oldData + " to " + newData;
		writeToLog(logEntry);
	}

}
