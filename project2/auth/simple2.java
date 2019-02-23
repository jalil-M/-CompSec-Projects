import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;

public class simple2 {

    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

        Scanner scan;
        scan = new Scanner(new File("/home/jalilm/Desktop/code/java/credentials.txt"));
        String credentials[][] = new String[100][4];
        int count = 0;

        while (scan.hasNextLine()) {

            String line = scan.nextLine();

            credentials[count][0] = line.substring(0, 13).trim();
            credentials[count][1] = line.substring(13, 78).trim();
            credentials[count][2] = line.substring(78, 85).trim();
            credentials[count][3] = line.substring(85).trim();
            count++;
        }

        Scanner input = new Scanner(System.in);
        boolean run = true;
        int tries = 0;

        while (run) {
            System.out.println("- Welcome to Lund's General Hospital -");
            System.out.println("1-Login");
            System.out.println("2-Exit");

            int ch = Integer.parseInt(input.nextLine().trim());

            if (ch == 1) {
//increment number of attempts
                tries++;
//request username and password
                System.out.print("Enter Username: ");
                String username = input.nextLine();
                String password = readPassword("Enter Password: ");
//generate hash
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(password.getBytes());
                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                String hash = sb.toString();

                boolean wrongUser = true;

                for (int i = 0; i < count; i++) {
                    if (username.contentEquals(credentials[i][0])) {
                        if (hash.contentEquals(credentials[i][1])) {
//if verified, logged in
                            List<String> data = null;
//check type of user and print
                            switch (credentials[i][3]) {
                                case "patient":
                                    data = Files.readAllLines(Paths.get("/home/jalilm/Desktop/code/java/patient.txt"), Charset.defaultCharset());
                                    break;
                                case "nurse":
                                    data = Files.readAllLines(Paths.get("/home/jalilm/Desktop/code/java/nurse.txt"), Charset.defaultCharset());
                                    break;
                                case "doctor":
                                    data = Files.readAllLines(Paths.get("/home/jalilm/Desktop/code/java/doctor.txt"), Charset.defaultCharset());
                                    break;
                                case "GA":
                                	data = Files.readAllLines(Paths.get("/home/jalilm/Desktop/code/java/GA.txt"), Charset.defaultCharset());
                                default:
                                    break;
                            }
                            if (data != null) {
                                for (String s : data) {
                                    System.out.println(s);
                                }
                            }
//reset
                            tries = 0;

                            System.out.println("\n1) Logout.");
                            System.out.println("2) Exit.");

                            ch = Integer.parseInt(input.nextLine().trim());
                            if (ch == 2) {
                                run = false;
                            }
                            wrongUser = false;
                            break;
                        }
                    }
                }
                if (wrongUser) {
                    System.out.println("Invalid Username or Password.");
                }
            } else {
                break;
            }
//limit attempts
            if (tries == 3) {
                run = false;
                System.out.println("You have exceeded the number of login attempts.");
            }
        }
        

    }
    
    public static String readPassword (String prompt) {
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
	    *@param prompt displayed to the user
	    */
	   public EraserThread(String prompt) {
	       System.out.print(prompt);
	   }

	   /**
	    * Begin masking...display asterisks (*)
	    */
	   public void run () {
	      stop = true;
	      while (stop) {
	         System.out.print("\010*");
	     try {
	        Thread.currentThread().sleep(1);
	         } catch(InterruptedException ie) {
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

    