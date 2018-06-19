//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class SceneDisconnect 
  extends Scene {
    //**************************************************************************
    // SceneDisconnect
    //**************************************************************************
    public SceneDisconnect() {
        super("scene-disconnect");
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    @Override public void OnActivateScene() {
        if(Main.inst.proxy_client.IsConnected()) {
            Main.inst.proxy_client.Stop();
        }
        super.OnActivateScene();
    }
}
