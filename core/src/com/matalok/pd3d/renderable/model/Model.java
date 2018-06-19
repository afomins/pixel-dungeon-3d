//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model;

//------------------------------------------------------------------------------
import java.util.LinkedList;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderable.model.template.MTmpl;

//------------------------------------------------------------------------------
public class Model 
  extends RenderableObject {
    // *************************************************************************
    // Model
    // *************************************************************************
    private ModelStack m_model_stack;
    private RendererModel m_cur_model;

    //--------------------------------------------------------------------------
    public Model(String name, RenderableObjectType type, int model_id, 
      MTmpl template, Renderer.Layer renderer_type) {
        super(name, type, model_id, template, renderer_type);

        // Model stack
        m_model_stack = new ModelStack(template);
        m_model_stack.StartAnim(template.default_anim);

        // Get bounds of 1st model in stack
        m_bounds = new Bounds(m_model_stack
          .GetFirstRendererModel().inst
          .calculateBoundingBox(new BoundingBox()));
    }

    //--------------------------------------------------------------------------
    public void SwitchTemplate(ITemplate new_template, LinkedList<Disposable> kill_list) {
        super.SwitchTemplate(new_template, kill_list);

        m_model_stack.SwitchTemplate(new_template, kill_list);
        m_cur_model = null;
    }

    //--------------------------------------------------------------------------
    public ModelStack GetModelStack() {
        return m_model_stack;
    }

    //**************************************************************************
    // GameNode
    //**************************************************************************
    @Override public long OnPreDelete(boolean delete_instantly) {
        super.OnPreDelete(delete_instantly);

        float duration = Main.inst.cfg.model_fade_out_duration;
        TweenAlpha(0.0f, duration, null);
        return Utils.SecToMsec(duration);
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Get model instance
        RendererModel old_model = m_cur_model;
        m_cur_model = (m_model_stack != null) ? m_model_stack.Update() : null;
        if(m_cur_model == null) {
            return true;
        }

        // Get runtime transform of the template
        Matrix4 runtime_transform = ((MTmpl)m_template).runtime_transform;

        // Update transformation
        if(IsGlobalTransformDirty() || old_model != m_cur_model || 
          runtime_transform != null) {
            // Apply global transformation
            m_cur_model.inst.transform.set(GetGlobalTransform());

            // Apply runtime transformation from template
            if(runtime_transform != null) {
                m_cur_model.inst.transform.mul(runtime_transform);
            }

            // Update center of bounding box
            m_bounds.UpdateCenterGlobal(GetGlobalPos());
        }

        // Update transparency
        m_cur_model.SetAlphaBlending(GetGlobalAlpha());

        // Add to renderer
        if(IsRenderAllowed()) {
            boolean is_visible = IsVisibleByCamera(ctx.GetCamera(m_renderer_layer));
            ctx.AddLayerObject(m_cur_model, m_renderer_layer, 
              is_visible, m_model_stack.GetTriangleNum());
        }

        // Exclude fading models from packed tiles 
        if(m_packed_tile != null && m_packed_tile_exclude_fading && IsFading()) {
            m_packed_tile.RemoveNode(m_packed_tile_idx);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodSelect(SelectCtx ctx) {
        // Filter out blacklisted nodes
        if(ctx.blacklist.contains(this)) {
            return true;
        }

        // Ignore invisible models
        if(!IsVisibleByCamera(ctx.camera)) {
            return true;
        }

        // Test if model is closer than previously selected one
        float dist2 = ctx.ray.origin.dst2(m_bounds.center_global);
        if (dist2 > ctx.distance) {
            return true;
        }

        // Test if selection ray is inside the bounding box
        if(Intersector.intersectRayBoundsFast(
          ctx.ray, m_bounds.center_global, m_bounds.dimensions)) {
            ctx.selected_node = this;
            ctx.distance = dist2;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_model_stack.OnCleanup();
        m_model_stack = null;
        m_cur_model = null;
        return true;
    }
}
