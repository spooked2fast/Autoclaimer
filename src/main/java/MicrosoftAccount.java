import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MicrosoftAccount extends Account{
    private XstsToken xstsToken;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public MicrosoftAccount(String email, String password){
        super(email, password, "http://accounts.xboxlive.com");
    }
    public MicrosoftAccount(String email, String password,String refreshToken){
        super(email, password, refreshToken,"http://accounts.xboxlive.com");
    }
    public void setXstsToken(XstsToken xstsToken){
        this.xstsToken = xstsToken;
    }
    public int sendTroublePost(String target, OkHttpClient client, boolean isOrigin){
        String url = "https://accountstroubleshooter.xboxlive.com/users/current/profile";
        if(isOrigin){
            url = "https://accountstroubleshooter-origin.xboxlive.com/users/current/profile";
        }
        Request claimRequest = new Request.Builder()
                .url(url)
                .addHeader("x-xbl-contract-version","4")
                .addHeader("Authorization","XBL3.0 x=" + xstsToken.toString())
                .post(getClaimJSON(target))
                .build();
        try (Response response = client.newCall(claimRequest).execute()) {
            return response.code();
        } catch (Exception e){
            return 0;
        }
    }
    private RequestBody getClaimJSON(String target){
        String body = "{\"dateOfBirth\": \"2000-01-01T00:00:00.0000000\", \"email\": \"\", \"firstName\": \"\", \"gamerTag\": \""+target+"\", \"gamerTagChangeReason\": null, \"homeAddressInfo\": {\"city\": null, \"country\": \"US\", \"postalCode\": null, \"state\": null, \"street1\": null, \"street2\": null}, \"homeConsole\": null, \"imageUrl\": \"\", \"isAdult\": true, \"lastName\": \"\", \"legalCountry\": \"US\", \"locale\": \"en-US\", \"midasConsole\": null, \"msftOptin\": true, \"ownerHash\": null, \"ownerXuid\": null, \"partnerOptin\": true, \"requirePasskeyForPurchase\": false, \"requirePasskeyForSignIn\": false, \"subscriptionEntitlementInfo\": null, \"touAcceptanceDate\": \"2000-01-01T00:00:00.0000000\", \"userHash\": " +xstsToken.getUserHash()+", \"userKey\": null, \"userXuid\": \"216258806147975844\"}";
        return RequestBody.create(JSON, body);
    }
    public String requestEmail(OkHttpClient client){
        Request claimRequest = new Request.Builder()
                .url("https://accountstroubleshooter.xboxlive.com/users/current/profile")
                .addHeader("x-xbl-contract-version","2")
                .addHeader("Authorization","XBL3.0 x=" + xstsToken.toString())
                .build();
        try (Response response = client.newCall(claimRequest).execute()) {
            String jsonResponse = response.body().string();
            JSONObject jsonSerial = new JSONObject(jsonResponse);
            String email = jsonSerial.getString("email");
            return email;
        } catch(Exception e){
            return null;
        }
    }
}
