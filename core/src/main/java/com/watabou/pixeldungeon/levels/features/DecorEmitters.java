package com.watabou.pixeldungeon.levels.features;

import static com.watabou.pixeldungeon.levels.Level.WIDTH;

import com.watabou.noosa.Game;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.Halo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class DecorEmitters {
    public static class Torch extends Emitter {

        private int pos;

        public Torch( int pos, int color ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );
            pos( p.x - 1, p.y + 3, 2, 0 );

            pour( FlameParticle.FACTORY, 0.15f );

            add( new Halo( 16, color, 0.2f ).point( p.x, p.y ) );
        }

        @Override
        public void update() {
            if (visible = Dungeon.visible[pos]) {
                super.update();
            }
        }
    }

    public static class Sink extends Emitter {

        private int pos;
        private float rippleDelay = 0;

        private static final Emitter.Factory factory = new Factory() {

            @Override
            public void emit( Emitter emitter, int index, float x, float y ) {
                WaterParticle p = (WaterParticle)emitter.recycle(WaterParticle.class );
                p.reset( x, y );
            }
        };

        public Sink( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );
            pos( p.x - 2, p.y + 1, 4, 0 );

            pour( factory, 0.05f );
        }
        public static final class WaterParticle extends PixelParticle {

            public WaterParticle() {
                super();

                acc.y = 50;
                am = 0.5f;

                color( ColorMath.random( 0xb6ccc2, 0x3b6653 ) );
                size( 2 );
            }

            public void reset( float x, float y ) {
                revive();

                this.x = x;
                this.y = y;

                speed.set( Random.Float( -2, +2 ), 0 );

                left = lifespan = 0.5f;
            }
        }
        @Override
        public void update() {
            if (visible = Dungeon.visible[pos]) {

                super.update();

                if ((rippleDelay -= Game.elapsed) <= 0) {
                    GameScene.ripple( pos + WIDTH ).y -= DungeonTilemap.SIZE / 2;
                    rippleDelay = Random.Float( 0.2f, 0.3f );
                }
            }
        }
    }

    public static class Smoke extends Emitter {

        private int pos;

        private static final Emitter.Factory factory = new Factory() {

            @Override
            public void emit( Emitter emitter, int index, float x, float y ) {
                SmokeParticle p = (SmokeParticle)emitter.recycle( SmokeParticle.class );
                p.reset( x, y );
            }
        };

        public Smoke( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );
            pos( p.x - 4, p.y - 2, 4, 0 );

            pour( factory, 0.2f );
        }

        @Override
        public void update() {
            if (visible = Dungeon.visible[pos]) {
                super.update();
            }
        }
    }

    public static final class SmokeParticle extends PixelParticle {

        public SmokeParticle() {
            super();

            color( 0x000000 );
            speed.set( Random.Float( 8 ), -Random.Float( 8 ) );
        }

        public void reset( float x, float y ) {
            revive();

            this.x = x;
            this.y = y;

            left = lifespan = 2f;
        }

        @Override
        public void update() {
            super.update();
            float p = left / lifespan;
            am = p > 0.8f ? 1 - p : p * 0.25f;
            size( 8 - p * 4 );
        }
    }
}
