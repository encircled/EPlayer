package cz.encircled.eplayer.tmdb.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Encircled on 05-Dec-14.
 */
public class ValidateWithLogin extends JsonRequest {

    @SerializedName("request_token")
    public String requestToken;

}
