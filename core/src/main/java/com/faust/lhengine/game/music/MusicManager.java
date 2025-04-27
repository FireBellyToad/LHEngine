package com.faust.lhengine.game.music;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.faust.lhengine.game.music.enums.TuneEnum;

import java.util.*;

/**
 * Music manager class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class MusicManager {

    private final Map<TuneEnum, Music> musicMap = new EnumMap<>(TuneEnum.class);
    private final List<TuneEnum> pausedTunes = new ArrayList<>();

    /**
     * Load and put in map a single tune
     *
     * @param tune
     * @param assetManager
     */
    public void loadSingleTune(TuneEnum tune, AssetManager assetManager) {
        Objects.requireNonNull(tune);
        Objects.requireNonNull(assetManager);

        assetManager.load(tune.getFileName(), Music.class);
        assetManager.finishLoading();
        musicMap.put(tune, assetManager.get(tune.getFileName()));
    }

    /**
     * Load all music from files. MusicManager.initTuneMap MUST BE CALLED AFTER
     *
     * @param assetManager
     */
    public void loadMusicFromFiles(AssetManager assetManager) {
        Objects.requireNonNull(assetManager);
        for (TuneEnum tune : TuneEnum.values()) {
            assetManager.load(tune.getFileName(), Music.class);
        }
    }

    /**
     * Should be called AFTER MusicManager.loadMusicFromFiles
     *
     * @param assetManager
     */
    public void initTuneMap(AssetManager assetManager) {
        Objects.requireNonNull(assetManager);

        for (TuneEnum tune : TuneEnum.values()) {
            musicMap.put(tune, assetManager.get(tune.getFileName()));
        }
    }

    /**
     * Play looping tune
     *
     * @param tune to play
     */
    public void playMusic(TuneEnum tune) {
        playMusic(tune, 1f, true);
    }

    /**
     * Play looping tune
     *
     * @param tune   to play
     * @param volume
     */
    public void playMusic(TuneEnum tune, float volume) {
        playMusic(tune, volume, true);
    }

    /**
     * Play tune
     *
     * @param tune to play
     */
    public void playMusic(TuneEnum tune, boolean loop) {
        playMusic(tune, 1f, loop);
    }

    /**
     * Play a tune
     *
     * @param tune   to play
     * @param volume
     * @param loop   true if must loop
     */
    public void playMusic(TuneEnum tune, float volume, boolean loop) {
        Objects.requireNonNull(tune);
        final Music tuneToPlay = musicMap.get(tune);
        Objects.requireNonNull(tuneToPlay);

        //If tune is not already playing, stop previous and play it
        if (!tuneToPlay.isPlaying()) {
            stopMusic();
            tuneToPlay.play();
            tuneToPlay.setLooping(loop);
            tuneToPlay.setVolume(volume);
        }
    }

    /**
     * Pause all music
     */
    public void pauseMusic() {
        musicMap.forEach((tune, music) -> {
            if (music.isPlaying()) {
                music.pause();
                pausedTunes.add(tune);
            }
        });
    }

    /**
     * Pause all music
     */
    public void resumeMusic() {
        pausedTunes.forEach( tune -> musicMap.get(tune).play());

        pausedTunes.clear();
    }

    /**
     * Stop all music
     */
    public void stopMusic() {
        musicMap.forEach((tune, music) -> music.stop());
    }

    /**
     * @param tune
     * @return true if is playing
     */
    public boolean isPlaying(TuneEnum tune) {
        final Music tuneToCheck = musicMap.get(tune);
        Objects.requireNonNull(tuneToCheck);
        return tuneToCheck.isPlaying();
    }

    /**
     * @return true if any music is playing
     */
    public boolean isPlaying() {
        for (Music tuneToCheck : musicMap.values()) {
            if (tuneToCheck.isPlaying()) {
                return true;
            }
        }
        return false;
    }
}
