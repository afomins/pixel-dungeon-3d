//------------------------------------------------------------------------------
package com.matalok.pd;

//------------------------------------------------------------------------------
import javax.microedition.khronos.opengles.GL10;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.utils.SystemTime;

//------------------------------------------------------------------------------
public class PdWrapper
  extends PixelDungeon {
    //**************************************************************************
    // PdWrapper
    //**************************************************************************
    public PdWrapper() {
        super(false);
    }

    //--------------------------------------------------------------------------
    public void RunOnCreate() {
        onCreate(null);
        onSurfaceChanged(null, 640, 480);
    }

    //--------------------------------------------------------------------------
    public void RunOnResume() {
        onResume();
    }

    //--------------------------------------------------------------------------
    public void RunOnPause() {
        onPause();
    }

    //--------------------------------------------------------------------------
    public void RunOnDestroy() {
        onDestroy();
    }

    //--------------------------------------------------------------------------
    public void RunOnDrawFrame() {
        onDrawFrame(null);
    }

    //**************************************************************************
    // Game
    //**************************************************************************
    @Override public void onDrawFrame(GL10 gl) {
        SystemTime.tick();
        long rightNow = SystemTime.now;
        step = (now == 0 ? 0 : rightNow - now);
        now = rightNow;
        step();
    }
}
