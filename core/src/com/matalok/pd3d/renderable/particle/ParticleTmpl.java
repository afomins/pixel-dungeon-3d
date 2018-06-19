//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.particle;

//------------------------------------------------------------------------------
import com.matalok.pd3d.desc.DescPfxMutator;
import com.matalok.pd3d.renderable.RenderableTemplate;
import com.matalok.pd3d.renderer.RendererParticle;

//------------------------------------------------------------------------------
public class ParticleTmpl 
  extends RenderableTemplate {
    //**************************************************************************
    // PTmpl
    //**************************************************************************
    public RendererParticle pfx;

    //--------------------------------------------------------------------------
    public ParticleTmpl(String name, String path, DescPfxMutator mutator) {
        super(name, 1.0f, false);

        // Create particle
        pfx = new RendererParticle(
          RendererParticle.manager.Load(path));

        // Mutate particle
        if(mutator != null) {
            pfx.ApplyMutator(mutator);
        }
    }

    //**************************************************************************
    // ModelBase.ITemplate
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.idt();
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
    }

    //**********************************************************************
    // IManaged
    //**********************************************************************
    @Override public void OnCleanup() {
        super.OnCleanup();

        pfx.OnCleanup();
        pfx = null;
    }
}
