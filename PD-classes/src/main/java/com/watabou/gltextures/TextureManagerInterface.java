package com.watabou.gltextures;

import java.io.InputStream;

public interface TextureManagerInterface {

    void loadTexturePack(InputStream stream);

    boolean hasAsset(String src);

    InputStream getAssetStream(String s);
}
