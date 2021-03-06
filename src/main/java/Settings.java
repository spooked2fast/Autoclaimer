public class Settings {
    private boolean tokenAccounts;
    private int authThreadCount;
    private int batchSize;
    private String senderEmail;
    private String gmailAppPassword;
    private String recipientEmail;
    private String claimWebhook;
    private String deviceToken;
    public Settings(boolean tokenAccounts, int authThreadCount, int batchSize, String senderEmail, String gmailAppPassword, String recipientEmail, String claimWebhook, String deviceToken){
        this.tokenAccounts = tokenAccounts;
        this.authThreadCount = authThreadCount;
        this.batchSize = batchSize;
        this.senderEmail = senderEmail;
        this.gmailAppPassword = gmailAppPassword;
        this.recipientEmail = recipientEmail;
        this.claimWebhook = claimWebhook;
        this.deviceToken = deviceToken;
    }
    public Settings(){}
    public boolean isTokenAccounts(){
        return tokenAccounts;
    }
    public int getAuthThreadCount(){
        return authThreadCount;
    }
    public int getBatchSize(){
        return batchSize;
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
    public String getclaimWebhook(){
        return claimWebhook;
    }
    public String getDeviceToken(){
        return deviceToken;
    }
}
