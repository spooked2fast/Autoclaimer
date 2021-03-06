import java.util.ArrayList;
import java.util.HashMap;

public class AccountCredentials {
    private final FileIO fileIO = new FileIO();
    private final HashMap<String,String> microsoftAccountCredentials = new HashMap<>();
    private final HashMap<String,String> xboxAccountCredentials = new HashMap<>();
    private final ArrayList<XstsToken> xboxTokenPool = new ArrayList<>();
    private final ArrayList<XstsToken> microsoftTokenPool = new ArrayList<>();
    private final ArrayList<XboxAccount> xboxAccounts = new ArrayList<>();
    private final ArrayList<MicrosoftAccount> microsoftAccounts = new ArrayList<>();

    public AccountCredentials(Settings settings){
        readCredentials("MicrosoftAccounts.txt", microsoftAccountCredentials);
        readCredentials("XboxAccounts.txt", xboxAccountCredentials);
        if(settings.isTokenAccounts()){
            fileIO.clearFile("MicrosoftTokens.txt");
            fileIO.clearFile("XboxTokens.txt");
        }
    }
    public String findPassword(String email,boolean xbox){
        if(! xbox)
            return microsoftAccountCredentials.get(email);
        else
            return xboxAccountCredentials.get(email);
    }
    public void readCredentials(String pathName, HashMap<String, String> credentials){
        ArrayList<String> accountsFileContents = fileIO
                .fileContentsToList(fileIO.getDataDirectoryPath() + pathName);
        for(String login : accountsFileContents){
            String[] emailAndPassword = login.split(":");
            credentials.put(emailAndPassword[0], emailAndPassword[1]);
        }
    }
    public ArrayList<String> getMicrosoftAccountCredentials(){
        return new ArrayList<>(microsoftAccountCredentials.keySet());
    }
    public ArrayList<String> getXboxAccountCredentials(){
        return new ArrayList<>(xboxAccountCredentials.keySet());
    }
    public void addXboxToken(XstsToken xstsToken){
        xboxTokenPool.add(xstsToken);
    }
    public void addMicrosoftToken(XstsToken xstsToken){
        microsoftTokenPool.add(xstsToken);
    }
    public void addXboxAccount(XboxAccount xboxAccount){
        xboxAccounts.add(xboxAccount);
    }
    public void addMicrosoftAccount(MicrosoftAccount microsoftAccount){
        microsoftAccounts.add(microsoftAccount);
    }
    public ArrayList<MicrosoftAccount> getMicrosoftAccounts(){
        return microsoftAccounts;
    }
    public ArrayList<XboxAccount> getXboxAccounts(){
        return xboxAccounts;
    }
    public XboxAccount getRandomAccount(){
        int index = (int) Math.random() * (xboxAccounts.size()-1) ;
        return xboxAccounts.get(index);
    }
}
