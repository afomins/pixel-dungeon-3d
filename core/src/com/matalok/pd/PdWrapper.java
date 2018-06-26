/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
