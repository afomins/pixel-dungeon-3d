//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.level.LevelDirection;
import com.matalok.pd3d.level.LevelTrashBin;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public abstract class LevelObjectDynamic 
  extends LevelObject {
    // *************************************************************************
    // LevelObjectDynamci
    // *************************************************************************
    protected LevelDirection m_direction;
    protected float m_model_move_duration;
    private GameNode m_scheduled_rotate_target;
    private RenderableObject m_shadow;

    //--------------------------------------------------------------------------
    public LevelObjectDynamic(String name, int pd_id) {
        super(name, pd_id);

        // Set initial direction
        m_direction = LevelDirection.NORTH;
        Rotate(0, 0.0f, null);

        // Default move duration
        m_model_move_duration = Main.inst.cfg.model_move_duration;
    }

    //--------------------------------------------------------------------------
    public LevelObjectCell GetParentCell() {
        return (LevelObjectCell)SgGetParent(LevelObjectCell.class);
    }

    //--------------------------------------------------------------------------
    public LevelDirection GetDirection() {
        return m_direction;
    }

    //--------------------------------------------------------------------------
    public LevelDirection GetDirection(boolean forward) {
        return forward ? m_direction : m_direction.Opposite();
    }

    //--------------------------------------------------------------------------
    public RenderableObject GetMainObject() {
        return (RenderableObject)SgGetFirstChild();
    }

    //--------------------------------------------------------------------------
    public void Rotate(int dir) {
        Rotate(dir, Main.inst.cfg.model_rotate_duration, null);
    }

    //--------------------------------------------------------------------------
    public void Rotate(int dir, float duration, final Tweener.Callback cb) {
        // Rotate to new direction
        m_direction = m_direction.Rotate(dir);

        // Build target quaternion with new angle
        Quaternion target_rot = 
          new Quaternion(Vector3.Y, m_direction.GetAngle());

        // Make sure we select shortest rotation path
        // https://stackoverflow.com/questions/20757240/tweening-a-rotation-with-quaternions
        if(target_rot.dot(GetLocalRot()) < 0) {
            target_rot.x = -target_rot.x;
            target_rot.y = -target_rot.y;
            target_rot.z = -target_rot.z;
            target_rot.w = -target_rot.w;
        }

        // Start movement animation
        OnStartMovementAnim();

        // Rotate
        TweenRot(target_rot, duration, cb);
    }

    //--------------------------------------------------------------------------
    public void ScheduleRotate(GameNode target) {
        Logger.d("Scheduling rotate to target :: obj=%s target=%s", 
          SgGetNameId(), target.SgGetNameId());
        m_scheduled_rotate_target = target;
    }

    //--------------------------------------------------------------------------
    public int GetRotationToTarget(GameNode target) {
        // Check easy scenario when target is located on neighbor cell
        LevelObjectCell cur_cell = GetParentCell();
        LevelObjectCell target_cell = (target instanceof LevelObjectCell) ? 
          (LevelObjectCell)target : (LevelObjectCell)target.SgGetParent(LevelObjectCell.class);
        LevelDirection neighbor_dir = cur_cell.IsNeighbor(target_cell);
        if(cur_cell != null && target_cell != null && neighbor_dir != null) {
            return GetDirection().GetDiff(neighbor_dir);
        }

        // Get distance between current position and target
        Vector3 cur_pos = GetGlobalPos();
        Vector3 target_pos = target.GetGlobalPos();
        float diff_x = target_pos.x - cur_pos.x;
        float diff_z = target_pos.z - cur_pos.z;

        // Minimize rotation diff so that model makes minimal 
        // rotation (i.e. +45 instead of -315)
        float cell_angle = MathUtils.atan2(diff_x, diff_z) * MathUtils.radiansToDegrees - 90.0f;

        // Rotate so that target is in front of us
        float angle = GetDirection().GetAngle();
        int step = 0;
        for(int i = 0; i < 2; i++) {
            float angle_diff = Utils.MinimizeRotAngle(cell_angle - angle);
            float step_size = LevelDirection.GetStepSize() * 0.7f;
            step += (int)(angle_diff / step_size);
            angle += step * LevelDirection.GetStepSize();
        }
        return step;
    }

    //--------------------------------------------------------------------------
    public void Rotate(GameNode target) {
        Rotate(GetRotationToTarget(target));
    }

    //--------------------------------------------------------------------------
    public void OnUpdateBegin(IUpdateCtx ctx) {
    }

    //--------------------------------------------------------------------------
    public void OnUpdateEnd(IUpdateCtx ctx, LevelTrashBin trash_bin) {
    }

    //--------------------------------------------------------------------------
    public RenderableObject OnNewObject(IUpdateCtx ctx) {
        return null;
    }

    //--------------------------------------------------------------------------
    public void OnNewCell(IUpdateCtx desc, LevelObjectCell new_cell) {
        // Relocate object to new cell
        new_cell.SgRelocateChild(this);

        // Start movement animation
        OnStartMovementAnim();

        // Move object to the center of new cell
        Vector3 pos = new Vector3();
//        pos.y = GetLocalPos().y;
        TweenPos(pos, m_model_move_duration, null);
    }

    //--------------------------------------------------------------------------
    public void OnStartMovementAnim() {
    }

    // *************************************************************************
    // LevelObject
    // *************************************************************************
    @Override public void OnUpdate(IUpdateCtx ctx, LevelTrashBin trash_bin) {
        super.OnUpdate(ctx, trash_bin);

        //......................................................................
        //
        // #1 - Begin
        //
        OnUpdateBegin(ctx);

        //......................................................................
        //
        // #2 - Create new model if needed
        //
        RenderableObject old_robj = GetMainObject();
        RenderableObject new_robj = null;

        // Create new model if ..
        if((old_robj == null) ||                            // .. there was no model before
          (old_robj.GetObjectId() != ctx.GetModelId())) {   // .. or model-id has changed (e.g. hero equipped new armor)
            // Get new model
            new_robj = OnNewObject(ctx);
            if(new_robj != null && new_robj == old_robj) {
                new_robj = null;
            }

            // Process new model
            if(new_robj != null) {
                // Handle old model
                if(old_robj != null) {
                    // Copy to params from to new model
                    old_robj.CopyParamsTo(new_robj);

                    // Get rid of old model
                    trash_bin.Put(old_robj, false);
                }

                // Make sure that new model is first child
                GameNode first = GetMainObject();
                if(first != null) {
                    new_robj.SgSetPriority(first.SgGetPriority() - 1);
                }
                SgAddChild(new_robj);

                // Fade-in new model
                new_robj.FadeIn();
            }
        }

        //......................................................................
        //
        // #3 - Move object to new parent cell
        //

        // Get owner cell
        LevelObjectCell cell = (LevelObjectCell)SgGetParent();
        Utils.Assert(cell != null, 
          "Failed to update dynamic object, no parent cell :: char=%s", SgGetNameId());

        // Move char to new cell
        int old_cell_id = cell.GetPdId();
        int new_cell_id = ctx.GetCellId();
        if(new_cell_id != old_cell_id) {
            LevelObjectCell new_cell = cell.GetCell(new_cell_id);
            Utils.Assert(new_cell != null, 
              "Failed to update dynamic object, new parent cell missing :: char=%s old-cell-id=%d new-cell-id=%d", 
              SgGetNameId(), old_cell_id, new_cell_id);

            // Handle new cell
            OnNewCell(ctx, new_cell);
        }

        //......................................................................
        //
        // #4 - End
        //
        OnUpdateEnd(ctx, trash_bin);

        // Enable shadow
        if(Main.inst.cfg.model_has_shadow && m_shadow == null) {
            m_shadow = (RenderableObject)SgAddChild(
              RenderableObjectType.SHADOW.Create());
        }
    }

    //**************************************************************************
    // GameNode
    //**************************************************************************
    @Override public void CopyParamsTo(GameNode dest) {
        ((LevelObjectDynamic)dest).m_direction = m_direction;
        super.CopyParamsTo(dest);
    }

    //--------------------------------------------------------------------------
    @Override public boolean IsStill() {
        // Dynamic object is considered to be still if main model is also still
        RenderableObject model = GetMainObject();
        boolean is_model_still = (model != null) ? model.IsStill() : true;
        return (super.IsStill() && is_model_still);
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Schedule rotation
        if(m_scheduled_rotate_target != null) {
            Rotate(m_scheduled_rotate_target);
            m_scheduled_rotate_target = null;
        }
        return true;
    }
}
