package com.bakbakan.audio;

import com.bakbakan.assets.AssetManager;
import com.bakbakan.events.GameEventBus;
import com.bakbakan.events.ScreenChangeEvent;
import com.bakbakan.events.SoundRequestEvent;
import javafx.event.EventHandler;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AudioManager {

    private static final Set<String> LOOPING_MUSIC = Set.of("loading", "menu", "battle");
    private static final double MUSIC_VOLUME = 0.32;
    private static final double SFX_VOLUME = 0.55;
    private static final int SAMPLE_RATE = 22050;

    private final AssetManager assets;
    private final GameEventBus bus;
    private final Map<String, AudioClip> sfxCache = new HashMap<>();
    private final EventHandler<SoundRequestEvent> soundHandler;
    private final EventHandler<ScreenChangeEvent> screenHandler;

    private MediaPlayer currentMusic;
    private String currentMusicId;

    public AudioManager(AssetManager assets, GameEventBus bus) {
        this.assets = assets;
        this.bus = bus;
        this.soundHandler = this::onSoundRequest;
        this.screenHandler = this::onScreenChange;
        this.bus.register(SoundRequestEvent.SOUND_REQUEST, soundHandler);
        this.bus.register(ScreenChangeEvent.SCREEN_CHANGE, screenHandler);
    }

    private void onSoundRequest(SoundRequestEvent event) {
        if (event.isMusic()) {
            playMusic(event.getSoundId());
        } else {
            playSfx(event.getSoundId());
        }
    }

    private void onScreenChange(ScreenChangeEvent event) {
        if (event.isRequest()) {
            return;
        }
        stopMusic();
    }

    public void playMusic(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        if (id.equals(currentMusicId)
                && currentMusic != null
                && currentMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }

        stopMusic();

        String uri = resolveUri(id, true);
        if (uri == null) {
            return;
        }

        try {
            Media media = new Media(uri);
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(MUSIC_VOLUME);
            player.setCycleCount(LOOPING_MUSIC.contains(id) ? MediaPlayer.INDEFINITE : 1);
            player.setOnError(this::stopMusic);
            currentMusic = player;
            currentMusicId = id;
            player.play();
        } catch (RuntimeException ex) {
            stopMusic();
        }
    }

    public void playSfx(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        AudioClip clip = sfxCache.computeIfAbsent(id, key -> {
            String uri = resolveUri(key, false);
            if (uri == null) {
                return null;
            }
            try {
                return new AudioClip(uri);
            } catch (RuntimeException ex) {
                return null;
            }
        });
        if (clip != null) {
            try {
                clip.play(SFX_VOLUME);
            } catch (RuntimeException ignored) {
                // Sound is optional; never let playback break the game.
            }
        }
    }

    private String resolveUri(String id, boolean music) {
        URL url = assets.resolveAudio(id, music);
        if (url != null) {
            return url.toExternalForm();
        }
        try {
            return ensurePlaceholder(id, music).toUri().toString();
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private Path ensurePlaceholder(String id, boolean music) throws IOException {
        Path directory = Paths.get(System.getProperty("java.io.tmpdir"), "bakbakan-audio");
        Files.createDirectories(directory);
        String safe = id.replaceAll("[^a-zA-Z0-9_-]", "_");
        Path file = directory.resolve((music ? "music_" : "sfx_") + safe + ".wav");
        if (Files.exists(file) && Files.size(file) > 44) {
            return file;
        }
        Files.write(file, generateWav(id, music),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }

    private byte[] generateWav(String id, boolean music) {
        double duration = music ? 3.2 : 0.16;
        int sampleCount = (int) (SAMPLE_RATE * duration);
        int dataSize = sampleCount * 2;

        ByteBuffer buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(ascii("RIFF"));
        buffer.putInt(36 + dataSize);
        buffer.put(ascii("WAVE"));
        buffer.put(ascii("fmt "));
        buffer.putInt(16);
        buffer.putShort((short) 1);
        buffer.putShort((short) 1);
        buffer.putInt(SAMPLE_RATE);
        buffer.putInt(SAMPLE_RATE * 2);
        buffer.putShort((short) 2);
        buffer.putShort((short) 16);
        buffer.put(ascii("data"));
        buffer.putInt(dataSize);

        int hash = Math.abs(id.hashCode());
        if (music) {
            int[] scale = {0, 3, 5, 7, 10, 12, 10, 7};
            double noteDuration = duration / scale.length;
            double base = 196 + (hash % 4) * 12;
            for (int i = 0; i < sampleCount; i++) {
                double t = i / (double) SAMPLE_RATE;
                int noteIndex = Math.min(scale.length - 1, (int) (t / noteDuration));
                double frequency = base * Math.pow(2, scale[noteIndex] / 12.0);
                double localT = t - noteIndex * noteDuration;
                double attack = Math.min(1.0, localT / 0.03);
                double release = Math.min(1.0, Math.max(0.0, (noteDuration - localT) / 0.08));
                double envelope = attack * release;
                double wave = Math.sin(2 * Math.PI * frequency * t) * 0.55
                        + Math.sin(2 * Math.PI * frequency * 2 * t) * 0.18;
                double sample = wave * envelope * 0.22;
                buffer.putShort((short) (sample * Short.MAX_VALUE));
            }
        } else {
            double frequency = 320 + (hash % 480);
            for (int i = 0; i < sampleCount; i++) {
                double t = i / (double) SAMPLE_RATE;
                double envelope = Math.exp(-t * 22.0) * Math.min(1.0, t / 0.005);
                double wave = Math.sin(2 * Math.PI * frequency * t) * 0.7
                        + Math.sin(2 * Math.PI * frequency * 1.5 * t) * 0.2;
                double sample = wave * envelope * 0.35;
                buffer.putShort((short) (sample * Short.MAX_VALUE));
            }
        }

        return buffer.array();
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    public void stopMusic() {
        if (currentMusic != null) {
            try {
                currentMusic.stop();
            } catch (RuntimeException ignored) {
            }
            try {
                currentMusic.dispose();
            } catch (RuntimeException ignored) {
            }
            currentMusic = null;
        }
        currentMusicId = null;
    }

    public void dispose() {
        stopMusic();
        bus.unregister(SoundRequestEvent.SOUND_REQUEST, soundHandler);
        bus.unregister(ScreenChangeEvent.SCREEN_CHANGE, screenHandler);
        sfxCache.clear();
    }
}
