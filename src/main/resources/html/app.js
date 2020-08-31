app = {

    callGetMediaTabs: function () {
        let tabs = JSON.parse(bridge.getMediaTabs());
        bridge.log('Load tabs: ' + JSON.stringify(tabs));
        return tabs;
    },

    callPlayMedia: function (path) {
        bridge.log('Call play media at path: ' + path);
        bridge.playMedia(path);
    },

    callCloseTab: function (path) {
        bridge.log('Call close tab: ' + path);
        bridge.closeTab(path);
    },

    callRemoveMedia: function (path) {
        bridge.log('Call remove media at path: ' + path);
        bridge.removeMedia(path, false); // TODO modal dialog
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

    onOrderByUpdate: function (orderBy) {
        bridge.onOrderByUpdate(orderBy);
    },

    onReverseOrderUpdate: function (isReverseOrder) {
        bridge.onReverseOrderUpdate(isReverseOrder);
    },

    ln: JSON.parse(bridge.getLocalization())

};

function showMediaCallback(arg) {
    var params = JSON.parse(arg);
    var path = params[0];
    var media = params[1]; // TODO cache last and check if render is needed?
    var selectedItem = params[2]; // TODO

    bridge.log(JSON.stringify(media))
    bridge.log('showMediaCallback: tab path is {0}'.format(path));
    bridge.log('Selected is' + selectedItem);

    var tabId = model.getTabByPath(path).id;
    var tab = ui.getTabById(tabId);

    tab.html('');
    media.forEach(function (m, index) {
        var mediaWrapper = components.getMediaWrapper(m, index === selectedItem);
        mediaWrapper.appendTo(tab);
        mediaWrapper.click(function () {
            app.callPlayMedia(m.path);
        });
        mediaWrapper.find('.glyphicon-file').tooltip({});
        mediaWrapper.find('.glyphicon-remove').click(function () {
            app.callRemoveMedia(m.path);
            mediaWrapper.remove();
        });
    });

    setTimeout("refreshImg()", 100);
}

function refreshImg() {
    var time = new Date().getTime();
    $('img.media-screenshot').each(function (index, img) {
        img.src = img.src + '?' + time;
    });
}

function addTab(tab) {
    var tabStick = components.getTab(tab);
    var tabContent = components.getTabContent(tab);

    tabContent.appendTo(ui.tabsContentWrapper);
    tabStick.appendTo(ui.tabsWrapper);

    model.tabs.push(tab);

    tabStick.find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var tabId = e.target.attributes['tab-id'].value;
        ui.selectedTabId = tabId;

        app.onTabUpdate(tabId);

        ui.searchInput.focus();
    });

    if (tab.closeable) {
        tabStick.find('.tab-close-button').click(function () {
            tabStick.remove();
            tabContent.remove();
            app.callCloseTab(tab.path);
        });
    }
}

function addTabCallback(arg) {
    addTab(JSON.parse(arg));
}
