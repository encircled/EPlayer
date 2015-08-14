app = {

    getMediaTabs: function () {
        if (!model.tabs.length) {
            model.tabs = [{id: 1, path: 'QuickNavi'}];
            model.tabs = model.tabs.concat(JSON.parse(bridge.getMediaTabs()));
            console.log('Tabs loaded');
        }
        return model.tabs;
    },

    callMediaTabContent: function (tabId) {
        console.log('Call media tab ' + tabId);
        var tab = model.getTabById(tabId);
        console.log('path ' + tab.path);
        if (tabId == 1) {
            this.callGetQuickNaviContent();
        } else {
            bridge.getMediaTabContent(tab.path);
        }
    },

    callGetQuickNaviContent: function () {
        console.log('Call GetQuickNaviContent');
        bridge.getQuickNaviContent();
    },

    callPlayMedia: function (path) {
        console.log('Call play media at path: ' + path);
        bridge.playMedia(path);
    },

    ln: JSON.parse(bridge.getLocalization())

};

function showMediaCallback(arg) {
    var params = JSON.parse(arg);
    var path = params[0];
    var media = params[1];

    console.log('showMediaCallback: tab path is {0}'.format(path));
    console.log(media);

    var tab = ui.getTabById(model.getTabByPath(path).id);

    showMediaInternal(tab, media);
}

function showQuickNaviCallback(arg) {
    var params = JSON.parse(arg);
    var media = params[0];

    console.log('showQuickNaviCallback');
    console.log(media);

    var tab = ui.getTabById(1);

    showMediaInternal(tab, media);
}

function showMediaInternal(tabElement, media) {
    console.log('Target tab is ' + tabElement);

    tabElement.html('');
    media.forEach(function (m) {
        var mediaWrapper = components.getMediaWrapper(m);
        mediaWrapper.appendTo(tabElement);
        mediaWrapper.click(function () {
            app.callPlayMedia(m.path);
        });
    })
}

function refreshCurrentTabCallback() {
    var tabId = $('#tabs-wrapper li.active a').attr('tab-id');
    app.callMediaTabContent(tabId ? parseInt(tabId) : 1);
}