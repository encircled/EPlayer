package cz.encircled.eplayer.view.fx;

/**
 * @author Kisel on 18.08.2015.
 */
public class UiState {

    private String filter = "";

    private ViewType viewType = ViewType.ALL;

    private String path = "";

    private boolean isQuickNavi = true;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isQuickNavi() {
        return isQuickNavi;
    }

    public void setIsQuickNavi(boolean isQuickNavi) {
        this.isQuickNavi = isQuickNavi;
    }

    public enum ViewType {

        ALL, FILMS, SERIES;

    }

}
