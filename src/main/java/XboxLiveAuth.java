import okhttp3.*;
public class XboxLiveAuth {
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String loginURL = "https://login.live.com/oauth20_authorize.srf?display=touch&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf&locale=en&response_type=token&client_id=0000000048093EE3";
    public XboxLiveAuth(){

    }
    public String login(String email, String password){
        String urlPost = "";
        String sFTTag = "";
        Request getPage = new Request.Builder()
                .url(loginURL)
                .build();
        try(Response response = client.newCall(getPage).execute()){
            String body = response.body().string();
            urlPost = body.substring(body.indexOf("urlPost:'") + 9,body.indexOf("',iUXMode"));
            sFTTag = body.substring(body.indexOf("id=\"i0327\" value=\"") + 18,body.indexOf("\"/>',BQ:"));
            System.out.println(urlPost + "\n" + sFTTag);
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
        System.out.println(postData);
        RequestBody jsonPost = RequestBody.create(JSON, postData);
        Request postLogin = new Request.Builder()
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
