//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.msg.MsgCommand;
import com.matalok.pd3d.msg.MsgGetScene;
import com.matalok.pd3d.msg.MsgUpdateSprites;

//------------------------------------------------------------------------------
public class SceneConnect 
  extends Scene {
    //**************************************************************************
    // SceneConnect
    //**************************************************************************
    public SceneConnect() {
        super("scene-connect");
    }

    // *************************************************************************
    // IProxyListener
    // *************************************************************************
    @Override public void OnConnected() {
        // Set sound and music
        MsgCommand msg = MsgCommand.CreateRequest();
        msg.sound = (Main.inst.cfg.app_sound_volume > 0.0f);
        msg.music = (Main.inst.cfg.app_music_volume > 0.0f);
        Main.inst.proxy_client.Send(msg);

        // Switch to new scene
        Main.inst.proxy_client.Send(
          MsgUpdateSprites.CreateRequest());
        Main.inst.proxy_client.Send(
          MsgGetScene.CreateRequest());
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    @Override public void OnActivateScene() {
        Main.inst.proxy_client.Start(
          2, Main.inst.cfg.server_addr, Main.inst.cfg.server_port);
        super.OnActivateScene();
    }
}
