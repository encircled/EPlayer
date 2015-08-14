var model = {

    tabs: [],

    getTabById: function (tabId) {
        var result;
        this.tabs.forEach(function (tab) {
            if (tab.id == tabId) {
                result = tab;
            }
        });
        return result;
    },

    getTabByPath: function (tabPath) {
        var result;
        this.tabs.forEach(function (tab) {
            if (tab.path == tabPath) {
                result = tab;
            }
        });
        return result;
    }

};