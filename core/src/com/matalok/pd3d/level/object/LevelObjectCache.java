//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class LevelObjectCache
  extends UtilsClass.Cache<Integer, LevelObject>
  implements IManaged {
    // *************************************************************************
    // LevelObjectCache
    // *************************************************************************
    public LevelObjectCache(String name) {
        super(name, false);
    }

    //--------------------------------------------------------------------------
    public LevelObject Put(LevelObject obj) {
        return Put(obj.GetPdId(), obj);
    }

    //--------------------------------------------------------------------------
    public LevelObject Delete(LevelObject obj) {
        return Delete(obj.GetPdId(), false);
    }

    // *************************************************************************
    // UtilsClass.Cache
    // *************************************************************************
    @Override protected String ToString(LevelObject obj) {
        return obj.SgGetNameId();
    }

    // *************************************************************************
    // IManaged
    // *************************************************************************
    @Override public void OnCleanup() {
        Clear();
    }
}
