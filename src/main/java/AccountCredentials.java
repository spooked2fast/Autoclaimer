import java.util.ArrayList;
import java.util.HashMap;

public class AccountCredentials {
    private FileIO fileIO = new FileIO();
    private HashMap<String,String> microsoftAccountCredentials = new HashMap<String, String>();
    private HashMap<String,String> xboxAccountCredentials = new HashMap<String, String>();
    private XboxLiveAuth xboxLiveAuth;
    private ArrayList<XstsToken> xboxTokenPool = new ArrayList<XstsToken>();
    private ArrayList<XstsToken> microsoftTokenPool = new ArrayList<XstsToken>();
    private ArrayList<XboxAccount> xboxAccounts = new ArrayList<XboxAccount>();
    private ArrayList<MicrosoftAccount> microsoftAccounts = new ArrayList<MicrosoftAccount>();
    private Settings settings;
    public AccountCredentials(Settings settings){
        this.settings = settings;
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
        ArrayList<String> returnSet = new ArrayList<String>();
        for(String keyValue : microsoftAccountCredentials.keySet()){
            returnSet.add(keyValue);
        }
        return returnSet;
    }
    public ArrayList<String> getXboxAccountCredentials(){
        ArrayList<String> returnSet = new ArrayList<String>();
        for(String keyValue : xboxAccountCredentials.keySet()){
            returnSet.add(keyValue);
        }
        return returnSet;
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
