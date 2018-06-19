//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class AndroidLauncher extends AndroidApplication {
    @Override protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hack: manually initialize "Gdx.files" before reading config
        Gdx.files = new AndroidFiles(getAssets(), getFilesDir().getAbsolutePath());
        Config pd3d_cfg = Config.Load();

        // Libgdx config
        AndroidApplicationConfiguration libgdx_config = 
          new AndroidApplicationConfiguration();

        // Platform API
        PlatformAPI platform_api = new PlatformAPI() {
            //------------------------------------------------------------------
            @Override public String GetInternalAssetsPath() {
                return "";
            }

            //------------------------------------------------------------------
            @Override public String GetPlatformName() {
                return "android";
            }

            //------------------------------------------------------------------
            @Override public void SetScreenLandscape() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            //------------------------------------------------------------------
            @Override public void SetScreenPortrait() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            //------------------------------------------------------------------
            @Override public void SetFullscreen(boolean value) {
                setImmersive(value);
            }
        };

//        platform_api.SetFullscreen(true);
        initialize(new Main(platform_api, pd3d_cfg), libgdx_config);
    }
}
