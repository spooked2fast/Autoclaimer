import com.google.common.base.Stopwatch;
import okhttp3.OkHttpClient;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Claimer {
    private OkHttpClient client = new OkHttpClient();
    private ArrayList<MicrosoftAccount> accounts;
    private int accountIndex = 0;
    private Console console;
    private FileIO fileIO = new FileIO();
    private Mail mail;
    private Settings settings;
    private AccountCredentials accountCredentials;
    private TargetGrabber targetGrabber;
    private boolean claiming = false;
    Claimer(ArrayList<MicrosoftAccount> accounts, Console console, Settings settings, AccountCredentials accountCredentials){
        this.accountCredentials = accountCredentials;
        this.settings = settings;
        this.mail = new Mail(settings);
        this.console = console;
        this.accounts = accounts;
        this.targetGrabber = new TargetGrabber(accountCredentials, console);
    }
    public boolean isClaiming(){
        return claiming;
    }
    public boolean claimGamertag(Target target){
        boolean claimed = false;
        Stopwatch stopwatch = Stopwatch.createStarted();
        claiming = true;
        System.out.println("\n");
        console.updateMessage("Autoclaimer", "Claiming gamertag: " + target.getGamertag());
        System.out.println("\n");
        int indexUsed = accountIndex;
        accountIndex++;
        MicrosoftAccount account = accounts.get(indexUsed);
        int responseCode = account.sendTroublePost(target.getClaimString(),this.client,true);
        if(responseCode == 200){
            stopwatch.stop();
            accounts.remove(account);
            if(accountIndex != 0){
                accountIndex--;
            }
            long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            console.updateMessage("Autoclaimer", "Successfully Claimed GT: " + target.getGamertag()+  " | (Took " +time + " ms)");
            System.out.println("\n");
            asyncSendClaimInfo(target, account);
            claimed = true;
        } else {
            console.printError("Failed to claim GT: " + target.getGamertag() + " Error code: " + responseCode);
            System.out.println("\n");
            asyncFail(target, responseCode);
        }
        increaseAccIndex();
        claiming = false;
        return claimed;
    }
    public void increaseAccIndex(){
        if(accountIndex < accounts.size() -1){
            accountIndex++;
        } else {
            accountIndex = 0;
        }
    }
    public void asyncSendClaimInfo(Target target, MicrosoftAccount account){
        String tag = target.getGamertag();
        String email = account.requestEmail(client);
        String password = accountCredentials.findPassword(email,false);
        String newTag = targetGrabber.getNewTag(target);
        try{
            fileIO.writeToFile(fileIO.getDataDirectoryPath() + "Claims.txt", tag + ":" +email + ":" + password);
        } catch (IOException e){

        }
        DiscordWebhook webhook = new DiscordWebhook(settings.getDiscordWebhook());
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Autoclaimer")
                .addField("***Claimed Gamertag***","``" + tag+"``",false)
                .addField("***New Gamertag***","``" + newTag+"``",false)
                .setThumbnail("https://i.pinimg.com/originals/24/5a/ed/245aed9521978839b8e16098c0f7c446.gif")
                .setColor(Color.BLACK));
        try{
            webhook.execute();
        } catch (Exception e) {
        }
        mail.createMsg(email + ":" + password,tag);
    }
    public void asyncFail(Target target, int code){
        String tag = "";
        String oldXuid = "";
        String newXuid = "";
        String newTag = "";
        try{
            tag = target.getGamertag();
            oldXuid = target.getXuid();
            newTag = targetGrabber.getNewTag(target);
            Target newTarget = targetGrabber.findNewTarget(target);
            target.setGamertag(newTarget.getGamertag());
            target.setXuid(newTarget.getXuid());
            newXuid = target.getXuid();
        } catch (Exception e){
            e.printStackTrace();
        }
        if(! (newXuid.equals(oldXuid) && newTag == null)){
            DiscordWebhook webhook = new DiscordWebhook(settings.getDiscordWebhook());
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Autoclaimer")
                    .addField("***Gamertag Swapped***","``" + tag+"``",false)
                    .addField("***New Gamertag***","``" + newTag+"``",false)
                    .setThumbnail("https://i.pinimg.com/originals/24/5a/ed/245aed9521978839b8e16098c0f7c446.gif")
                    .setColor(Color.BLACK));
            try{
                webhook.execute();
                try {
                    fileIO.writeToFile("Attempts.log", "Gamertag: " + tag + ":" + oldXuid +  " Claim Failed | Swapped to: "
                            + newTag + " | New Target is GT: " + target.getGamertag() + ":" + target.getXuid() + " | Response code from request was: " + code);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public boolean multiClaim(Target target){
        String[] claimCombonations = target.getClaimCombonations();
        CountDownLatch latch = new CountDownLatch(claimCombonations.length);
        for(String combo : claimCombonations){
            new Thread(new Runnable() {
                public void run() {
                    claim
                }
            }).start();
        }
    }
}
