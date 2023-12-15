package com.nikita22007.pixeldungeonmultiplayer;

import com.watabou.gltextures.TextureCache;
import com.watabou.gltextures.TextureManagerInterface;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextureManager implements TextureManagerInterface {
    public static TextureManager INSTANCE = new TextureManager();
    LinkedHashMap<String,TexturePack> texturePacks = new LinkedHashMap<String, TexturePack>();

    TextureManager()
    {

    }

    @Override
    public void loadTexturePack(InputStream stream)
    {
        try {
            TexturePack texturePack = new TexturePack(stream, true);
            texturePacks.put(texturePack.name,texturePack);
            updateTextureCache();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasAsset(String src) {
        for (TexturePack texturePack : texturePacks.values())
        {
            if (texturePack.hasAsset(src))
                return true;
        }
        return false;
    }

    @Override
    public InputStream getAssetStream(String s) {
        for (TexturePack texturePack : texturePacks.values())
        {
            InputStream stream = texturePack.getAssetStream(s);
            if (stream != null)
            {
                return stream;
            }
        }
        return null;
    }

    public JSONObject getAnimationsJsonObject(String animationsFile) {
        for (TexturePack texturePack : texturePacks.values())
        {
            InputStream stream = texturePack.getAnimationStream(animationsFile);
            if (stream != null)
            {
                JSONObject object = JavaUtils.JSONObjectFromInputStream(stream);
                if (object != null) {
                    return object;
                }
            }
        }
        return null;
    }

    public void unloadServerTexturePacks() {
        boolean invalidateChache = false;
        for (Iterator<Map.Entry<String, TexturePack>> it = texturePacks.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, TexturePack> entry = it.next();
            if (entry.getValue().isServerTexturePack())
            {
                entry.getValue().unload();
                it.remove();
                invalidateChache = true;
            }
        }
        if (invalidateChache)
        {
            updateTextureCache();
        }
    }
    
    protected void updateTextureCache()
    {
        TextureCache.reloadFromAssets();
    }
}
