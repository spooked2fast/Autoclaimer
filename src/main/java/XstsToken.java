public class XstsToken {
    private String userHash;
    private String accessToken;

    public XstsToken(String userHash, String accessToken, String relyingParty){
        this.userHash = userHash;
        this.accessToken = accessToken;
    }
    public String getAccessToken(){
        return accessToken;
    }
    public String getUserHash(){
        return userHash;
    }
    public String toString(){
        return userHash + ";" + accessToken; //format for request header "Authorization"
    }
}
