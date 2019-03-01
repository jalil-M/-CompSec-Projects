import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
	private File userfile;
	private HashMap<String, File> writePerm = new HashMap<String, File>();
	private HashMap<String, File> readPerm = new HashMap<String, File>();
	private int userType = -1;

	protected static final int PATIENT_USER = 0;
	protected static final int NURSE_USER = 1;
	protected static final int DOCTOR_USER = 2;
	protected static final int GA_USER = 3;

	public DataHandler(File userfile, int uType) {
		this.userfile = userfile;
		this.userType = uType;
	}

	public void handleRequest(String clientMsg, PrintWriter out) {
		// TODO Auto-generated method stub
		switch (userType) {
		case PATIENT_USER:
			break;
		case DOCTOR_USER:
			break;
		case NURSE_USER:
			break;
		case GA_USER:
			break;
		default:
			break;
		}
	}

	private String[] ls() {
		// TODO internal files + division

		return null;
	}

}
