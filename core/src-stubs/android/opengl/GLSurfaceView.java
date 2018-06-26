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

package android.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.view.View;

public class GLSurfaceView 
  extends View {

    public interface Renderer {

        void onDrawFrame(GL10 gl);

        void onSurfaceChanged(GL10 gl, int width, int height);

        void onSurfaceCreated(GL10 gl, EGLConfig config);

    }

    public GLSurfaceView(Context context) {
        // TODO Auto-generated constructor stub
    }

    public void setEGLContextClientVersion(int i) {
        // TODO Auto-generated method stub
    }

    public void setEGLConfigChooser(boolean b) {
        // TODO Auto-generated method stub
        
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        // TODO Auto-generated method stub
        
    }

    public void setOnTouchListener(View.OnTouchListener l) {
        // TODO Auto-generated method stub
        
    }

    public void onResume() {
        // TODO Auto-generated method stub
        
    }

    public void onPause() {
        // TODO Auto-generated method stub
        
    }

}
