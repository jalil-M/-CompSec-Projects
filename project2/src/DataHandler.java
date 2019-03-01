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
	private int uType = 0;

	public DataHandler(File userfile, int uType) {
		this.userfile = userfile;
		this.uType = uType;
	}

	public void handleRequest(String clientMsg, PrintWriter out) {
		// TODO Auto-generated method stub
		switch (clientMsg) {
		case "read":
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
		out.println("to be printed");
		out.flush();
	}

}
