//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.renderer.Renderer;

//------------------------------------------------------------------------------
public class GuiCamera
  extends Camera {
    //**************************************************************************
    // LevelCamera
    //**************************************************************************
    public GuiCamera() {
        super();
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Register self as level camera
        ctx.SetCamera(Renderer.CameraType.GUI, this);

        // Default static camera without target
        m_gdx_camera.up.set(new Vector3(0.0f, 0.0f, -1.0f));
        m_gdx_camera.position.set(0.0f, 2.0f, 0.0f);
        m_gdx_camera.lookAt(0.0f, 0.0f, 0.0f);

        // Update camera matrix
        m_gdx_camera.update();
        return true;
    }
}
