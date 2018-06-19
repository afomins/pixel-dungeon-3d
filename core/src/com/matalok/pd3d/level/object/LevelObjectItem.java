//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.desc.Desc;
import com.matalok.pd3d.desc.DescHeap;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderable.model.template.MTmplSpriteVertical;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class LevelObjectItem 
  extends LevelObjectDynamic {
    // *************************************************************************
    // UpdateCtx
    // *************************************************************************
    public static class UpdateCtx
      implements LevelObject.IUpdateCtx {
        // *********************************************************************
        // UpdateCtx
        // *********************************************************************
        public DescHeap desc;

        //----------------------------------------------------------------------
        public UpdateCtx(DescHeap desc) {
            this.desc = desc;
        }

        // *********************************************************************
        // LevelObject.IUpdateCtx
        // *********************************************************************
        @Override public int GetCellId() {
            return desc.pos;
        }

        //----------------------------------------------------------------------
        @Override public int GetPdId() {
            return desc.pos;
        }

        //----------------------------------------------------------------------
        @Override public int GetModelId() {
            return desc.sprite_id;
        }

        //----------------------------------------------------------------------
        @Override public Desc GetDescriptor() {
            return desc;
        }

        //----------------------------------------------------------------------
        @Override public boolean IsDirty() {
            return false;
        }
    }

    // *************************************************************************
    // LevelObjectItem
    // *************************************************************************
    public LevelObjectItem(String name, int pd_id) {
        super(name, pd_id);
    }

    // *************************************************************************
    // LevelObjectDynamic
    // *************************************************************************
    @Override public RenderableObject OnNewObject(IUpdateCtx desc) {
        RenderableObject robj = RenderableObjectType.ITEM.Create(desc.GetModelId());

        // Vertical items should be aligned by terrain
        if(robj.GetTemplate() instanceof MTmplSpriteVertical) {
            // Get parent cell object
            LevelObjectCell cell = (LevelObjectCell)SgGetParent();
            Utils.Assert(cell != null, 
              "Failed to create item model, no parent cell :: item=%s", SgGetNameId());
    
            // Get terrain object
            LevelObjectTerrain terrain = cell.GetTerrain();
            Utils.Assert(terrain != null, 
              "Failed to create item model, no terrain :: item=%s cell=%s", 
              SgGetNameId(), cell.SgGetNameId());

            // Rotate
            float rotation = 
              terrain.GetObjectAlignment(RenderableObjectType.ITEM).rotation + 105.0f;
            robj.GetLocalRot(true)
              .mul(new Quaternion(Vector3.Y, rotation));
        }
        return robj;
    }
}
