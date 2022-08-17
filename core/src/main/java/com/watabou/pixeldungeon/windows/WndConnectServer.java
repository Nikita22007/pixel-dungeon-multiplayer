package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.network.Client;
import com.watabou.pixeldungeon.network.NetworkScanner;
import com.watabou.pixeldungeon.network.ServerInfo;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.StartScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndConnectServer extends Window {
    private static final int WIDTH			= 120;
    private static final int MARGIN 		= 2;
    private static final int BUTTON_HEIGHT	= 20;
    private ServerInfo serverInfo;
    private Scene scene;

    private String generateMessage(int players, int playersMax){
        String message="Players: ";
        message+=(players>-1)?players:"?";
        message+='/';
        message+=(playersMax>-1)?playersMax:"?";
        message+='\n';
        //message+=IP+':'+port;
        return message;
    }
    public WndConnectServer(Scene scene, ServerInfo server){
        super();
        this.serverInfo = server;
        this.scene=scene;

        BitmapTextMultiline tfTitle = PixelScene.createMultiline(server.name, 9 );
        tfTitle.hardlight( TITLE_COLOR );
        tfTitle.x = tfTitle.y = MARGIN;
        tfTitle.maxWidth = WIDTH - MARGIN * 2;
        tfTitle.measure();
        tfTitle.x= (tfTitle.maxWidth-tfTitle.width()) / 2 ;
        add( tfTitle );

        BitmapTextMultiline tfMesage = PixelScene.createMultiline( generateMessage(server.players,server.maxPlayers), 8 );
        tfMesage.maxWidth = WIDTH - MARGIN * 2;
        tfMesage.measure();
        tfMesage.x = MARGIN;
        tfMesage.y = tfTitle.y + tfTitle.height() + MARGIN;
        add( tfMesage );

        float pos = tfMesage.y + tfMesage.height() + MARGIN;

      /*  for (int i=0; i < options.length; i++) {
            final int index = i;
            RedButton btn = new RedButton( options[i] ) {
                @Override
                protected void onClick() {
                    hide();
                    onSelect( index );
                }
            };
            btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
            add( btn );

            pos += BUTTON_HEIGHT + MARGIN;
        }
        */
        {
            RedButton btn = new RedButton("Connect" ) {
                @Override
                protected void onClick() {
                    hide();
                    onSelect( 1 );
                }
            };
            btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
            add( btn );

            pos += BUTTON_HEIGHT + MARGIN;
        }
        {
            RedButton btn = new RedButton( "Exit" ) {
                @Override
                protected void onClick() {
                    hide();
                    onSelect( 2 );
                }
            };
            btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
            add( btn );

            pos += BUTTON_HEIGHT + MARGIN;
        }

        resize( WIDTH, (int)pos );
    }
    //Fixme delete this function
    protected void onSelect( int index ) {
        if (index==1){
            if (!Client.connect(serverInfo)){
                scene.add(new WndError("Can't connect"));
            }else{
                NetworkScanner.stop();
                StartScene.startNewGame();
            }
        }

    };
}
