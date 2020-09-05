package cz.encircled.eplayer.view.fx;

/**
 * @author Kisel on 18.08.2015.
 */
public class UiState {

    private ViewType viewType = ViewType.ALL;

    private OrderBy orderBy = OrderBy.NAME;

    private boolean isReverseOrder = false;

    public enum ViewType {
        ALL, FILMS, SERIES;
    }

    public enum OrderBy {
        NAME, SIZE, CREATION_DATE
    }

}
