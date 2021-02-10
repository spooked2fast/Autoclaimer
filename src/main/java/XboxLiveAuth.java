import net.dongliu.requests.Header;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Session;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class XboxLiveAuth {
    private final OkHttpClient client = new OkHttpClient();
    private Session session = Requests.session();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String loginURL = "https://login.live.com/oauth20_authorize.srf?display=touch&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&locale=en&response_type=token&client_id=0000000048093EE3";
    public XboxLiveAuth(){
    }
    public String login(String email, String password) {
        try{
            String urlPost;
            String sFTTag;
            String resp = session.get(loginURL).send().readToText();
            urlPost = resp.substring(resp.indexOf("urlPost:'") + 9,resp.indexOf("',iUXMode"));
            sFTTag = resp.substring(resp.indexOf("id=\"i0327\" value=\"") + 18,resp.indexOf("\"/>',BQ:"));
            String postData = "i13=1&login="+URLEncoder.encode(email,StandardCharsets.UTF_8)+"&loginfmt="+URLEncoder.encode(email,StandardCharsets.UTF_8)+"&type=11&LoginOptions=1&lrt=&lrtPartition=&hisRegion=&hisScaleUnit=&passwd=" + URLEncoder.encode(password,StandardCharsets.UTF_8) + "&ps=2&psRNGCDefaultType=&psRNGCEntropy=&psRNGCSLK=&canary=&ctx=&hpgrequestid=&PPFT=" + URLEncoder.encode(sFTTag,StandardCharsets.UTF_8) + "&PPSX=Passp&NewUser=1&FoundMSAs=&fspost=0&i21=0&CookieDisclosure=0&IsFidoSupported=1&isSignupPost=0&i2=39&i17=0&i18=&i19=4128";
            RawResponse resp2 = session.post(urlPost)
                    .body(postData)
                    .headers(new Header("Content-Type", "application/x-www-form-urlencoded"))
                    .followRedirect(false)
                    .send();
            String locationHeader = resp2.getHeader("Location");
            String accessToken = locationHeader.substring(locationHeader.indexOf("access_token=") + 13, locationHeader.indexOf("&"));
            String refreshToken  = postJWTfromRPS(accessToken);
            return refreshToken;
        } catch (Exception e){
            return null;
        }
    }
    public String postJWTfromRPS(String RPS){
        String json = getJWTAuthJSON(RPS);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("https://user.auth.xboxlive.com/user/authenticate")
                .addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; Zoom 3.6.0)")
                .addHeader("Host", "beta-user.auth.xboxlive.com")
                .addHeader("Connection", " Keep-Alive")
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            String token = jsonResponse.substring( jsonResponse.indexOf("\"Token\":\"") + 9, jsonResponse.indexOf("DisplayClaims") -3);
            return token;
        } catch (Exception e){
            return null;
        }
    }
    public String getJWTAuthJSON(String RPS){
        return "{'RelyingParty':'http://auth.xboxlive.com','TokenType':'JWT','Properties':{'AuthMethod':'RPS','SiteName':'user.auth.xboxlive.com','RpsTicket':'" + RPS + "'}}";
    }
    public XstsToken getXstsToken(Account account){
        if(account.getXstsRelyingParty().contains("accounts")){
            if(! isValidMicrosoft(account))
                return null;
        }
        String refreshToken = account.getRefreshToken();
        String json = "{'RelyingParty':'" + account.getXstsRelyingParty() +"','TokenType':'JWT','Properties':{'UserTokens':['" + refreshToken+ "'],'SandboxId':'RETAIL'}}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("https://xsts.auth.xboxlive.com/xsts/authorize")
                .addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; Zoom 3.6.0)")
                .addHeader("Host", " xsts.auth.xboxlive.com")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String token = jsonObject.getString("Token");
            JSONArray xui =jsonObject.getJSONObject("DisplayClaims").getJSONArray("xui");
            String uhs = xui.getJSONObject(0).getString("uhs");
            XstsToken xstsToken = new XstsToken(uhs,token,account.getXstsRelyingParty());
            return xstsToken;
        } catch (Exception e){
            return null;
        }
    }
    public boolean isValidMicrosoft(Account account){
        String refreshToken = account.getRefreshToken();
        String json = "{'RelyingParty':'http://xboxlive.com','TokenType':'JWT','Properties':{'UserTokens':['" + refreshToken+ "'],'SandboxId':'RETAIL'}}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("https://xsts.auth.xboxlive.com/xsts/authorize")
                .addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; Zoom 3.6.0)")
                .addHeader("Host", " xsts.auth.xboxlive.com")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.code() == 200){
                return false;
            } else {
                return true;
            }
        } catch (Exception e){
            return false;
        }
    }
}
