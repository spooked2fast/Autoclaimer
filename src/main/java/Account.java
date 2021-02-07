public abstract class Account {
    private String email;
    private String password;
    private String refreshToken;
    public Account(String email, String password){
        this.email = email;
        this.password = password;
    }
    public Account(String email, String password,String refreshToken){
        this.email = email;
        this.password = password;
        this.refreshToken = refreshToken;
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
}
