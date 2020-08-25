package cz.encircled.eplayer.model;

/**
 * Audio or text track description
 *
 * @author encir on 25-Aug-20.
 */
public class GenericTrackDescription {

    public int id;

    public String description;

    public GenericTrackDescription(int id, String description) {
        this.id = id;
        this.description = description;
    }

}
