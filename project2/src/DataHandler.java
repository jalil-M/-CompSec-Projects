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

	public DataHandler(String directoryPath, int UID) {
	}

	public DataHandler(File userfile) {
		// TODO Auto-generated constructor stub
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
