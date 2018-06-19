//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable;

//------------------------------------------------------------------------------
import java.util.HashSet;
import java.util.LinkedList;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public abstract class RenderableTemplate 
  implements RenderableObject.ITemplate {
    //**************************************************************************
    // RenderableTemplate
    //**************************************************************************
    public String name;
    public Float alpha;
    public Matrix4 init_transform;
    public Matrix4 runtime_transform;
    public HashSet<RenderableObject> objs;

    //--------------------------------------------------------------------------
    public RenderableTemplate(String name, Float alpha, boolean do_runtime_transform) {
        this.name = name;
        this.alpha = alpha;
        this.runtime_transform = do_runtime_transform ? new Matrix4() : null;
        this.init_transform = new Matrix4();
        this.objs = new HashSet<RenderableObject>(); 
    }

    //**************************************************************************
    // RenderableObject.ITemplate
    //**************************************************************************
    @Override public String GetName() {
        return name;
    }

    //--------------------------------------------------------------------------
    @Override public Float GetAlpha() {
        return alpha;
    }

    //--------------------------------------------------------------------------
    @Override public Matrix4 GetInitialTransform() {
        return init_transform;
    }

    //--------------------------------------------------------------------------
    @Override public Matrix4 GetRuntimeTransform() {
        return runtime_transform;
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateInitialTransform() {
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
    }

    //--------------------------------------------------------------------------
    @Override public int GetSize() {
        return objs.size();
    }

    //--------------------------------------------------------------------------
    @Override public boolean IsRegistered(RenderableObject obj) {
        return objs.contains(obj);
    }

    //--------------------------------------------------------------------------
    @Override public void Register(RenderableObject obj) {
        Utils.Assert(obj.GetTemplate() == this, 
          "Failed to register model, wrong template");
        Utils.Assert(!objs.contains(obj), 
          "Failed to register model, alredy in hash");
        objs.add(obj);
    }

    //--------------------------------------------------------------------------
    @Override public void Unregister(RenderableObject obj) {
        Utils.Assert(obj.GetTemplate() == this, 
          "Failed to unregister model, wrong template");
        Utils.Assert(objs.contains(obj), 
          "Failed to register model, not in hash");
        objs.remove(obj);
    }

    //--------------------------------------------------------------------------
    @Override public RenderableObject.ITemplate InheritOldTemplate(
      RenderableObject.ITemplate old_template) {
        // Inherit values from old template
        runtime_transform = ((RenderableTemplate)old_template).runtime_transform;
        objs = ((RenderableTemplate)old_template).objs;

        // Switch models to new template
        LinkedList<Disposable> kill_list = null; 
        for(RenderableObject o : objs) {
            if(kill_list == null) {
                // Collect old template models once
                kill_list = new LinkedList<Disposable>();
                o.SwitchTemplate(this, kill_list);
            } else {
                o.SwitchTemplate(this, null);
            }
        }

        // Dispose old model instances
        if(kill_list != null) {
            for(Disposable d : kill_list) {
                d.dispose();
            }
        }
        return this;
    }

    //--------------------------------------------------------------------------
    @Override public RenderableObject.ITemplate Finalize() {
        UpdateInitialTransform();
        return this;
    }

    //**********************************************************************
    // IManaged
    //**********************************************************************
    @Override public void OnCleanup() {
        name = null;
        alpha = null;
        init_transform = null;
        runtime_transform = null;

        objs.clear();
        objs = null;
    }
}
