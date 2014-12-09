package cz.encircled.eplayer.tmdb.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Encircled on 05-Dec-14.
 */
public class NewSession extends JsonRequest {

    @SerializedName("session_id")
    public String sessionId;

}
