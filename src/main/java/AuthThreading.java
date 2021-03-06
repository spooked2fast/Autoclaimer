import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class AuthThreading {
    private final FileIO fileIO = new FileIO();
    private final ThreadPoolExecutor executor;
    private ArrayList<String> microsoftAccounts;
    private ArrayList<String> xboxAccounts;
    private final Console console;
    private int tokensFinished;
    private final ReentrantLock lock = new ReentrantLock();
    private final AccountCredentials accountCredentials;
    private CountDownLatch latch;
    private final Settings settings;
    private ArrayList<String> xboxRefreshTokens;
    private ArrayList<String> microsoftRefreshTokens;
    public AuthThreading(int numThreads, Console console, AccountCredentials accountCredentials, Settings settings){
        this.console = console;
        this.settings = settings;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
        this.accountCredentials = accountCredentials;
        this.microsoftAccounts = accountCredentials.getMicrosoftAccountCredentials();
        this.xboxAccounts = accountCredentials.getXboxAccountCredentials();
        if(settings.isTokenAccounts()){
            this.microsoftAccounts = accountCredentials.getMicrosoftAccountCredentials();
            this.xboxAccounts = accountCredentials.getXboxAccountCredentials();
            console.updateMessage("+", microsoftAccounts.size() + " Microsoft accounts found");
            System.out.println();
            console.updateMessage("+", xboxAccounts.size() + " Xbox accounts found");
        } else {
            xboxRefreshTokens = fileIO.fileContentsToList(fileIO.getDataDirectoryPath() + "XboxTokens.txt");
            xboxRefreshTokens = fileIO.removeDuplicates(xboxRefreshTokens);
            microsoftRefreshTokens = fileIO.fileContentsToList(fileIO.getDataDirectoryPath() + "MicrosoftTokens.txt");
            microsoftRefreshTokens =fileIO.removeDuplicates(microsoftRefreshTokens);
            console.updateMessage("+", microsoftRefreshTokens.size() + " Microsoft tokens found");
            System.out.println();
            console.updateMessage("+", xboxRefreshTokens.size() + " Xbox tokens found");
        }
        System.out.println();
    }
    public void getTokens(){
        if(settings.isTokenAccounts()){
            getRefreshTokens();
        } else {
            getXstsOnly();
        }
        executor.shutdown();
    }
    public void writeToken(String token,boolean isXbox){
        lock.lock();
        try{
            if(isXbox)
                fileIO.writeToFile(fileIO.getDataDirectoryPath() + "XboxTokens.txt", token);
            else
                fileIO.writeToFile(fileIO.getDataDirectoryPath() +"MicrosoftTokens.txt", token);
        }catch(Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public void increaseCount(){
    }
    public void addMicrosoftAccount(MicrosoftAccount microsoftAccount){
        lock.lock();
        tokensFinished++;
        accountCredentials.addMicrosoftAccount(microsoftAccount);
        console.updateTokenHeader(tokensFinished);
        lock.unlock();
    }
    public void addXboxAccount(XboxAccount xboxAccount){
        lock.lock();
        tokensFinished++;
        accountCredentials.addXboxAccount(xboxAccount);
        console.updateTokenHeader(tokensFinished);
        lock.unlock();
    }
    public void getRefreshTokens(){
        latch = new CountDownLatch(microsoftAccounts.size() + xboxAccounts.size());
        for(String next : microsoftAccounts){
            AuthThread authThread = new AuthThread(next, accountCredentials.findPassword(next,false), this, false, latch,settings);
            executor.execute(authThread);
        }
        for(String next : xboxAccounts){
            AuthThread authThread = new AuthThread(next, accountCredentials.findPassword(next,true), this, true, latch,settings);
            executor.execute(authThread);
        }
        try {
            latch.await();
            executor.shutdown();
            System.out.print("\r");
            console.updateMessage("*", "Accounts Loaded: (" + tokensFinished+")");
        } catch (InterruptedException e) {
        }
    }
    public void getXstsOnly(){
        latch = new CountDownLatch(microsoftRefreshTokens.size() + xboxRefreshTokens.size());
        for(String next : microsoftRefreshTokens){
            AuthThread authThread = new AuthThread(next, this, false, latch, settings);
            executor.execute(authThread);
        }
        for(String next : xboxRefreshTokens){
            AuthThread authThread = new AuthThread(next, this, true, latch, settings);
            executor.execute(authThread);
        }
        try {
            latch.await();
            System.out.print("\r");
            console.updateMessage("*", "Accounts Loaded: (" + tokensFinished+")");
        } catch (InterruptedException e) {
        }
    }
}
