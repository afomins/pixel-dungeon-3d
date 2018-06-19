//------------------------------------------------------------------------------
package com.matalok.pd3d.node;

//-------------------------------------------------------------------------------
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class GameNodeAttrib<T> 
  implements IManaged {
    //**************************************************************************
    // Stack
    //**************************************************************************
    public static class Stack<T>
      implements IManaged {
        //----------------------------------------------------------------------
        public GameNodeAttrib<T> init;
        public GameNodeAttrib<T> local;
        public GameNodeAttrib<T> init_local;
        public GameNodeAttrib<T> global;

        //----------------------------------------------------------------------
        public Stack(GameNodeAttrib<T> init, GameNodeAttrib<T> local, 
          GameNodeAttrib<T> init_local, GameNodeAttrib<T> global) {
            this.init = init;
            this.local = local;
            this.init_local = init_local;
            this.global = global;
        }

        //----------------------------------------------------------------------
        @Override public void OnCleanup() {
            if(init != null) init.OnCleanup();
            if(local != null) local.OnCleanup();
            if(init_local != null) init_local.OnCleanup();
            if(global != null) global.OnCleanup();
            init = local = init_local = global = null;
        }
    }

    //**************************************************************************
    // GameNodeAttrib
    //**************************************************************************
    public T value;
    public boolean is_dirty;

    //--------------------------------------------------------------------------
    public GameNodeAttrib(T value) {
        SetDirty().value = value;
    }

    //--------------------------------------------------------------------------
    public boolean IsDirty() {
        return is_dirty;
    }

    //--------------------------------------------------------------------------
    public GameNodeAttrib<T> SetDirty(boolean is_dirty) {
        this.is_dirty = is_dirty;
        return this;
    }

    //--------------------------------------------------------------------------
    public GameNodeAttrib<T> SetDirty() {
        is_dirty = true;
        return this;
    }

    //--------------------------------------------------------------------------
    public GameNodeAttrib<T> ResetDirty() {
        is_dirty = false;
        return this;
    }

    //--------------------------------------------------------------------------
    public void CopyTo(GameNodeAttrib<T> dest) {
        dest.is_dirty = true;
    }

    //--------------------------------------------------------------------------
    @Override public void OnCleanup() {
        value = null;
    }
}
