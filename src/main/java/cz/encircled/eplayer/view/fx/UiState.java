package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.model.PlayableMedia;

import java.util.List;

/**
 * @author Kisel on 18.08.2015.
 */
public class UiState {

    /**
     * Selected media item by remote control
     */
    Integer selectedItem = null;

    List<PlayableMedia> currentMedia = null;

    private String filter = "";

    private ViewType viewType = ViewType.ALL;

    private OrderBy orderBy = OrderBy.NAME;

    private String path = "";

    private boolean isQuickNavi = true;

    private boolean isReverseOrder = false;

    public boolean isReverseOrder() {
        return isReverseOrder;
    }

    public void setIsReverseOrder(boolean isReverseOrder) {
        this.isReverseOrder = isReverseOrder;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }

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

    public enum OrderBy {

        NAME, SIZE, CREATION_DATE

    }

}
