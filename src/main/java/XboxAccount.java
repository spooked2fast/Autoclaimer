import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class XboxAccount extends Account{
    private XstsToken xstsToken;
    private boolean RL = false;
    private boolean useOrigin  = false;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public XboxAccount(String email, String password){
        super(email, password, "http://xboxlive.com");
    }
    public XboxAccount(String email, String password,String refreshToken){
        super(email, password, refreshToken, "http://xboxlive.com");
    }
    public int socialCheck(String gamertag, OkHttpClient client){
        String check = gamertag;
        if(gamertag.contains(" ")){
            String[] tmp = gamertag.split(" ");
            check = tmp[0] + "%20" + tmp[1];
        }
        Request request = new Request.Builder()
                .url("https://social.xboxlive.com/users/gt("+ check+")/people/")
                .addHeader("Authorization", "XBL3.0 x=" + xstsToken.toString())
                .addHeader("x-xbl-contract-version", "2")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.code();
        } catch (Exception e){
            return 0;
        }
    }
    public boolean isRL(){
        return RL;
    }
    public int commentsRegCheck(String gamertag, OkHttpClient client, boolean useOrigin){
        String postData = "{\"rootPaths\":[\"userposts.xboxlive.com/users/gt(" + gamertag + ")/posts/2535433843908393(1)_Link.e9d072ad-7886-4f15-9659-b418614dfa28/timelines/Club/3379843461593882/comments/08585890389005819430_1504549120_2535457069083014\"]}";
        RequestBody body = RequestBody.create(JSON, postData);
        String url = "https://comments.xboxlive.com/summaries/batch";
        if(useOrigin){
            url = "https://comments.xboxlive.com/summaries/batch";
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "XBL3.0 x=" + xstsToken.toString())
                .addHeader("x-xbl-contract-version", "4")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.code();
        } catch (Exception e){
            return 0;
        }
    }
    public void setRL(boolean RL){
        this.RL = RL;
    }
    public String getXuidFromTag(String gamertag,OkHttpClient client){
        String check = gamertag;
        try{
            if(gamertag.contains(" ")){
                String[] tmp = gamertag.split(" ");
                check = tmp[0] + "%20" + tmp[1];
            }
        } catch (Exception e){

        }
        Request request = new Request.Builder()
                .url("https://profile.xboxlive.com/users/gt("+ check+")/settings")
                .addHeader("Authorization", "XBL3.0 x=" + xstsToken.toString())
                .addHeader("x-xbl-contract-version", "2")
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            if(response.code() != 200){
                return null;
            }
            return jsonResponse.substring(jsonResponse.indexOf("id\":\"") + 5, jsonResponse.indexOf("\",\"hostId\":\""));
        } catch (Exception e){
            return null;
        }
    }
    public String getXuidFromTag2(String gamertag,OkHttpClient client){
        String check = gamertag;
        if(gamertag.contains(" ")){
            String[] tmp = gamertag.split(" ");
            check = tmp[0] + "%20" + tmp[1];
        }
        Request request = new Request.Builder()
                .url("https://avatarservices.xboxlive.com/users/gt(" + check + ")/avatar/manifest")
                .addHeader("Authorization", "XBL3.0 x=" + xstsToken.toString())
                .addHeader("Host", "avatarservices.xboxlive.com")
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            int i = jsonResponse.indexOf("\"xuid\"") + 7;
            String xuid = jsonResponse.substring(i,i+16 );
            if(xuid.contains("filtered")){
                return null;
            } else {
                return xuid;
            }
        } catch (Exception e){
            return null;
        }
    }
    public void setXstsToken(XstsToken xstsToken){
        this.xstsToken = xstsToken;
    }
    public String findNewTag(Target target, OkHttpClient client){
        String xuid = target.getXuid();
        String post = "{\"userIds\":[" + xuid + "],\"settings\":[\"Gamertag\"]}";
        RequestBody body = RequestBody.create(JSON, post);
        Request request = new Request.Builder()
                .url("https://profile.xboxlive.com/users/batch/profile/settings")
                .addHeader("Authorization", "XBL3.0 x=" + xstsToken.toString())
                .addHeader("x-xbl-contract-version", "2")
                .addHeader("Content-Type","application/json")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            return jsonResponse.substring(jsonResponse.indexOf("\"value\":\"")+9, jsonResponse.indexOf("\"}]"));
        } catch (Exception e){
            return null;
        }
    }
    public String profileBatch(RequestBody body, OkHttpClient client){
        String url;
        if(useOrigin){
            useOrigin = false;
            url = "https://profile-origin.xboxlive.com/users/batch/profile/settings";
        } else {
            useOrigin = true;
            url = "https://profile.xboxlive.com/users/batch/profile/settings";
        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "XBL3.0 x=" + xstsToken.toString())
                .addHeader("x-xbl-contract-version", "2")
                .addHeader("Content-Type","application/json")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.code() != 200){
                return null;
            }
            return response.body().string();
        } catch (Exception e){
//            e.printStackTrace();
            return null;
        }
    }
    public JSONObject requestEmail(OkHttpClient client){
        Request claimRequest = new Request.Builder()
                .url("https://accounts.xboxlive.com/users/current/profile/settings")
                .addHeader("x-xbl-contract-version","2")
                .addHeader("Authorization","XBL3.0 x=" + xstsToken.toString())
                .build();
        try (Response response = client.newCall(claimRequest).execute()) {
            String jsonResponse = response.body().string();
            System.out.println("\n" + jsonResponse + "\n");
            JSONObject jsonSerial = new JSONObject(jsonResponse);
            String email = jsonSerial.getString("email");
//            String gtg = jsonSerial.getString("gamerTag");
////            String[] returnArray = new String[2];
////            returnArray[0] = email;
////            returnArray[1] =
////            return email;
            return jsonSerial;
        } catch(Exception e){
//            e.printStackTrace();
            return null;
        }
    }
}
