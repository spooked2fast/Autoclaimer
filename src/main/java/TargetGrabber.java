import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class TargetGrabber {
    private final FileIO fileIO = new FileIO();

    private final ArrayList<Target> validatedTargets = new ArrayList<>();

    private final ArrayList<String> uncheckedTargets;

    private final ArrayList<XboxAccount> accounts;

    private final AccountCredentials accountCredentials;

    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final ThreadPoolExecutor executor;

    private CountDownLatch latch;

    private final Console console;

    private int accountIndex;

    private final ReentrantLock lock = new ReentrantLock();

    private ArrayList<String> xuids = new ArrayList<>();

    private ArrayList<String> allTags = new ArrayList<>();

    public TargetGrabber(AccountCredentials accountCredentials, Console console) {
        this.uncheckedTargets = this.fileIO.fileContentsToList(this.fileIO.getDataDirectoryPath() + "Gamertags.txt");
        this.accountCredentials = accountCredentials;
        this.accounts = accountCredentials.getXboxAccounts();
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        this.console = console;
    }

    public void refineTargets(){
        ArrayList<String> tagsNoDupes = fileIO.removeDuplicates(uncheckedTargets);
        //            String combo1 = tag.charAt(0)+ " "+ tag.substring(1);
        //            String combo2 = tag.substring(0,tag.length()-1)+ " "+ tag.substring(tag.length()-1);
        //            allTagCombos.add(combo1);
        //            allTagCombos.add(combo2);
        ArrayList<String> allTagCombos = new ArrayList<>(tagsNoDupes);
        allTags = fileIO.removeDuplicates(allTagCombos);
    }


    public void createAndRunTasks() {
        refineTargets();
        this.fileIO.clearFile("Filtered.txt");
        this.latch = new CountDownLatch(allTags.size());
        this.accountIndex = 0;
        for (String allTag : allTags) {
            ValidateTargetThread validateTargetThread = new ValidateTargetThread(this, allTag, this.latch, false);
            this.executor.execute(validateTargetThread);
        }
        try {
            this.latch.await();
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        this.executor.shutdown();
        OkHttpClient client = new OkHttpClient();
        int sublistAmount = this.xuids.size() / 200;
        int xuidArrayIndex = 0;
        XboxAccount account = getNewAccount();
        for (int j = 0; j < sublistAmount; j++) {
            String str2 = "";
            try {
                ArrayList<String> arrayList = new ArrayList<>();
                for (int m = 0; m < 200; m++)
                    arrayList.add(this.xuids.get(m + xuidArrayIndex));
                String str1 = buildBatchPost(arrayList);
                RequestBody requestBody = RequestBody.create(JSON, str1);
                str2 = account.profileBatch(requestBody, client);
            } catch (Exception ignored){
            }
                try{
                    JSONObject obj2 = new JSONObject(str2);
                    JSONArray profileUsers = obj2.getJSONArray("profileUsers");
                    for (int index = 0; index < profileUsers.length(); index++) {
                        JSONObject jsonObj = profileUsers.getJSONObject(index);
                        JSONArray settings = jsonObj.getJSONArray("settings");
                        String value = settings.getJSONObject(0).getString("value");
                        Target target = new Target(value, this.xuids.get(index + xuidArrayIndex));
                        this.validatedTargets.add(target);
                    }
                    xuidArrayIndex += 200;
                    account = getNewAccount();
                }catch (Exception e){
                    e.printStackTrace();
                }
        }
        int deltaXuids = this.xuids.size() - xuidArrayIndex;
        ArrayList<String> xuidSublist = new ArrayList<>();
        for (int k = 0; k < deltaXuids; k++)
            xuidSublist.add(this.xuids.get(k + xuidArrayIndex));
        String batchPost = buildBatchPost(xuidSublist);
        RequestBody body = RequestBody.create(JSON, batchPost);
        String response = account.profileBatch(body, client);
            try{
                JSONObject obj2 = new JSONObject(response);
                JSONArray profileUsers = obj2.getJSONArray("profileUsers");
                for (int index = 0; index < profileUsers.length(); index++) {
                    JSONObject jsonObj = profileUsers.getJSONObject(index);
                    JSONArray settings = jsonObj.getJSONArray("settings");
                    String value = settings.getJSONObject(0).getString("value");
                    Target target = new Target(value, this.xuids.get(index + xuidArrayIndex));
                    this.validatedTargets.add(target);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        for (Target target : this.validatedTargets)
            writeTagFiltered(target.getGamertag() + ":" + target.getXuid());
        executor.shutdown();
        System.out.print("\r");
        this.console.updateMessage("*", "Targets Found: (" + this.validatedTargets.size() + ")");
    }

    public XboxAccount getRandomAccount() {
        return this.accountCredentials.getRandomAccount();
    }

    public void setValidatedTargets(Target target, boolean fail) {
        this.lock.lock();
        this.validatedTargets.add(target);
        if (!fail) {
            writeTagFiltered(target.getGamertag() + ":" + target.getGamertag());
            this.console.updateTargetHeader(this.validatedTargets.size());
        }
        this.lock.unlock();
    }

    public synchronized void updateConsole() {
        this.console.updateTargetHeader(this.xuids.size());
    }

    public ArrayList<Target> getTargets() {
        return this.validatedTargets;
    }

    public void writeTagFiltered(String tag) {
        try {
            this.fileIO.writeToFile(this.fileIO.getDataDirectoryPath() + "Filtered.txt", tag);
        } catch (IOException ignored) {}
    }

    public String getNewTag(Target target) {
        XboxAccount account = getRandomAccount();
        OkHttpClient client = new OkHttpClient();
        return account.findNewTag(target, client);
    }

    public void findNewTarget(Target target) {
        this.validatedTargets.clear();
        this.allTags.clear();
        this.xuids.clear();
        String tag = target.getGamertag();
        if (tag.contains(" ")) {
            String[] tagSplit = tag.split(" ");
            tag = tagSplit[0] + tagSplit[0];
        }
        String combo1 = tag.charAt(0)+ " "+ tag.substring(1);
        String combo2 = tag.substring(0,tag.length()-1)+ " "+ tag.substring(tag.length()-1);
        allTags.add(combo1);
        allTags.add(combo2);
        allTags.add(tag);
        this.fileIO.clearFile("Filtered.txt");
        this.latch = new CountDownLatch(allTags.size());
        this.accountIndex = 0;
        for (String allTag : allTags) {
            ValidateTargetThread validateTargetThread = new ValidateTargetThread(this, allTag, this.latch, false);
            this.executor.execute(validateTargetThread);
        }
        try {
            this.latch.await();
            executor.shutdown();
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        this.executor.shutdown();
        OkHttpClient client = new OkHttpClient();
        this.xuids = fileIO.removeDuplicates(xuids);
        int sublistAmount = this.xuids.size() / 200;
        int xuidArrayIndex = 0;
        XboxAccount account = getNewAccount();
        for (int j = 0; j < sublistAmount; j++) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int m = 0; m < 200; m++)
                arrayList.add(this.xuids.get(m + xuidArrayIndex));
            String str1 = buildBatchPost(arrayList);
            RequestBody requestBody = RequestBody.create(JSON, str1);
            String str2 = account.profileBatch(requestBody, client);
            if (str2 == null) {
                str2 = account.profileBatch(requestBody, client);
            } else {
                try{
                    JSONObject obj2 = new JSONObject(str2);
                    JSONArray profileUsers = obj2.getJSONArray("profileUsers");
                    for (int index = 0; index < profileUsers.length(); index++) {
                        JSONObject jsonObj = profileUsers.getJSONObject(index);
                        JSONArray settings = jsonObj.getJSONArray("settings");
                        String value = settings.getJSONObject(0).getString("value");
                        Target newTarget = new Target(value, this.xuids.get(index + xuidArrayIndex));
                        this.validatedTargets.add(newTarget);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            xuidArrayIndex += 200;
            account = getNewAccount();
        }
        int deltaXuids = this.xuids.size() - xuidArrayIndex;
        ArrayList<String> xuidSublist = new ArrayList<>();
        for (int k = 0; k < deltaXuids; k++)
            xuidSublist.add(this.xuids.get(k + xuidArrayIndex));
        String batchPost = buildBatchPost(xuidSublist);
        RequestBody body = RequestBody.create(JSON, batchPost);
        String response = account.profileBatch(body, client);
        if (response == null) {
            response = account.profileBatch(body, client);
        } else {
            try{
                JSONObject obj2 = new JSONObject(response);
                JSONArray profileUsers = obj2.getJSONArray("profileUsers");
                for (int index = 0; index < profileUsers.length(); index++) {
                    JSONObject jsonObj = profileUsers.getJSONObject(index);
                    JSONArray settings = jsonObj.getJSONArray("settings");
                    String value = settings.getJSONObject(0).getString("value");
                    Target newtar = new Target(value, this.xuids.get(index + xuidArrayIndex));
                    this.validatedTargets.add(newtar);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        Target newTarget = validatedTargets.get(0);
        target.setGamertag(newTarget.getGamertag());
        target.setXuid(newTarget.getXuid());
//
//        OkHttpClient client = new OkHttpClient();
//        XboxAccount account = getNewAccount();
//        String xuid = account.getXuidFromTag(tag, client);
//        if (xuid == null) {
//            account = getNewAccount();
//            xuid = account.getXuidFromTag(tag, client);
//        }
//        Target newTarget = new Target(tag, xuid);
//        return newTarget;
    }

    public synchronized XboxAccount getNewAccount() {
        XboxAccount account = this.accounts.get(this.accountIndex);
        if (this.accountIndex < this.accounts.size() - 2) {
            this.accountIndex++;
        } else {
            this.accountIndex = 0;
        }
        return account;
    }

    public synchronized void addXuidToList(String xuid) {
        this.xuids.add(xuid);
    }

    public String buildBatchPost(ArrayList<String> xuidList){
        StringBuilder post = new StringBuilder("{\"userIds\":[");
        for(String xuid : xuidList){
            post.append(xuid).append(",");
        }
        post.append("],\"settings\":[\"Gamertag\"]}");
        return post.toString();
    }
}