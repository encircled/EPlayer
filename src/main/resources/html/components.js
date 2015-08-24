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
        console.log(media.pathToScreenshot);
        var image = media.time ? '<img src="{0}" alt="" class="media-screenshot" width="336" height="189"/>'.format(media.pathToScreenshot) : '';

        return $(('<div class="media-wrapper"><div class="panel panel-default">' +
        '<div style="height: 189px">' + image + '</div>' +
        '<div class="panel-body">' +
        '<h4 style="font-weight: bold">{0}</h4>' +
        '<p>{1}, watched {2}</p>' +
        '</div>' +
        '<div class="panel-footer"><div class="center-block" style="width: 100px">' +
        '<span title="{3}, {4}" class="glyphicon glyphicon-file"></span>' +
        '<span class="glyphicon glyphicon-remove"></span>' +
        '</div></div>' +
        '</div></div>').format(media.name, media.formattedCurrentTime, media.formattedWatchDate, media.extension, media.formattedSize));
    }

};