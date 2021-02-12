import okhttp3.OkHttpClient;

import java.util.ArrayList;

public class CheckThread extends Thread {
    private ArrayList<XboxAccount> accounts;
    private ArrayList<Target> targets;
    private volatile boolean finished = false;
    private int targetIndex = 0;
    private int accountIndex = 0;
    private int checkOpt = 0;
    private OkHttpClient client = new OkHttpClient();
    private CheckThreading master;
    private int code;
    private boolean modifyingArray = false;
    public CheckThread(ArrayList<XboxAccount> accounts, ArrayList<Target> targets,CheckThreading master){
        this.targets = targets;
        this.accounts = accounts;
        this.master = master;
    }
    public void stopSelf(){
        this.finished = true;
    }
    public void run(){
        while(! finished){
            for(XboxAccount account : accounts){
                if(!modifyingArray){
                    performCheck(account);
                }
            }
        }
    }
    public void increaseTargetIndex(){
        if(targetIndex < targets.size()-1){
            targetIndex++;
        } else {
            targetIndex = 0;
        }
    }
    public void changeCheckMethod(){
        if(checkOpt == 1){
            checkOpt = 0;
        } else {
            checkOpt++;
        }
    }
    public void performCheck(XboxAccount account){
        Target target = targets.get(targetIndex);
        if(checkOpt == 0){
            code = account.socialCheck(target.getGamertag(),this.client);
            if(code == 400){
                startAsyncClaim(target);
            }
            else if(code == 403 || code == 200){
                master.updateRequestCount(false);
                increaseTargetIndex();
            }
            else if(code == 429){
                changeCheckMethod();
                if(account.isRL()){
                    master.updateRequestCount(true);
                } else {
                    account.setRL(true);
                }
            }
        }
        else if(checkOpt == 1){
            code = account.commentsRegCheck(target.getGamertag(),this.client,false);
            if(code == 400){
                startAsyncClaim(target);
            }
            else if(code == 200){
                master.updateRequestCount(false);
                increaseTargetIndex();
            }
            else if(code == 429){
                changeCheckMethod();
                if(account.isRL()){
                    master.updateRequestCount(true);
                } else {
                    account.setRL(true);
                }
            }
        }
    }
    public void startAsyncClaim(Target target){
        if(targetIndex > 0){
            targetIndex--;
        }
        targets.remove(target);
        new Thread(new Runnable() {
            public void run() {
                master.claim(target);
                modifyingArray = true;
                targets.add(target);
                modifyingArray = false;
            }
        }).start();
    }
}
