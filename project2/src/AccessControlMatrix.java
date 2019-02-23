import java.util.HashMap;

public class AccessControlMatrix {

	private RecordHandler rh;
	private String[][] acl;
	private HashMap<String, Integer> uidMap;
	private HashMap<String, Integer> fileMap;
	private int fileCounter;
	private int userCounter;

	public AccessControlMatrix(RecordHandler rh) {
		this.rh = rh;
		loadACLfile();
	}

	public boolean putRequest(String cmd, String file, int UID) {
		// if(acl too large) {allocMatrix();}
		return false;
	}

	private boolean loadACLfile() {
		return false;
	}

	private void allocMatrix() {
		return;
	}
}
