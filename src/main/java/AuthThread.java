import java.util.concurrent.CountDownLatch;

public class AuthThread extends Thread{
    private String email;
    private String password;
    private volatile boolean finished;
    private final XboxLiveAuth xboxLiveAuth;
    private final AuthThreading master;
    private final boolean createXbox;
    private final CountDownLatch latch;
    private String refreshToken;
    private final Settings settings;
    public AuthThread(String email , String password, AuthThreading master, boolean createXbox, CountDownLatch latch, Settings settings){
        this.settings = settings;
        this.createXbox = createXbox;
        this.email = email;
        this.password = password;
        this.master = master;
        this.latch = latch;
        this.xboxLiveAuth = new XboxLiveAuth(this.settings);
    }
    public AuthThread(String refreshToken, AuthThreading master, boolean createXbox, CountDownLatch latch, Settings settings){
        this.settings = settings;
        this.refreshToken = refreshToken;
        this.createXbox = createXbox;
        this.master = master;
        this.latch = latch;
        this.xboxLiveAuth = new XboxLiveAuth(this.settings);
    }
    public void stopSelf(){
        finished = true;
    }
    public void run(){
        while(! finished){
            if(refreshToken ==null)
            getRefreshFromAccount();
            else{
                getXsts();
                latch.countDown();
                stopSelf();
            }
        }
    }
    public void getRefreshFromAccount(){
        refreshToken = xboxLiveAuth.login(email,password);
        if(refreshToken != null) {
            master.writeToken(refreshToken,createXbox);
            getXsts();
        }
        master.increaseCount();
        latch.countDown();
        stopSelf();
    }
    public void getXsts(){
        if (createXbox) {
            XboxAccount account = new XboxAccount(email, password, refreshToken);
            XstsToken xstsToken = xboxLiveAuth.getXstsToken(account);
            if (xstsToken != null) {
                account.setXstsToken(xstsToken);
                master.addXboxAccount(account);
            }
        } else {
            MicrosoftAccount account = new MicrosoftAccount(email, password, refreshToken);
            XstsToken xstsToken = xboxLiveAuth.getXstsToken(account);
            if (xstsToken != null) {
                account.setXstsToken(xstsToken);
                master.addMicrosoftAccount(account);
            }
        }
    }
}
