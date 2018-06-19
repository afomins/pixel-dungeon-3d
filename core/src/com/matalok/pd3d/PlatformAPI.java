//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
public abstract class PlatformAPI {
    //--------------------------------------------------------------------------
    public abstract String GetInternalAssetsPath();
    public abstract String GetPlatformName();
    public abstract void SetScreenLandscape();
    public abstract void SetScreenPortrait();
    public abstract void SetFullscreen(boolean value);
}
