package com.watabou.pixeldungeon.scenes;

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.network.NetworkScanner;
import com.watabou.pixeldungeon.network.ServerInfo;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndConnectServer;

import java.util.ArrayList;
import java.util.List;


/* TODO
    * Add UpdateButton
    * Add Next/Previous buttons to navigate on server  menu
    * TABLE_SIZE should be calculated based on the height of the screen
*/

public class ConnectScene extends PixelScene implements NetworkScanner.ServicesListener {

    private static final int DEFAULT_COLOR	= 0xCCCCCC;
    private static final int TABLE_SIZE=6;

    private static final String TXT_TITLE		= "Servers";
    private static final String TXT_NO_GAMES	= "No servers found.";
    private static final String TXT_ERROR	    = "Servers search error.";
    private static final String TXT_SEARCHING	= "Searching...";
    private static final String TXT_WIFI_DISABLED	= "WI-FI is not connected.";

    private static final float ROW_HEIGHT_L	= 22;
    private static final float ROW_HEIGHT_P	= 28;

    private static final float MAX_ROW_WIDTH	= 180;

    private static final float GAP	= 4;

    private Archs archs;

    private Group page;
    private ArrayList<Record> rows  = new ArrayList<>();
    private BitmapText title;
    private int width;
    private int height;

    public void CreateCenterText(int cameraWidth, int cameraHeight,String text){
        BitmapText title = PixelScene.createText( text, 8 );
        title.hardlight( DEFAULT_COLOR );
        title.measure();
        title.x = align( (cameraWidth - title.width()) / 2 );
        title.y = align( (cameraHeight - title.height()) / 2 );
        add( title );
    }

    protected void drawServers(){

        ServerInfo[] serverList;
        if (title!=null) {
            title.kill();
        }
        for (Record row:rows) {
            row.kill();
        }
        List<ServerInfo> list;
        list= NetworkScanner.getServerList();
        serverList=list.toArray(new ServerInfo[0]); //Todo use only List<?>
        if (serverList.length > 0) {

            float rowHeight = PixelDungeon.landscape() ? ROW_HEIGHT_L : ROW_HEIGHT_P;

            float left = (width - Math.min(MAX_ROW_WIDTH, width)) / 2 + GAP;
            float top = align((height - rowHeight * Math.min(serverList.length,TABLE_SIZE)) / 2);

            title = PixelScene.createText(TXT_TITLE, 9);
            title.hardlight(Window.TITLE_COLOR);
            title.measure();
            title.x = align((width - title.width()) / 2);
            title.y = align(top - title.height() - GAP);
            add(title);

            int pos = 0;

            for (int i = 0; i < Math.min(serverList.length,TABLE_SIZE); i += 1) {
                Record row = new Record(pos, false, serverList[i], this);
                row.setRect(left, top + pos * rowHeight, width - left * 2, rowHeight);
                add(row);
                rows.add(row);
                pos++;
            }

            if (serverList.length > TABLE_SIZE) {
                //todo previous/next
            }

        } else {

            title = PixelScene.createText(TXT_SEARCHING, 8);
            title.hardlight(DEFAULT_COLOR);
            title.measure();
            title.x = align((width - title.width()) / 2);
            title.y = align((height - title.height()) / 2);
            add(title);

        }

    }
    @Override
    public void create() {

        super.create();

        Music.INSTANCE.play( Assets.THEME, true );
        Music.INSTANCE.volume( 1f );

        uiCamera.visible = false;

        width = Camera.main.width;
        height = Camera.main.height;

        archs = new Archs();
        archs.setSize(width, height);
        add( archs );
      //  if (!NSD.isWifiConnected()){
        //    CreateCenterText(width, height,TXT_WIFI_DISABLED);
        //} else
        {
            if (!NetworkScanner.start(this)) {
                CreateCenterText(width, height,TXT_ERROR);
            } else {
                drawServers();
            }
        }
        ExitButton btnExit = new ExitButton(){
            @Override
            public void onClick(){ onBackPressed(); }
        };
        btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
        add( btnExit );

        fadeIn();
    }

    @Override
    protected void onBackPressed() {
        NetworkScanner.stop();
        PixelDungeon.switchNoFade( TitleScene.class );
    }

    private boolean needRedraw = false;
    public Window showWindow = null;

    @Override
    public void update() {
        super.update();
        if (showWindow!=null){
            addToFront(showWindow);
            bringToFront(showWindow);
            showWindow = null;
        }
        else if (needRedraw && !hasWindow()){
            drawServers();
            needRedraw = false;
        }
    }

    private boolean hasWindow() {
        for (Gizmo elem: members){
            if (elem instanceof Window){
                return true;
            }
        }
        return false;
    }

    @Override
    public void OnServerConnected(ServerInfo info) {
        needRedraw = true;
    }


    public static class Record extends Button {
        public Scene ConnectScene;

        private static final float GAP	= 4;

        private static final int TEXT_WIN	= 0xFFFF88;
        private static final int TEXT_LOSE	= 0xCCCCCC;
        private static final int FLARE_WIN	= 0x888866;
        private static final int FLARE_LOSE	= 0x666666;

        private ServerInfo rec;

        private ItemSprite shield;
        private Flare flare;
        private BitmapText position;
        private BitmapTextMultiline desc;

        public Record(int pos, boolean withFlare, ServerInfo rec, Scene scene ) {
            super();
            this.ConnectScene=scene;
            this.rec = rec;

            if (withFlare) {
                flare = new Flare( 6, 24 );
                flare.angularSpeed = 90;
              //  flare.color( rec.win ? FLARE_WIN : FLARE_LOSE );
                flare.color( FLARE_WIN);
                addToBack( flare );
            }

            position.text( Integer.toString( pos+1 ) );
            position.measure();

            desc.text( rec.name );
            desc.measure();

            if (rec.haveChallenges) {
                shield.view( ItemSpriteSheet.AMULET, null );
                position.hardlight( TEXT_WIN );
                desc.hardlight( TEXT_WIN );
            } else {
                shield.view( ItemSpriteSheet.CHEST, null );
                position.hardlight( TEXT_LOSE );
                desc.hardlight( TEXT_LOSE );
            }
        }

        @Override
        protected void createChildren() {

            super.createChildren();

            shield = new ItemSprite( ItemSpriteSheet.CHEST, null );
            add( shield );

            position = new BitmapText( PixelScene.font1x );
            add( position );

            desc = createMultiline( 9 );
            add( desc );

        }

        @Override
        protected void layout() {

            super.layout();

            shield.x = x;
            shield.y = y + (height - shield.height) / 2;

            position.x = align( shield.x + (shield.width - position.width()) / 2 );
            position.y = align( shield.y + (shield.height - position.height()) / 2 + 1 );

            if (flare != null) {
                flare.point( shield.center() );
            }

            desc.x = shield.x + shield.width + GAP;
            //desc.maxWidth = (int)(shield.x - desc.x);
            desc.measure();
            desc.y = position.y + position.baseLine() - desc.baseLine();
        }

        @Override
        protected void onClick() {
            ((ConnectScene)this.ConnectScene).showWindow =  (new WndConnectServer( ConnectScene, rec));
        }
    }
}
