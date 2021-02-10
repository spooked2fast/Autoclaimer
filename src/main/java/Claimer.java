import okhttp3.OkHttpClient;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Claimer {
    private OkHttpClient client = new OkHttpClient();
    private ArrayList<MicrosoftAccount> accounts;
    private int accountIndex = 0;
    private Console console;
    private FileIO fileIO = new FileIO();
    Claimer(ArrayList<MicrosoftAccount> accounts, Console console){
        this.console = console;
        this.accounts = accounts;
    }
    public void claimGamertag(Target target){
        MicrosoftAccount account = accounts.get(accountIndex);
        int responseCode = account.sendTroublePost(target.getGamertag(),this.client);
        if(responseCode == 200){
            System.out.println("\n");
            console.updateMessage("Autoclaimer", "Successfully Claimed GT: " + target.getGamertag());
            System.out.println("\n");
        } else {
            System.out.println("\n");
            console.printError("Failed to claim GT: " + target.getGamertag() + " Error code: " + responseCode);
            System.out.println("\n");
        }
    }
    public void increaseAccIndex(){
        if(accountIndex < accounts.size() -1){
            accountIndex++;
        } else {
            accountIndex = 0;
        }
    }
    public void asyncSendClaimInfo(String tag, MicrosoftAccount account){
        String email = account.getEmail();
        String password = account.getPassword();
        try{
            fileIO.writeToFile(fileIO.getDataDirectoryPath() + "Claims.txt", tag + ":" +email + ":" + password);
        } catch (IOException e){

        }
        DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/802709248542507045/vIcLoRwSsvUuAuOBlgfKmfgmUBjJXb-7Mh8Zak_HRZVPKuc9VsBLqmq49h86PZD4kO92");
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Autoclaimer")
                .addField("***Claimed Gamertag***","``" + tag+"``",false)
                .addField("***Claimed Gamertag***","``" + tag+"``",false)
                .setThumbnail("https://i.pinimg.com/originals/24/5a/ed/245aed9521978839b8e16098c0f7c446.gif")
                .setColor(Color.BLACK));
        try{
            webhook.execute(); //Handle exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
