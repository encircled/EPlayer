package cz.encircled.eplayer.tmdb.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Encircled on 05-Dec-14.
 */
public class NewToken extends JsonRequest {

    @SerializedName("expires_at")
    public String expiresAt;

    @SerializedName("request_token")
    public String requestToken;

}
