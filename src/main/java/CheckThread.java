import com.google.common.base.Stopwatch;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CheckThread extends Thread {
    private final ArrayList<XboxAccount> accounts;
    private final ArrayList<Target> targets;
    private volatile boolean finished = false;
    private int targetIndex = 0;
    private int checkOpt = 0;
    private final OkHttpClient client = new OkHttpClient();
    private final CheckThreading master;
    private final ArrayList<String> expectedGamertagSequence = new ArrayList<>();
    private final ArrayList<String> xuidSequence = new ArrayList<>();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private RequestBody batchPostData;
    private final boolean modifyingArray = false;
    private final boolean singleCheck;
    public CheckThread(ArrayList<XboxAccount> accounts, ArrayList<Target> targets,CheckThreading master){
        this.targets = targets;
        this.accounts = accounts;
        this.master = master;
        setExpectedGamertagArray();
        String rawPostData = buildBatchPost();
        setBatchPostData(rawPostData);
        this.singleCheck = false;
    }
    public void stopSelf(){
        this.finished = true;
    }
    public void run(){
        while(! finished){
                for(XboxAccount account : accounts){
                    if(!modifyingArray){
                        if(singleCheck){
                            performCheck(account);

                        } else {
                            performBatchCheck(account);
                        }
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
        Target target;
        if(targets.size() > 0 && ! modifyingArray){
            target = targets.get(targetIndex);
        } else {
            return;
        }
        int code;
        if(checkOpt == 0){
            code = account.socialCheck(target.getGamertag(),this.client);
            if(code == 400){
            }
            else if(code == 403 || code == 200){
                master.updateRequestCount(false);
                increaseTargetIndex();
            }
            else if(code == 429){
                if(account.isRL()){
                    changeCheckMethod();
                    master.updateRequestCount(true);
                } else {
                    account.setRL(true);
                }
            }
        }
        else if(checkOpt == 1){
            code = account.commentsRegCheck(target.getGamertag(),this.client,false);
            if(code == 400){
            }
            else if(code == 200){
                master.updateRequestCount(false);
                increaseTargetIndex();
            }
            else if(code == 429){
                if(account.isRL()){
                    changeCheckMethod();
                    master.updateRequestCount(true);
                } else {
                    account.setRL(true);
                }
            }
        }
    }
public void setBatchPostData(String batchPostData){
        this.batchPostData = RequestBody.create(JSON, batchPostData);
    }
    public void performBatchCheck(XboxAccount account) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String response = account.profileBatch(batchPostData,client);
        if(response == null){
                master.updateRequestCount(true);
                return;
        }
        try{
            JSONObject obj2 = new JSONObject(response);
            JSONArray profileUsers = obj2.getJSONArray("profileUsers");
            for (int i = 0; i < profileUsers.length(); i++)
            {
                Target target = targets.get(i);
                JSONObject jsonObj = profileUsers.getJSONObject(i);
                JSONArray settings = jsonObj.getJSONArray("settings");
                String value = settings.getJSONObject(0).getString("value");
                if(! target.getGamertag().equalsIgnoreCase(value)) {
                    if(! target.isFailedClaim())
                    master.claim(target,value);
                    xuidSequence.set(i,target.getXuid());
                    String newPost = buildBatchPost();
                    setBatchPostData(newPost);
                    return;
                }
            }
            stopwatch.stop();
            long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            master.updateRequestCount(false,time);
        }catch (Exception e){
            master.updateRequestCount(true);
        }
    }
    public void setExpectedGamertagArray(){
        for(Target target : targets){
            this.xuidSequence.add(target.getXuid());
            this.expectedGamertagSequence.add(target.getXuid());
        }
    }
    public String buildBatchPost(){
        StringBuilder post = new StringBuilder("{\"userIds\":[");
        for(String xuid : xuidSequence){
            post.append(xuid).append(",");
        }
        post.append("],\"settings\":[\"Gamertag\"]}");
        return post.toString();
    }
}
