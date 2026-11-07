package com.bakbakan.assets;

import com.bakbakan.entities.HeroData;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AssetManager {

    public static final String[] SPRITE_STATES = {
            "idle", "walk", "jump", "duck", "block",
            "attack", "skill", "special", "hit", "ko"
    };

    public static final String[] MUSIC_IDS = {
            "loading", "menu", "battle", "victory", "gameover"
    };

    public static final String[] SFX_IDS = buildSfxIds();

    private static final String[] AUDIO_EXTENSIONS = {".wav", ".mp3", ".aiff"};

    private final List<AssetEntry> manifest = new ArrayList<>();
    private final Map<String, Image> sprites = new HashMap<>();
    private final Map<String, URL> audio = new HashMap<>();
    private int cursor;

    public AssetManager() {
        buildManifest();
    }

    private static String[] buildSfxIds() {
        List<String> ids = new ArrayList<>(List.of(
                "ui_click", "jump", "hit", "block", "no_mana", "round_start", "ko"));
        for (HeroData hero : HeroData.ALL) {
            ids.add("skill_" + hero.id());
            ids.add("special_" + hero.id());
        }
        return ids.toArray(String[]::new);
    }

    private void buildManifest() {
        for (HeroData hero : HeroData.ALL) {
            for (String state : SPRITE_STATES) {
                manifest.add(new AssetEntry(
                        "sprite:" + hero.id() + ":" + state,
                        "/sprites/" + hero.id() + "/" + state + ".png",
                        true));
            }
        }
        for (String music : MUSIC_IDS) {
            manifest.add(new AssetEntry("music:" + music, null, false));
        }
        for (String sfx : SFX_IDS) {
            manifest.add(new AssetEntry("sfx:" + sfx, null, false));
        }
    }

    public void beginLoading() {
        cursor = 0;
    }

    public boolean loadNext() {
        if (cursor >= manifest.size()) {
            return false;
        }
        AssetEntry entry = manifest.get(cursor++);
        if (entry.image) {
            URL url = getResource(entry.path);
            if (url != null) {
                try {
                    sprites.put(entry.key, new Image(url.toExternalForm(), false));
                } catch (RuntimeException ignored) {
                    // A broken PNG must never stop the game from starting.
                }
            }
        } else {
            int separator = entry.key.indexOf(':');
            boolean music = separator > 0 && "music".equals(entry.key.substring(0, separator));
            String id = separator > 0 ? entry.key.substring(separator + 1) : entry.key;
            URL url = resolveAudio(id, music);
            if (url != null) {
                audio.put(entry.key, url);
            }
        }
        return true;
    }

    public int getLoadedCount() {
        return cursor;
    }

    public int getTotalCount() {
        return manifest.size();
    }

    public double getProgress() {
        if (manifest.isEmpty()) {
            return 1.0;
        }
        return Math.min(1.0, cursor / (double) manifest.size());
    }

    public Image getSprite(String heroId, String state) {
        return sprites.get("sprite:" + heroId + ":" + state);
    }

    public URL getResource(String path) {
        if (path == null) {
            return null;
        }
        return AssetManager.class.getResource(path);
    }

    public URL resolveAudio(String id, boolean music) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String safe = id.replace('.', '_').replaceAll("[^a-zA-Z0-9_-]", "_");
        String folder = music ? "/audio/music/" : "/audio/sfx/";
        for (String extension : AUDIO_EXTENSIONS) {
            URL url = getResource(folder + safe + extension);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public boolean hasAudio(String id, boolean music) {
        return audio.containsKey((music ? "music:" : "sfx:") + id);
    }

    private static final class AssetEntry {

        private final String key;
        private final String path;
        private final boolean image;

        private AssetEntry(String key, String path, boolean image) {
            this.key = key;
            this.path = path;
            this.image = image;
        }
    }
}
