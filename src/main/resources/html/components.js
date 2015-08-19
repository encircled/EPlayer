var components = {

    getTab: function (tab) {
        var start = '<li role="presentation">' +
            '<a href="#tab_{0}" tab-id="{0}" role="tab" data-toggle="tab">';
        var end = '{1}</a></li>';
        var closeButton = tab.closeable ? '<span class="tab-close-button">&times;</span>' : '';

        return $((start + closeButton + end).format(tab.id, tab.path));
    },

    getTabContent: function (tab) {
        return $('<div role="tabpanel" class="tab-pane" id="tab_{0}"></div>'.format(tab.id));
    },

    getMediaWrapper: function (media) {
        return $(('<div class="media-wrapper"><div class="panel panel-primary">' +
        '<div class="panel-heading"><h3 class="panel-title">{0}</h3></div>' +
        '<div class="panel-body">' +
        '<p>{1}</p>' +
        '<p>{2}</p>' +
        '<span title="{3}, {4}" class="glyphicon glyphicon-info-sign" />' +
        '</div>' +
        '</div></div>').format(media.name, media.formattedCurrentTime, media.formattedWatchDate, media.extension, media.formattedSize));
    }

};