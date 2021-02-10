public class Account {
    private String email;
    private String password;
    private String xstsRelyingParty;
    private String refreshToken;
    private XstsToken xstsToken;
    public Account(String email, String password, String xstsRelyingParty){
        this.email = email;
        this.password = password;
        this.xstsRelyingParty = xstsRelyingParty;
    }
    public Account(String email, String password,String refreshToken,String xstsRelyingParty){
        this.email = email;
        this.password = password;
        this.refreshToken = refreshToken;
        this.xstsRelyingParty = xstsRelyingParty;
    }
    public String getRefreshToken(){
        return refreshToken;
    }
    public String getEmail(){
        return email;
    }
    public String getPassword(){
        return password;
    }
    public void setRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }
    public String getXstsRelyingParty(){
        return xstsRelyingParty;
    }
}
