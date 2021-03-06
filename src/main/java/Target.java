public class Target {
    private String gamertag;
    private String xuid;
    private String claimString;
    private String[] claimCombonations = new String[3];
    private boolean failedClaim;
    public Target(String gamertag, String xuid){
        this.gamertag = gamertag;
        initializeClaimCombonations(gamertag);
        this.claimString = newClaimValue(this.gamertag);
        this.xuid = xuid;
    }
    public void setFailedClaim(boolean failedClaim){
        this.failedClaim = failedClaim;
    }
     public boolean isFailedClaim(){
        return this.failedClaim;
     }
    public Target(String gamertag){
        this.gamertag = gamertag;
        this.claimString = newClaimValue(this.gamertag);
        initializeClaimCombonations(gamertag);
    }
    public void setXuid(String xuid){
        this.xuid = xuid;
    }
    public String getGamertag(){
        return gamertag;
    }
    public String getXuid(){
        return xuid;
    }
    public void setGamertag(String gamertag){
        this.gamertag = gamertag;
        this.claimString = newClaimValue(this.gamertag);
    }
    public String getClaimString(){
        return claimString;
    }
    public String[] getClaimCombonations(){
        return claimCombonations;
    }
    public String toString(){
        return gamertag + ":" + xuid;
    }
    private String newClaimValue(String gt){
        if( ! gt.contains(" ")){
            return gt.charAt(0)+ " "+ gt.substring(1);
        } else {
            String[] tagSplit = gt.split(" ");
            String unspaced = tagSplit[0] + tagSplit[1];
            return unspaced.substring(0,unspaced.length()-1)+ " "+ unspaced.substring(unspaced.length()-1);
        }
    }
    public void initializeClaimCombonations(String gt){
        if( ! gt.contains(" ")){
            claimCombonations[2] = gt.charAt(0)+ " "+ gt.substring(1);
            claimCombonations[1] = gt.substring(0,gt.length()-1)+ " "+ gt.substring(gt.length()-1);
            claimCombonations[0] = gt;
        } else {
            String[] tagSplit = gt.split(" ");
            String unspaced = tagSplit[0] + tagSplit[1];
            claimCombonations[2] = unspaced.substring(0,unspaced.length()-1)+ " "+ unspaced.substring(unspaced.length()-1);
            claimCombonations[1] = unspaced.charAt(0)+ " "+ unspaced.substring(1);
            claimCombonations[0] = unspaced;
        }
    }
}
