package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndError;

import java.io.IOException;

import static com.watabou.pixeldungeon.network.Client.readStream;
import static com.watabou.pixeldungeon.network.Client.socket;

public class ParceThread extends Thread {

    @Override
    public void run() {
        while (!socket.isClosed()){
            try {
                int code = (Integer) readStream.readInt();
                switch (code){
                    //Network block
                    case Codes.NOP: {break;}
                    case Codes.SERVER_FULL:{
                        PixelDungeon.switchScene(TitleScene.class);
                        // TODO   PixelDungeon.scene().add(new WndError("Server full"));
                        return;
                       }
                    //level block
                    case Codes.LEVEL_MAP: {
                        Dungeon.level.map= readIntArray();break;
                    }
                    //UI block
                    case Codes.IL_FADE_OUT: {
                        InterlevelScene.phase  = InterlevelScene.Phase.FADE_OUT;break;
                    }
                }
            }catch (IOException e){
                GLog.n(e.getMessage());

                PixelDungeon.switchScene(TitleScene.class);
//                PixelDungeon.scene().add(new WndError("Disconnected"));
                return;
            }
        }
    }
    protected int[] readIntArray()throws IOException{
        int len =  readStream.readInt();
        int[] res =new int[len];
        for  (int i=0;i<len;i++){
            res[i]=readStream.readInt();
        }
        return res;
    }
}