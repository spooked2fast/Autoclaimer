import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;


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
    private OkHttpClient client = new OkHttpClient().newBuilder()
    //        .proxy(proxyTest)
//            .cookieJar()
            .followRedirects(false)
//            .followSslRedirects(false)
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String loginURL = "https://login.live.com/oauth20_authorize.srf?display=touch&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&locale=en&response_type=token&client_id=0000000048093EE3";
    public XboxLiveAuth(){
    }
    public String login(String email, String password) {

//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyPort", "8888");


        String urlPost = "";
        String sFTTag = "";
        Request getPage = new Request.Builder()
                .url(loginURL)
                .build();

        HashSet<String> cookies = new HashSet<>();

        try(Response response = client.newCall(getPage).execute()){
            String body = response.body().string();
            urlPost = body.substring(body.indexOf("urlPost:'") + 9,body.indexOf("',iUXMode"));
            sFTTag = body.substring(body.indexOf("id=\"i0327\" value=\"") + 18,body.indexOf("\"/>',BQ:"));
            System.out.println(urlPost + "\n" + sFTTag);
            for (String header : response.headers("Set-Cookie")) {
                cookies.add(header);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        String postData =
                "{'login': '" + email + "'," +
                "'passwd': '" + password + "'," +
                "'PPFT': '" + sFTTag + "'," +
                "'PPSX': 'Passpor'," +
                "'SI': 'Sign in', 'type': '11'," +
                " 'NewUser': '1'," +
                " 'LoginOptions': '1'," +
                " 'i3': '36728'," +
                " 'm1': '768'," +
                " 'm2': '1184'," +
                " 'm3': '0'," +
                " 'i12': '1'," +
                " 'i17': '0'," +
                " 'i18': '__Login_Host|1'}";
//        postData = URLEncoder.encode(postData, StandardCharsets.UTF_8);
        System.out.println(postData);
        RequestBody jsonPost = RequestBody.create(JSON, postData);

        Request.Builder builder = new Request.Builder();

        for (String cookie : cookies) {
            builder.addHeader("Cookie", cookie);
        //    Log.v("OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }
        Request postLogin = builder
                .url(urlPost)
                .post(jsonPost)
                .build();
        try(Response response = client.newCall(postLogin).execute()){
            Headers headers = response.headers();
            System.out.println(headers);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
