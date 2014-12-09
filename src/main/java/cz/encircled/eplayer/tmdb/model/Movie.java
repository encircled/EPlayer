package cz.encircled.eplayer.tmdb.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Encircled on 05-Dec-14.
 */
public class Movie {

    public Long id;

    public Boolean adult;

    @SerializedName("backdrop_path")
    public String backdropPath;

    @SerializedName("original_title")
    public String originalTitle;

    @SerializedName("releaseDate")
    public Date releaseDate;

    @SerializedName("poster_path")
    public String posterPath;

    public Float popularity;

    public String title;

    @SerializedName("vote_average")
    public Double voteAverage;

    @SerializedName("vote_count")
    public Double voteCount;

}
