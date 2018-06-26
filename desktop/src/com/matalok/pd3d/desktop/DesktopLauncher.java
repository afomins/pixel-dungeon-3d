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
package com.matalok.pd3d.desktop;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.matalok.pd3d.Config;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformAPI;

//------------------------------------------------------------------------------
public class DesktopLauncher {
    public static void main(String[] arg) {
        // Hack: manually initialize "Gdx.files" before reading config
        Gdx.files = new LwjglFiles();
        Config pd3d_cfg = Config.Load();

        // Libgdx config
        LwjglApplicationConfiguration libgdx_config = 
          new LwjglApplicationConfiguration();
        libgdx_config.title = "PixelDungeon 3D | " + Config.version;
        libgdx_config.width = (pd3d_cfg.app_landscape) ? 
          pd3d_cfg.app_width : pd3d_cfg.app_height;
        libgdx_config.height = (pd3d_cfg.app_landscape) ? 
          pd3d_cfg.app_height : pd3d_cfg.app_width;
        libgdx_config.vSyncEnabled = false;
        libgdx_config.foregroundFPS = 0;
        libgdx_config.backgroundFPS = 0;

        // Platform API
        PlatformAPI platform_api = new PlatformAPI() {
            //------------------------------------------------------------------
            @Override public String GetPlatformName() {
                return "desktop";
            }

            //------------------------------------------------------------------
            @Override public String GetInternalAssetsPath() {
                return "bin/";
            }

            //------------------------------------------------------------------
            @Override public void SetScreenLandscape() {
            }

            //------------------------------------------------------------------
            @Override public void SetScreenPortrait() {
            }

            //------------------------------------------------------------------
            @Override public void SetFullscreen(boolean value) {
            }
        };
        new LwjglApplication(new Main(platform_api, pd3d_cfg), libgdx_config);
    }
}
