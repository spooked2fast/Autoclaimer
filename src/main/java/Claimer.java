import com.google.common.base.Stopwatch;
import okhttp3.OkHttpClient;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Claimer {
    private final OkHttpClient client = new OkHttpClient();
    private final ArrayList<MicrosoftAccount> accounts;
    private int accountIndex = 0;
    private final Console console;
    private final FileIO fileIO = new FileIO();
    private final Mail mail;
    private final AccountCredentials accountCredentials;
    private final TargetGrabber targetGrabber;
    private boolean claiming = false;
    private long claimTime = 0;
    private boolean claimedUsingOrigin;
    private final ReentrantLock lock = new ReentrantLock();
    private MicrosoftAccount success;

    Claimer(ArrayList<MicrosoftAccount> accounts, Console console, Settings settings, AccountCredentials accountCredentials){
        this.accountCredentials = accountCredentials;
        this.mail = new Mail(settings);
        this.console = console;
        this.accounts = accounts;
        this.targetGrabber = new TargetGrabber(accountCredentials, console);
    }
    public boolean isClaiming(){
        return claiming;
    }
    public boolean claimGamertag(String gamertag, CountDownLatch latch, boolean isOrigin) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean claimed = false;
        increaseAccIndex();
        int indexUsed = accountIndex;
        MicrosoftAccount account = accounts.get(indexUsed);
        int responseCode = account.sendTroublePost(gamertag, this.client, isOrigin);
        if (responseCode == 200) {
            stopwatch.stop();
            accounts.remove(account);
            decreaseAccIndex();
            claimTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            claimedUsingOrigin = isOrigin;
            success = account;
            claimed = true;
        }
        latch.countDown();
        return claimed;
    }
    public boolean claimGamertag(String gamertag) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean claimed = false;
        increaseAccIndex();
        int indexUsed = accountIndex;
        MicrosoftAccount account = accounts.get(indexUsed);
        int responseCode = account.sendTroublePost(gamertag, this.client, false);
        if (responseCode == 200) {
            stopwatch.stop();
            accounts.remove(account);
            decreaseAccIndex();
            claimTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            claimedUsingOrigin = false;
            success = account;
            claimed = true;
        }
        return claimed;
    }
    public void increaseAccIndex(){
         lock.lock();
        if(accountIndex < accounts.size() -1){
            accountIndex++;
        } else {
            accountIndex = 0;
        }
        lock.unlock();
    }
    public void decreaseAccIndex(){
        lock.lock();
        if (accountIndex != 0) {
            accountIndex--;
        }
        lock.unlock();
    }
    public void asyncSendClaimInfo(Target target, MicrosoftAccount account, String newTag, long claimTime){
        String tag = target.getGamertag();
        String email = account.requestEmail(client);
        String password = accountCredentials.findPassword(email,false);
        mail.createMsg(email + ":" + password,tag, newTag,claimTime);
        try{
            fileIO.writeToFile(fileIO.getDataDirectoryPath() + "Claims.txt", tag + ":" +email + ":" + password + " | Origin used: " + claimedUsingOrigin);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/816102227399409704/RwpGQnVp4Cfr1-MyvHayRXE459fUXyoRPFa7tNfsaOHk1PSTW__DP4CDQW_Lp0HUZRLG");
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Autoclaimer")
                .addField("***Claimed Gamertag***","``" + tag+"``",false)
                .addField("***New Gamertag***","``" + newTag+"``",false)
                .setThumbnail("https://i.pinimg.com/originals/e6/4f/90/e64f90155b938ace8665c85844b7a04e.gif")
                .setColor(Color.BLACK));
        try{
            webhook.execute();
            targetGrabber.findNewTarget(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void asyncFail(Target target, int code, String newTag){
        String tag = "";
        String oldXuid = "";
        try{
            tag = target.getGamertag();
            oldXuid = target.getXuid();
        } catch (Exception e){
        }
            DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/816102564830380032/FyJ65KxY1NV5cXoy7GunupAymW9C9RfKSy1Cp1aFyEMcqwWy906t_208d2Sft_h_LZPi");
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Swap Monitor")
                    .addField("***Gamertag Swapped***","``" + tag+"``",false)
                    .addField("***New Tag***","``" + newTag+"``",false)
                    .setThumbnail("https://media.tenor.com/images/419af3f12e62872e34326431f2c28cca/tenor.gif")
                    .setColor(Color.BLACK));
            try{
                webhook.execute();
                targetGrabber.findNewTarget(target);
                try {
                    fileIO.writeToFile(fileIO.getDataDirectoryPath() + "Attempts.log", "Gamertag: " + tag + ":" + oldXuid +  " Claim Failed | Swapped to: "
                            + newTag + " | New Target is GT: " + target.getGamertag() + ":" + target.getXuid() + " | Response code from request was: " + code);
                } catch (IOException e) {
                }
            } catch (Exception e) {
            }
            if(target.getXuid().equals(oldXuid)) {
                target.setFailedClaim(true);
            }
    }
    public boolean multiClaim(Target target,String newTag){
        Stopwatch stopwatch = Stopwatch.createStarted();
        claiming = true;
        boolean targetClaimed = false;
        ArrayList<Boolean> claimed = new ArrayList<>();
        System.out.println("\n");
        console.updateMessage("Autoclaimer", "Claiming gamertag: " + target.getGamertag());
        System.out.println("\n");
        String[] claimCombonations = target.getClaimCombonations();
        CountDownLatch latch = new CountDownLatch(claimCombonations.length);
        for(String combo : claimCombonations){
            new Thread(() -> claimed.add(claimGamertag(combo, latch,false))).start();
//            new Thread(() -> claimed.add(claimGamertag(combo, latch,true))).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        for(boolean next : claimed){
            if(next){
                stopwatch.stop();
                long totalTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                console.updateMessage("Autoclaimer", "Successfully Claimed GT: " + target.getGamertag()+  " | (Took " +claimTime + " ms) | (Claim Delay: " + (totalTime - claimTime)+")");
                System.out.println("\n");
                asyncSendClaimInfo(target, success,newTag,claimTime);
                targetClaimed = true;
            }
        }
        if(! claimed.contains(true)){
            console.printError("Failed to claim GT: " + target.getGamertag());
            System.out.println("\n");
            asyncFail(target,400, newTag);
        }
        claiming = false;
        return targetClaimed;
    }
    public boolean claim(Target target,String newTag){
        claiming = true;
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean targetClaimed = false;
        String tag = target.getGamertag();
        System.out.println("\n");
        console.updateMessage("Autoclaimer", "Claiming gamertag: " + tag);
        System.out.println("\n");
        if(claimGamertag(tag)){
            stopwatch.stop();
            long totalTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            console.updateMessage("Autoclaimer", "Successfully Claimed GT: " + target.getGamertag()+  " | (Took " +claimTime + " ms) | (Claim Delay: " + (totalTime - claimTime)+" ms)");
            System.out.println("\n");
            asyncSendClaimInfo(target, success,newTag,claimTime);
            targetClaimed = true;
        } else {
            console.printError("Failed to claim GT: " + target.getGamertag());
            System.out.println("\n");
            asyncFail(target,400, newTag);
        }
        claiming = false;
        return targetClaimed;
    }
}
