//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import java.util.HashMap;
import com.matalok.pd3d.renderable.RenderableTemplate;
import com.matalok.pd3d.renderable.model.ModelAnim;

//------------------------------------------------------------------------------
public abstract class MTmpl 
  extends RenderableTemplate {
    //**************************************************************************
    // Template
    //**************************************************************************
    public String default_anim;
    public HashMap<String, ModelAnim.Template> anim_templates;

    //--------------------------------------------------------------------------
    public MTmpl(String name, Float alpha, boolean do_runtime_transform) {
        super(name, alpha, do_runtime_transform);
        anim_templates = new HashMap<String, ModelAnim.Template>();
    }

    //----------------------------------------------------------------------
    public ModelAnim.Template SetAnim(String name, int fps, boolean is_looped) {
        if(default_anim == null || name.equals("idle")) {
            default_anim = name;
        }

        ModelAnim.Template anim = 
          new ModelAnim.Template(fps, is_looped);
        anim_templates.put(name, anim);
        return anim;
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

        for(ModelAnim.Template t : anim_templates.values()) {
            t.OnCleanup();
        }
        anim_templates.clear();
        anim_templates = null;
        default_anim = null;
    }
}
