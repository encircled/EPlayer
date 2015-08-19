package cz.encircled.eplayer.util;

public enum Localization {

    file,

    language,

    save,

    cancel,

    settings,

    tools,

    open,

    exit,

    openQuickNavi,

    errorTitle,

    msgQnFileCorrupted,

    msgQnFileIoFail,

    media,

    fileOpenFailed,

    subtitles,

    audioTrack,

    view,

    fullScreen,

    play,

    pause,

    fitScreen,

    deleteMissing,

    today,

    yesterday,

    daysAgo,

    back;

    public String ln() {
        return LocalizationProvider.get2(this.name());
    }
}
