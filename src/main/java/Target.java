public class Target {
    private String gamertag;
    private String xuid;
    public Target(String gamertag, String xuid){
        this.gamertag = gamertag;
        this.xuid = xuid;
    }
    public Target(String gamertag){
        this.gamertag = gamertag;
    }
    public void setXuid(){
        this.xuid = xuid;
    }
    public String getGamertag(){
        return gamertag;
    }
    public String getXuid(){
        return xuid;
    }
}
