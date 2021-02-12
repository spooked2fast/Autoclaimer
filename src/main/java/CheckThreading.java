import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class CheckThreading {
    private AccountCredentials accountCredentials;
    private ArrayList<XboxAccount> accounts;
    private ArrayList<String> gamertags;
    private ArrayList<Target> targets;
    private ThreadPoolExecutor executor;
    private int nThreads;
    private int RL = 0;
    private int successfulTries = 0;
    private Console console;
    private ReentrantLock lock = new ReentrantLock();
    private Claimer claimer;
    private Settings settings;
    private boolean running = false;
    public CheckThreading(AccountCredentials accountCredentials, ArrayList<Target> targets, int nThreads, Console console, Settings settings){
        this.settings = settings;
        this.console = console;
        this.nThreads = nThreads;
        this.accountCredentials = accountCredentials;
        this.targets = targets;
        accounts = accountCredentials.getXboxAccounts();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        ArrayList<MicrosoftAccount> claimAccounts = accountCredentials.getMicrosoftAccounts();
        claimer = new Claimer(claimAccounts,console, settings,accountCredentials);
    }
    public void createAndRunTasks(){
        int accountsSublistSize = accounts.size()/nThreads;
        int accountsArrayIndex =0;
        int targetsSublistSize = targets.size()/nThreads;
        int targetsArrayIndex = 0;
        for(int i =0; i < nThreads; i++){
            ArrayList<XboxAccount> accountSublist = new ArrayList<XboxAccount>();
            ArrayList<Target> targetSublist = new ArrayList<Target>();
            for(int j =0; j < accountsSublistSize;j++){
                accountSublist.add(accounts.get(j+accountsArrayIndex));
            }
            for(int x =0; x < targetsSublistSize;x++){
                targetSublist.add(targets.get(x+targetsArrayIndex));
            }
            accountsArrayIndex+=accountsSublistSize;
            targetsArrayIndex+=targetsSublistSize;
            CheckThread task = new CheckThread(accountSublist,targetSublist,this);
            executor.execute(task);
        }
        System.out.println();
        running = true;

    }
//    public void printClaiming(Target target){
//        System.out.println("\n");
//        console.updateMessage("Autoclaimer", "Claiming gamertag: " + target.getGamertag());
//        System.out.println("\n");
//    }
    public void updateRequestCount(boolean isRL){
        lock.lock();
        if(isRL){
            RL++;
        } else {
            successfulTries++;
        }
        if(! claimer.isClaiming() && running)
        console.updateAutoclaimerMessage(successfulTries,RL);
        lock.unlock();
    }
    public boolean claim(Target target){
        return claimer.claimGamertag(target);
    }
}
