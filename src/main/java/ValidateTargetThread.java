import okhttp3.OkHttpClient;

import java.util.concurrent.CountDownLatch;

public class ValidateTargetThread extends Thread{
    private TargetGrabber master;
    private String target;
    private volatile boolean finished = false;
    private OkHttpClient client = new OkHttpClient();
    private CountDownLatch latch;
    private XboxAccount account;
    private boolean fail;
    public ValidateTargetThread(TargetGrabber master, String target, CountDownLatch latch, XboxAccount account, boolean fail){
        this.account = account;
        this.latch = latch;
        this.target = target;
        this.master = master;
        this.fail = fail;
    }
    public void stopSelf(){
        finished = true;
    }
    public void run(){
        while(! finished){
            int code = account.socialCheck(target, client);
            if(code == 200 || code ==403){
                String xuid = account.getXuidFromTag(target, client);
                Target newTarget = new Target(target,xuid);
                master.setValidatedTargets(newTarget,fail);
            }
            latch.countDown();
            stopSelf();
        }
    }
}
