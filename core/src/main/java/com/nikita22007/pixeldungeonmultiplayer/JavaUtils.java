package com.nikita22007.pixeldungeonmultiplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import android.util.Base64;

public class JavaUtils {

    public static JSONObject JSONObjectFromInputStream(InputStream stream) {
        JSONObject reader = null;
        try {
            reader = new JSONObject(
                    JavaUtils.StringFromInputStream(
                            stream
                    )
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return reader;
    }
    public static boolean hasNotNull(JSONObject obj, String key)
    {
        return obj.has(key) && !obj.isNull(key);
    }

    public static Integer[] JSONArrayToIntegerClassArray(JSONArray arr) throws JSONException {
        Integer[] ints = new Integer[arr.length()];
        for (int i = 0; i < ints.length; i++)
        {
            ints[i] = arr.getInt(i);
        }
        return ints;
    }

    public static InputStream InputStreamFromBase64(String encoded) {
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        return new ByteArrayInputStream(decoded);
    }

    public static String StringFromInputStream(InputStream inputStream) throws IOException {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream,"UTF-8");
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        return out.toString();
    }
}
