import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	// TODO change entry to include userid

	private synchronized void writeToLog(String logEntry) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		try {
			fw = new FileWriter(logPath.toString(), true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			out.println(logEntry);
			out.close();
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	// todo include time stamp
	public void clientConnectsEvent(String subject, String issuer, BigInteger serial, int numConnectedClients) {
		String logEntry = "Client connected \n";
		logEntry = logEntry.concat("Client certificate subject DN field: " + subject + "\n");
		logEntry = logEntry.concat("Client certificate issuer DN field: " + issuer + "\n");
		logEntry = logEntry.concat("Certificate serial number: " + serial + "\n");
		logEntry = logEntry.concat(numConnectedClients + " concurrent connection(s) \n");

		writeToLog(logEntry);
	}

	// todo include time stamp
	public void disconnectEvent(int numConnectedClients) {
		String logEntry = "Client disconnected \n" + numConnectedClients + " concurrent connection(s)\n";
		writeToLog(logEntry);
	}

	// todo include time stamp
	public void createdRecordEvent() {
		// TODO Auto-generated method stub
		writeToLog("logEntry");

	}

	// todo include time stamp
	public void writeToRecordEvent() {
		// TODO Auto-generated method stub
		writeToLog("logEntry");

	}

	// todo include time stamp
	public void recordAccessedEvent() {
		// TODO Auto-generated method stub
		writeToLog("logEntry");

	}

	// todo include time stamp
	public void deletedRecordEvent() {
		// TODO Auto-generated method stub
		writeToLog("logEntry");

	}

	// todo include time stamp
	public void recordsListedEvent(boolean correctInput) {
		// TODO Auto-generated method stub
		writeToLog("logEntry");

	}

	public void caughtExceptionEvent(String string, Exception e) {
		// TODO Auto-generated method stub
		String logEntry = string + e.getMessage();
		writeToLog(logEntry);
	}

	public void authenticationAttemptSucceeded(String username) {
		// TODO Auto-generated method stub
		
	}

	public void authenticationAttemptFailed(String username) {
		// TODO Auto-generated method stub
		
	}

	public void unknownUsertypeEvent() {
		// TODO Auto-generated method stub
		
	}

	public void unauthorisedActionAttemptedEvent(String cmdParts, String username) {
		// TODO Auto-generated method stub
		
	}

}
