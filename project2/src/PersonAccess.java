public class PersonAccess {

    public static String userCommands(int attribute) {

        String commands = "\n'ls' to list available records\n" +
                "'quit' to exit\n\n";
        String read = "READ records type: read patientID-recordID.records\n";
        String create = "CREATE records type: create patientID;nurseID;doctorID;division;'insert record data here'\n";
        String edit = "EDIT records type: write: patientID-recordID.records 'insert record data here'\n";
        String delete = "DELETE records type: delete patientID-recordId.records\n";
        String quit = "'quit' to exit.";

        switch (attribute) {
            case 0: //patient
                return commands + read;

            case 1: //nurse
                return commands + read + edit;

            case 2: //doctor
                return commands + read + edit + create;

            case 3: //GA
                return commands + read + delete;
        }
        return "Error person not identified!";
    }

}
