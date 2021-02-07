import java.util.ArrayList;
import java.util.HashMap;

public class AccountCredentials {
    private FileIO fileIO = new FileIO();
    private HashMap<String,String> microsoftAccountCredentials;
    private XboxLiveAuth xboxLiveAuth;
    public AccountCredentials(){
        ArrayList<String> accountsFileContents = fileIO
        .fileContentsToList(fileIO.getDataDirectoryPath() + "MicrosoftAccounts.txt");
        for(String login : accountsFileContents){
            String[] emailAndPassword = login.split(":");
            microsoftAccountCredentials.put(emailAndPassword[0], emailAndPassword[1]);
        }
    }
    public String findPassword(String email){
        return microsoftAccountCredentials.get(email);
    }
}
