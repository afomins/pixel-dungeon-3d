//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import com.matalok.pd3d.desc.DescHeap;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;

//------------------------------------------------------------------------------
public class LevelObjectPlant 
  extends LevelObjectDynamic {
    // *************************************************************************
    // UpdateCtx
    // *************************************************************************
    public static class UpdateCtx
      extends LevelObjectItem.UpdateCtx {
        // *********************************************************************
        // UpdateCtx
        // *********************************************************************
        public UpdateCtx(DescHeap desc) {
            super(desc);
        }
    }

    // *************************************************************************
    // LevelObjectPlant
    // *************************************************************************
    public LevelObjectPlant(String name, int pd_id) {
        super(name, pd_id);
    }

    // *************************************************************************
    // LevelObjectDynamic
    // *************************************************************************
    @Override public RenderableObject OnNewObject(IUpdateCtx desc) {
        return RenderableObjectType.PLANT.Create(desc.GetModelId());
    }
}
