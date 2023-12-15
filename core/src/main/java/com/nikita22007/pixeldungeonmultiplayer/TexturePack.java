package com.nikita22007.pixeldungeonmultiplayer;

import android.util.JsonReader;

import com.watabou.utils.Point;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import kotlin.io.ByteStreamsKt;

public class TexturePack {
    private boolean isServerTexturePack = false;
    ZipFile file;
    String name;
    String description;
    String version;

    Point frameSize;
    public TexturePack(InputStream stream, boolean isServerTexturePack) throws IOException {
        this.isServerTexturePack = isServerTexturePack;
        try {
            File tmpFile = File.createTempFile("texturePack-", ".zip");
            tmpFile.deleteOnExit();
            FileOutputStream filestream = new FileOutputStream(tmpFile);
            ByteStreamsKt.copyTo(stream,filestream,4096);
            file = new ZipFile(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadTexturePack();
    }
    public TexturePack(ZipFile file) throws IOException {
        this.file = file;
        loadTexturePack();
    }

    public void unload(){
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isServerTexturePack(){
        return isServerTexturePack;
    }
    private void loadManifest() throws IOException
    {
        try {
            JSONObject reader = new JSONObject(
                    JavaUtils.StringFromInputStream(
                            file.getInputStream(
                                    file.getEntry("manifest.json")
                            )
                    )
            );
            name = reader.optString("name",file.getName());
            description = reader.optString("description", "No description");
            version = reader.optString("version", "0.0.0");
            frameSize = new Point(
                    reader.optInt("frame_size_x", 16),
                    reader.optInt("frame_size_y", 16)
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadTexturePack() throws IOException {

        loadManifest();
    }

    public boolean hasAsset(String src) {
        return (file.getEntry("assets/"+src) != null);
    }

    protected InputStream getStream(String path)
    {
        ZipEntry entry = file.getEntry(path);
        if (entry == null)
        {
            return null;
        }
        try {
            return file.getInputStream(entry);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public InputStream getAssetStream(String src) {
        return getStream("assets/"+src);
    }

    public InputStream getAnimationStream(String animationsFile) {
        return getStream("animations/"+animationsFile);
    }
}
