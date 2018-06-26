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
