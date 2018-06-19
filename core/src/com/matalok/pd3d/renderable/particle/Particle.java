//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.particle;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter.EmissionMode;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererParticle;

//------------------------------------------------------------------------------
public class Particle
  extends RenderableObject {
    //**************************************************************************
    // Particle
    //**************************************************************************
    private RendererParticle m_pfx;

    //--------------------------------------------------------------------------
    public Particle(String name, RenderableObjectType type, int model_id, 
      ParticleTmpl template, Renderer.Layer renderer_type) {
        super(name, type, model_id, template, renderer_type);

        // Instantiate & start particle
        m_pfx = new RendererParticle(template.pfx.inst);
        m_pfx.inst.init();
        m_pfx.inst.start();

        // Get bounds
        float size = 0.5f;
        m_bounds = new Bounds(new BoundingBox(
          new Vector3(-size, -size, -size), new Vector3(size, size, size)));
    }

    //--------------------------------------------------------------------------
    public RendererParticle GetPfx() {
        return m_pfx;
    }

    //**************************************************************************
    // GameNode
    //**************************************************************************
    @Override public long OnPreDelete(boolean delete_instantly) {
        super.OnPreDelete(delete_instantly);

        return m_pfx.StopEmitter();
    }

    //--------------------------------------------------------------------------
    @Override public boolean IsExpired() {
        // Not expired
        if(m_pfx.emitter.getEmissionMode() == EmissionMode.Enabled && 
          m_pfx.emitter.isContinuous()) {
            return false;
        }

        // Expired
        if(m_pfx.emitter.getEmissionMode() == EmissionMode.Disabled) {
            return true;
        }

        // Expired if complete
        return m_pfx.inst.isComplete();
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Update transform
        if(IsGlobalTransformDirty()) {
            m_pfx.inst.setTransform(GetGlobalTransform());
            m_bounds.UpdateCenterGlobal(GetGlobalPos());
        }

        // Add to renderer
        if(IsRenderAllowed()) {
            ctx.AddLayerObject(m_pfx, Renderer.Layer.PARTICLE,
              IsVisibleByCamera(ctx.GetCamera(m_renderer_layer)), 0);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_pfx.OnCleanup();
        m_pfx = null;
        return true;
    }
}
