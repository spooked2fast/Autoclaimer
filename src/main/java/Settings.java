public class Settings {
    private boolean tokenAccounts;
    private int authThreadCount;
    private String senderEmail;
    private String gmailAppPassword;
    private String recipientEmail;
    private String discordWebhook;
    public Settings(boolean tokenAccounts, int authThreadCount, String senderEmail, String gmailAppPassword, String recipientEmail, String discordWebhook){
        this.tokenAccounts = tokenAccounts;
        this.authThreadCount = authThreadCount;
        this.senderEmail = senderEmail;
        this.gmailAppPassword = gmailAppPassword;
        this.recipientEmail = recipientEmail;
        this.discordWebhook = discordWebhook;
    }
    public Settings(){}
    public boolean isTokenAccounts(){
        return tokenAccounts;
    }
    public int getAuthThreadCount(){
        return authThreadCount;
    }
    public String getSenderEmail(){
        return senderEmail;
    }
    public String getGmailAppPassword(){
        return gmailAppPassword;
    }
    public String getRecipientEmail(){
        return recipientEmail;
    }
    public String getDiscordWebhook(){
        return discordWebhook;
    }
}
