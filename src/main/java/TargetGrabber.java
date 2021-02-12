import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class TargetGrabber {
    private FileIO fileIO = new FileIO();
    private ArrayList<Target> validatedTargets = new ArrayList<Target>();
    private ArrayList<String> uncheckedTargets = new ArrayList<String>();
    private ArrayList <XboxAccount> accounts = new ArrayList<XboxAccount>();
    private AccountCredentials accountCredentials;
    private ThreadPoolExecutor executor;
    private CountDownLatch latch;
    private Console console;
    private ReentrantLock lock = new ReentrantLock();
    public TargetGrabber(AccountCredentials accountCredentials, Console console){
        this.uncheckedTargets = fileIO.fileContentsToList(fileIO.getDataDirectoryPath() + "Gamertags.txt");
        this.accountCredentials = accountCredentials;
        this.accounts = accountCredentials.getXboxAccounts();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        this.console = console;
    }
    public void refineTargets(){
        ArrayList<String> tagsNoDupes = fileIO.removeDuplicates(uncheckedTargets);
        ArrayList<String> allTagCombos = new ArrayList<String>();
        for(String tag : tagsNoDupes){
            String combo1 = tag.substring(0,1)+ " "+ tag.substring(1);
            String combo2 = tag.substring(0,tag.length()-1)+ " "+ tag.substring(tag.length()-1);
            allTagCombos.add(combo1);
            allTagCombos.add(combo2);
            allTagCombos.add(tag);
        }
        allTagCombos = fileIO.removeDuplicates(allTagCombos);
        createAndRunTasks(allTagCombos);
    }
    public void createAndRunTasks(ArrayList<String> allTagCombos){
        fileIO.clearFile("Filtered.txt");
        latch = new CountDownLatch(allTagCombos.size());
        int accountIndex = 0;
        for(int i = 0; i < allTagCombos.size(); i++){
            ValidateTargetThread validateTargetThread = new ValidateTargetThread(this,allTagCombos.get(i), latch,accounts.get(accountIndex), false);
            executor.execute(validateTargetThread);
            if(accountIndex < accounts.size()){
                accountIndex++;
            } else {
                accountIndex = 0;
            }
        }
        try {
            latch.await();
            System.out.print("\r");
            console.updateMessage("*", "Targets Found: (" + validatedTargets.size()+")");
        } catch (InterruptedException e) {
            //continue...
        }

    }
    public XboxAccount getRandomAccount(){
        return accountCredentials.getRandomAccount();
    }
    public void setValidatedTargets(Target target, boolean fail){
        lock.lock();
        validatedTargets.add(target);
        if(! fail){
            writeTagFiltered(target.getGamertag() + ":" + target.getXuid());
            console.updateTargetHeader(validatedTargets.size());
        }
        lock.unlock();
    }
    public ArrayList<Target> getTargets(){
        return validatedTargets;
    }
    public void writeTagFiltered(String tag)  {
        try {
            fileIO.writeToFile(fileIO.getDataDirectoryPath() + "Filtered.txt", tag);
        } catch (IOException e) {
        }
    }
    public String getNewTag(Target target){
        XboxAccount account = getRandomAccount();
        OkHttpClient client = new OkHttpClient();
        String newTag = account.findNewTag(target, client);
        return newTag;
    }
    public Target findNewTarget(Target target){
        validatedTargets.clear();
        ArrayList<String> toCheck = new ArrayList<String>(); //finding how tag was spaced in the swap on failure
        String tag = target.getGamertag();
        if(tag.contains(" ")){
            String[] tagSplit = tag.split(" ");
            tag = tagSplit[0] + tagSplit[1];
        }
        toCheck.add(tag);
        String combo1 = tag.substring(0,1)+ " "+ tag.substring(1);
        String combo2 = tag.substring(0,tag.length()-1)+ " "+ tag.substring(tag.length()-1);
        toCheck.add(combo1);
        toCheck.add(combo2);
        CountDownLatch latch = new CountDownLatch(toCheck.size());
        for(String combo: toCheck){
            ValidateTargetThread nextThread = new ValidateTargetThread(this,combo,latch,getRandomAccount(),true);
            nextThread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            //continue...
        }
        Target newTarget = validatedTargets.get(0);
        return newTarget;
    }
}
