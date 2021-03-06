import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class CheckThreading {
    private final ArrayList<XboxAccount> accounts;
    private final ArrayList<Target> targets;
    private final ThreadPoolExecutor executor;
    private final int nThreads;
    private int RL = 0;
    private long averageTime = 0;
    private int checkCount = 0;
    private long deltaTime = 0;
    private int successfulTries = 0;
    private final Console console;
    private final ReentrantLock lock = new ReentrantLock();
    private final Claimer claimer;
    private boolean running = false;
    private final Settings settings;
    private final AtomicBoolean claiming = new AtomicBoolean(false);
    public CheckThreading(AccountCredentials accountCredentials, ArrayList<Target> targets, int nThreads, Console console, Settings settings){
        this.console = console;
        this.settings = settings;
        this.nThreads = nThreads;
        this.targets = targets;
        accounts = accountCredentials.getXboxAccounts();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        ArrayList<MicrosoftAccount> claimAccounts = accountCredentials.getMicrosoftAccounts();
        claimer = new Claimer(claimAccounts,console, settings,accountCredentials);
    }
    public void createAndRunTasks(){
        int accountsSublistSize = accounts.size()/nThreads;
        int accountsArrayIndex =0;
        int targetsSublistSize = settings.getBatchSize();
        int targetsArrayIndex = 0;
        for(int i =0; i < nThreads; i++){
            ArrayList<XboxAccount> accountSublist = new ArrayList<>();
            ArrayList<Target> targetSublist = new ArrayList<>();
            for(int j =0; j < accountsSublistSize;j++){
                accountSublist.add(accounts.get(j+accountsArrayIndex));
            }
            for(int x =0; x < targetsSublistSize;x++){
                targetSublist.add(targets.get(targetsArrayIndex));
                if(targetsArrayIndex < targets.size()-1){
                    targetsArrayIndex++;
                } else {
                    targetsArrayIndex =0;
                }
            }
            accountsArrayIndex+=accountsSublistSize;
            CheckThread task = new CheckThread(accountSublist,targetSublist,this);
            executor.execute(task);
        }
        System.out.println();
        running = true;
    }
public void updateRequestCount(boolean isRL, long time){
        lock.lock();
        checkCount++;
        deltaTime +=time;
        averageTime = deltaTime / checkCount;
        if(isRL){
            RL++;
        } else {
            successfulTries++;
        }
        if(! claimer.isClaiming() && running)
        console.updateAutoclaimerMessage(successfulTries,RL,averageTime);
        lock.unlock();
    }
    public void updateRequestCount(boolean isRL){
        lock.lock();
        if(isRL){
            RL++;
        } else {
            successfulTries++;
        }
        if(! claimer.isClaiming() && running)
            console.updateAutoclaimerMessage(successfulTries,RL,averageTime);
        lock.unlock();
    }
    public boolean claim(Target target,String newTag){
        boolean claimed = false;
        if(! claiming.get()){
            claiming.set(true);
//            claimed = claimer.multiClaim(target,newTag);
            claimed = claimer.claim(target,newTag);
            claiming.set(false);
        }
        return claimed;
    }
    public void setAverageTime(long deltaTime){
        checkCount++;
        averageTime = deltaTime / checkCount;
    }
}
