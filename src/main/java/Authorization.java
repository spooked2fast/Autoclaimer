import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.StringTokenizer;

public class Authorization {
    private String clientIP;
    public Authorization(){
    }
    public boolean getIsAuth() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.ipify.org?format=json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            String body = response.body().string();
            clientIP = body.substring(body.indexOf(":\"")+2, body.indexOf("\"}"));
        } catch (IOException e){
            e.printStackTrace();
        }
        Request pastebin = new Request.Builder()
                .url("https://pastebin.com/raw/Gz8zeGG0")
                .build();
        try (Response response = client.newCall(pastebin).execute()) {
            assert response.body() != null;
            String body = response.body().string();
            StringTokenizer st = new StringTokenizer(body);
            while(st.hasMoreElements()){
                if(st.nextElement().equals(clientIP)){
                    return true;
                }
            }
        } catch(Exception e){
//            getIsAuth();
        }
        return false;
    }
}
