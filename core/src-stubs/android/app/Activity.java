package android.app;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Activity 
  extends ContextWrapper {

    public WindowManager getWindowManager() {
        return WindowManager.stub;
    }
    
    protected void setVolumeControlStream(int v0) {
    }

    protected void setContentView(View view) {
    }

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        
    }

    public void onResume() {
        // TODO Auto-generated method stub
        
    }

    public void onPause() {
        // TODO Auto-generated method stub
        
    }

    public void onDestroy() {
        // TODO Auto-generated method stub
        
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getSystemService(String name) {
        return null;
    }
    
    public void finish() {
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        
    }

    public void setRequestedOrientation(int requestedOrientation) {
    }

    public void setImmersive(boolean value) {
    }

    public final void runOnUiThread(Runnable action) {
    }

    public Window getWindow() {
        return null;
    }

    public SharedPreferences getPreferences(int mode) {
        return new SharedPreferences();
    }

    public void startActivity(Intent intent) {
        // TODO Auto-generated method stub
        
    }
}
