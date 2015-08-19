var components = {

    getTab: function (tab) {
        return ('<li role="presentation">' +
        '<a href="#tab_{0}" tab-id="{0}" role="tab" data-toggle="tab"><button type="button" class="close"><span aria-hidden="true">&times;</span></button>{1}</a>' +
        '</li>').format(tab.id, tab.path);
    },

    getTabContent: function (tab) {
        return '<div role="tabpanel" class="tab-pane" id="tab_{0}"></div>'.format(tab.id);
    },

    getMediaWrapper: function (media) {
        return $(('<div class="media-wrapper"><div class="panel panel-primary">' +
        '<div class="panel-heading"><h3 class="panel-title"><p>{0}</p></h3></div>' +
        '<div class="panel-body">' +
        '{1}' +
        '<span title="{2}, {3}" class="glyphicon glyphicon-info-sign" />' +
        '</div>' +
        '</div></div>').format(media.name, media.timeLabel, media.extension, media.formattedSize));
    }

};