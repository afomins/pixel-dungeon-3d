// -----------------------------------------------------------------------------
package com.matalok.pd3d;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.matalok.pd3d.node.GameNode;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// -----------------------------------------------------------------------------
public abstract class Camera 
  extends GameNode {
    // *************************************************************************
    // Camera
    // *************************************************************************
    protected PerspectiveCamera m_gdx_camera;
    protected Viewport m_viewport;

    // -------------------------------------------------------------------------
    public Camera() {
        super("camera", 1.0f);

        // Camera
        m_gdx_camera = new PerspectiveCamera(70.0f, 
          Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        m_gdx_camera.near = 0.2f;
        m_gdx_camera.far = Main.inst.cfg.camera_draw_distance;

        // Viewport
        m_viewport = new ScreenViewport(m_gdx_camera); 
    }

    // -------------------------------------------------------------------------
    public com.badlogic.gdx.graphics.Camera GetGdxCamera() {
        return m_gdx_camera;
    }

    // -------------------------------------------------------------------------
    // http://en.wikipedia.org/wiki/Back-face_culling
    private static Vector3 tmp_bfc = new Vector3();
    public boolean TestBackfaceCulling(Vector3 norm, Vector3 pos, 
      float threshold) {
        // Get camera-to-triangle vector
        Camera.tmp_bfc.set(m_gdx_camera.position).sub(pos);

        // Backface-culling test passes if dot-product is greater than threshold
        return (norm.dot(Camera.tmp_bfc) >= threshold);
    }

    // -------------------------------------------------------------------------
    // https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    public boolean TestFrustumCulling(Vector3 pos, float sphere_radius) {
        return m_gdx_camera.frustum.sphereInFrustum(pos, sphere_radius);
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodResize(int width, int height) {
        super.OnMethodResize(width, height);

        m_viewport.update(width, height);
        return true;
    }
}
