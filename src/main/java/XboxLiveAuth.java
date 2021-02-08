import net.dongliu.requests.Header;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Session;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


//public class AddCookiesInterceptor implements Interceptor {
//
//    @Override
//    public Response intercept(Chain chain) throws IOException {
//        Request.Builder builder = chain.request().newBuilder();
//        HashSet<String> preferences = (HashSet) Preferences.getDefaultPreferences().getStringSet(Preferences.PREF_COOKIES, new HashSet<>());
//        for (String cookie : preferences) {
//            builder.addHeader("Cookie", cookie);
//            Log.v("OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
//        }
//
//        return chain.proceed(builder.build());
//    }
//}

public class XboxLiveAuth {
  //  private Proxy proxyTest = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1", 8888));
//    private OkHttpClient client = new OkHttpClient().newBuilder()
    private final OkHttpClient client = new OkHttpClient();
    private Session session = Requests.session();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String loginURL = "https://login.live.com/oauth20_authorize.srf?display=touch&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&locale=en&response_type=token&client_id=0000000048093EE3";
    public XboxLiveAuth(){
    }
    public String login(String email, String password) {
        String urlPost = "";
        String sFTTag = "";
        Request getPage = new Request.Builder()
                .url(loginURL)
                .build();

        HashSet<String> cookies = new HashSet<>();

//        try(Response response = client.newCall(getPage).execute()){
//            String body = response.body().string();
//            urlPost = body.substring(body.indexOf("urlPost:'") + 9,body.indexOf("',iUXMode"));
//            sFTTag = body.substring(body.indexOf("id=\"i0327\" value=\"") + 18,body.indexOf("\"/>',BQ:"));
//            System.out.println(urlPost + "\n" + sFTTag);
//            for (String header : response.headers("Set-Cookie")) {
//                cookies.add(header);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
        String resp = session.get(loginURL).send().readToText();
        urlPost = resp.substring(resp.indexOf("urlPost:'") + 9,resp.indexOf("',iUXMode"));
        sFTTag = resp.substring(resp.indexOf("id=\"i0327\" value=\"") + 18,resp.indexOf("\"/>',BQ:"));
//        System.out.println(urlPost + "\n" + sFTTag);
//        String postData =
//                "{'login': '" + email + "'," +
//                "'passwd': '" + password + "'," +
//                "'PPFT': '" + sFTTag + "'," +
//                "'PPSX': 'Passpor'," +
//                "'SI': 'Sign in', 'type': '11'," +
//                " 'NewUser': '1'," +
//                " 'LoginOptions': '1'," +
//                " 'i3': '36728'," +
//                " 'm1': '768'," +
//                " 'm2': '1184'," +
//                " 'm3': '0'," +
//                " 'i12': '1'," +
//                " 'i17': '0'," +
//                " 'i18': '__Login_Host|1'}";
        String postData = "i13=1&login="+URLEncoder.encode(email,StandardCharsets.UTF_8)+"&loginfmt="+URLEncoder.encode(email,StandardCharsets.UTF_8)+"&type=11&LoginOptions=1&lrt=&lrtPartition=&hisRegion=&hisScaleUnit=&passwd=" + URLEncoder.encode(password,StandardCharsets.UTF_8) + "&ps=2&psRNGCDefaultType=&psRNGCEntropy=&psRNGCSLK=&canary=&ctx=&hpgrequestid=&PPFT=" + URLEncoder.encode(sFTTag,StandardCharsets.UTF_8) + "&PPSX=Passp&NewUser=1&FoundMSAs=&fspost=0&i21=0&CookieDisclosure=0&IsFidoSupported=1&isSignupPost=0&i2=39&i17=0&i18=&i19=4128";
//        postData = URLEncoder.encode(postData, StandardCharsets.UTF_8);
//        RequestBody jsonPost = RequestBody.create(JSON, postData);
//        JSONObject jsonPost = (JSONObject) JSONObject.stringToValue(postData);
        Request.Builder builder = new Request.Builder();
        for (String cookie : cookies) {
            builder.addHeader("Cookie", cookie);
        //    Log.v("OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }
        Request postLogin = builder
                .url(urlPost)
//                .post(jsonPost)
                .build();
        RawResponse resp2 = session.post(urlPost)
                .body(postData)
                .headers(new Header("Content-Type", "application/x-www-form-urlencoded"))
                .followRedirect(false)
                .send();
        String locationHeader = resp2.getHeader("Location");
        String accessToken = locationHeader.substring(locationHeader.indexOf("access_token=") + 13, locationHeader.indexOf("&"));
        String refreshToken  = postJWTfromRPS(accessToken);
        System.out.println("Token: " + refreshToken);
    //        try(Response response = client.newCall(postLogin).execute()){
//            if(response.isRedirect()){
//                System.out.println("yikes");
//            }
//            Headers headers = response.headers();
//            System.out.println(headers);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
        return null;
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
        try{
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            String token = jsonResponse.substring( jsonResponse.indexOf("\"Token\":\"") + 9, jsonResponse.indexOf("DisplayClaims") -3);
            return token;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public String getJWTAuthJSON(String RPS){
        return "{'RelyingParty':'http://auth.xboxlive.com','TokenType':'JWT','Properties':{'AuthMethod':'RPS','SiteName':'user.auth.xboxlive.com','RpsTicket':'" + RPS + "'}}";
    }
    public String getXstsJSON(String token){
        return "{'RelyingParty':'http://xboxlive.com','TokenType':'JWT','Properties':{'UserTokens':['"+token+"'],'SandboxId':'RETAIL'}}";
    }
    public String grabAuthInfo(String contents) throws JSONException {
        JSONObject response = new JSONObject(contents);
        String token = response.getString("Token");
        JSONArray xui =response.getJSONObject("DisplayClaims").getJSONArray("xui");
        String uhs = xui.getJSONObject(0).getString("uhs");
        return uhs + ";" + token;
    }
}
