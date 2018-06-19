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
