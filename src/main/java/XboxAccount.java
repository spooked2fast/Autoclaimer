public class XboxAccount extends Account{
    private String email;
    private String password;
    public XboxAccount(String email, String password){
        super(email, password);
    }
    public XboxAccount(String email, String password,String refreshToken){
        super(email, password, refreshToken);
    }
}
