package cz.encircled.eplayer.service;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.event.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import java.util.concurrent.CountDownLatch;

/**
 * @author Encircled on 9.6.2014.
 */
public class VLCMediaService implements MediaService {

    private static final Logger log = LogManager.getLogger();


    private long currentTime;

    private MediaPlayer player;

    @Nullable
    private String current;

    private ApplicationCore core;

    public VLCMediaService(ApplicationCore applicationCore, MediaPlayer mediaPlayer) {
        core = applicationCore;
        init(mediaPlayer);
    }

    @Override
    public void releasePlayer() {
        stop();
        if (player != null) {
            player.release();
        }
    }

    // TODO change me
    @Override
    public void updateCurrentMediaInCache() {
        if (current != null)
            core.getCacheService().updateEntry(current, currentTime);
    }

    private void play(@NotNull String path, long time) {
        log.debug("Play {}, start time is {}", path, time);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        log.debug("Show player requested");
        core.getViewService().showPlayer(countDownLatch);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("Show player done");
        if (!path.equals(current)) {
            log.debug("Path {} is new", path);
            current = path;
            player.prepareMedia(path);
        }
        player.start();
        setTime(Math.min(time, player.getLength()));
        log.debug("Playing started");
    }

    @Override
    public void play(@NotNull String path) {
        play(core.getCacheService().createIfAbsent(path));
    }

    @Override
    public void play(@NotNull MediaType p) {
        core.getCacheService().addIfAbsent(p);
        play(p.getPath(), p.getTime());
    }

    @Override
    public int getSubtitles() {
        return player.getSpu();
    }

    @Override
    public void setSubtitles(int id) {
        player.setSpu(id);
    }

    @Override
    public int getAudioTrack() {
        return player.getAudioTrack();
    }

    @Override
    public void setAudioTrack(int trackId) {
        player.setAudioTrack(trackId);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getMediaLength() {
        return (int) player.getLength();
    }

    @Override
    public long getCurrentTime() {
        return currentTime;
    }

    @Override
    public int getVolume() {
        return player.getVolume();
    }

    @Override
    public void setVolume(int value) {
        player.setVolume(value);
    }

    @Override
    public void setTime(long value) {
        player.setTime(value);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public void toggle() {
        player.pause();
    }

    @Override
    public void pause() {
        if (player.isPlaying())
            player.pause();
    }

    @Override
    public void stop() {
        log.debug("Stop player");
        current = null;
        currentTime = 0L;
        if (player != null) {
            player.stop();
        }
        core.getViewService().enableSubtitlesMenu(false);
    }

    private void init(MediaPlayer mediaPlayer) {
        long start = System.currentTimeMillis();
        log.trace("VLCMediaService init start");
        try {
            player = mediaPlayer;

            player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    core.getEventObserver().fire(Event.playingChanged, true);
                }

                @Override
                public void paused(MediaPlayer mediaPlayer) {
                    core.getEventObserver().fire(Event.playingChanged, false);
                }

                @Override
                public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
                    log.debug("Media parsed - {}", current);
                    core.getEventObserver().fire(Event.subtitlesUpdated, player.getSpuDescriptions());
                    core.getEventObserver().fire(Event.audioTracksUpdated, player.getAudioDescriptions());
                }

                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    currentTime = 0L;
                    core.openQuickNavi();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    currentTime = newTime;
                    core.getEventObserver().fire(Event.mediaTimeChange, newTime, null);
                }

                @Override
                public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
                    core.getEventObserver().fire(Event.mediaDurationChange, newDuration, null);
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    log.error("Failed to open media {} ", current);
                    // TODO
//                    guiUtil.showMessage(Localization.fileOpenFailed.ln(), Localization.errorTitle.ln());
                    if (current != null) {
                        core.getCacheService().deleteEntry(current);
                        current = null;
                    }
                    core.getViewService().switchToQuickNavi();
                }

            });

        } catch (Exception e) {
            log.error("Player initialization failed", e);
            // TODO
//            guiUtil.showMessage("VLC library not found", "Error title");
        }
        log.trace("VLCMediaService init complete in {} ms", System.currentTimeMillis() - start);
    }

}
