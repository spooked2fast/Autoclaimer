import okhttp3.OkHttpClient;

import java.util.concurrent.CountDownLatch;

public class ValidateTargetThread extends Thread{
    private TargetGrabber master;
    private String target;
    private volatile boolean finished = false;
    private OkHttpClient client = new OkHttpClient();
    private CountDownLatch latch;
    private XboxAccount account;
    private int retries = 0;
    public ValidateTargetThread(TargetGrabber master, String target, CountDownLatch latch, boolean fail){
        this.latch = latch;
        this.target = target;
        this.master = master;
    }
    public void stopSelf(){
        finished = true;
    }
    public void run(){
        while(! finished) {
            this.account = master.getNewAccount();
//            int code = account.socialCheck(target, client);
//            if(code == 200 || code ==403){
            String xuid = account.getXuidFromTag(target, client);
            if (xuid != null) {
//                    Target newTarget = new Target(target,xuid);
//                    master.setValidatedTargets(newTarget,fail);
                master.addXuidToList(xuid);
                master.updateConsole();
            }
//        }
            latch.countDown();
            stopSelf();
        }
    }
    public String getXuid(String target, OkHttpClient client){
        int retries = 0;
        this.account = master.getNewAccount();
        String xuid = account.getXuidFromTag(target, client);
        if(xuid==null){
            if(retries < 3){
                retries ++;
                getXuid(target, client);
            } else {
                return null;
            }
        } else {
            return xuid;
        }
        return null;
    }
}
