app = {

    callGetMediaTabs: function () {
        if (!model.tabs.length) {
            model.tabs = [{id: 1, path: 'QuickNavi'}];
            model.tabs = model.tabs.concat(JSON.parse(bridge.getMediaTabs()));
            console.log('Tabs loaded');
        }
        return model.tabs;
    },

    callPlayMedia: function (path) {
        console.log('Call play media at path: ' + path);
        bridge.playMedia(path);
    },

    onFilterUpdate: function (filter) {
        bridge.onFilterUpdate(filter);
    },

    onTabUpdate: function (tabId) {
        var tabPath = model.getTabById(tabId).path;
        bridge.onTabUpdate(tabPath);
    },

    onViewTypeUpdate: function (viewType) {
        bridge.onViewTypeUpdate(viewType);
    },

    ln: JSON.parse(bridge.getLocalization())

};

function showMediaCallback(arg) {
    var params = JSON.parse(arg);
    var path = params[0];
    var media = params[1];

    console.log('showMediaCallback: tab path is {0}'.format(path));

    var tab = ui.getTabById(model.getTabByPath(path).id);

    tab.html('');
    media.forEach(function (m) {
        var mediaWrapper = components.getMediaWrapper(m);
        mediaWrapper.appendTo(tab);
        mediaWrapper.click(function () {
            app.callPlayMedia(m.path);
        });
    })
}
