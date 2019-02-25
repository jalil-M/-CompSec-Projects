import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;

public class simple {

	public static void main(String[] args) throws FileNotFoundException {

		boolean run = true;
		int tries = 0;
		
		Scanner scnr;
        scnr = new Scanner(new File("/home/jalilm/Desktop/code/java/list.txt"));
        String list[] = new String[20];
        int count = 0;

        while (scnr.hasNextLine()) {

            String line = scnr.nextLine();
            list[count] = line.substring(0, 5).trim();
            count++;
        }

        while (run) {

			Random rand = new Random();
			int n = rand.nextInt(5);

			String password = readPassword("Enter Password Number " + (n+1) + " on your list :");
			String Password = list[n];

			System.out.println(Password);

			if (password.equals(Password)) {
				System.out.println("Successful login!");
				tries = 0;
				// ENTER HERE THE COMMANDS OR UI IF SUCCESSFUL
				run = false;
			} else {
				System.out.println("Invalid Password! Look at the Table again");
				++tries;
				if (tries >= 3) {
					run = false;
					System.out.println("You have exceeded the number of login attempts.");
				}
			}
		}
	}

	public static String readPassword(String prompt) {
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
	 * @param The
	 *            prompt displayed to the user
	 */
	public EraserThread(String prompt) {
		System.out.print(prompt);
	}

	/**
	 * Begin masking...display asterisks (*)
	 */
	public void run() {
		stop = true;
		while (stop) {
			System.out.print("\010*");
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException ie) {
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
