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
                Target target = targets.get(targetIndex);
                if(checkOpt == 0){
                    code = account.socialCheck(target.getGamertag(),this.client);
                    if(code == 400){
                        master.printClaiming(target);
                        master.claim(target);
                    }
                    else if(code == 403 || code == 200){
                        master.updateRequestCount(false);
                        increaseTargetIndex();
                    }
                    else if(code == 429){
                        master.updateRequestCount(true);
                    }
                }
                else if(checkOpt == 1){
                    code = account.commentsRegCheck(target.getGamertag(),this.client,false);
                    if(code == 400){
                        master.printClaiming(target);
                        master.claim(target);
                    }
                    else if(code == 200){
                        master.updateRequestCount(false);
                        increaseTargetIndex();
                    }
                    else if(code == 429){
                        master.updateRequestCount(true);
                    }
                    else {
                        System.out.println(code);
                    }
                }
//                else if(checkOpt == 2){
//                    int code = account.commentsRegCheck(target.getGamertag(),this.client,false);
//                    if(code == 400){
//                        master.printClaiming(target);
//                        //Claimer.claim(target);
//                    }
//                    else if(code == 200){
//                        master.updateRequestCount(false);
//                        increaseTargetIndex();
//                    }
//                    else if(code == 429){
//                        master.updateRequestCount(true);
//                    }
//                }
                if(checkOpt < 1){
                    checkOpt++;
                } else {
                    checkOpt =0;
                }
            }
        }
    }
    public void increaseTargetIndex(){
        if(targetIndex < targets.size() -1){
            targetIndex++;
        } else {
            targetIndex = 0;
        }
    }
}
