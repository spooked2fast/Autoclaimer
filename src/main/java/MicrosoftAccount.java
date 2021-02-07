public class MicrosoftAccount extends Account{
    public MicrosoftAccount(String email, String password){
        super(email, password);
    }
    public MicrosoftAccount(String email, String password,String refreshToken){
        super(email, password, refreshToken);
    }
}
