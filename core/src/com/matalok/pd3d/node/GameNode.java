//------------------------------------------------------------------------------
package com.matalok.pd3d.node;

//-------------------------------------------------------------------------------
import java.util.HashSet;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderable.model.Model;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.renderer.RendererProfiler;
import com.matalok.pd3d.renderer.layer.RendererLayer;
import com.matalok.pd3d.shared.Logger;
import com.matalok.scenegraph.SgMethod;
import com.matalok.scenegraph.SgNode;

//------------------------------------------------------------------------------
public class GameNode 
  extends SgNode {
    //**************************************************************************
    // METHOD - RESIZE
    //**************************************************************************
    public static class ResizeCtx {
        //----------------------------------------------------------------------
        public int width, height;

        //----------------------------------------------------------------------
        public ResizeCtx Init(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
    }

    //----------------------------------------------------------------------
    public static SgMethod RESIZE = new SgMethod("resize", true, true, false) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            ResizeCtx ctx = (ResizeCtx)arg;
            return ((GameNode)node).OnMethodResize(ctx.width, ctx.height);
    }};

    //**************************************************************************
    // METHOD - RENDER
    //**************************************************************************
    public static class RenderCtx {
        //----------------------------------------------------------------------
        public Camera[] cameras;
        public RendererLayer[] layers;
        public ModelBatch model_batch;
        public DecalBatch decal_batch;
        public RendererProfiler profiler;

        //----------------------------------------------------------------------
        public void AddLayerObject(Object obj, Renderer.Layer type, 
          boolean is_visible, int child_num) {
            layers[type.ordinal()]
              .AddLayerObject(this, obj, is_visible, child_num);
        }

        //----------------------------------------------------------------------
        public Camera GetCamera(Renderer.Layer type) {
            return GetCamera(layers[type.ordinal()].GetCameraType());
        }

        //----------------------------------------------------------------------
        public Camera GetCamera(Renderer.CameraType cam_type) {
            return cameras[cam_type.ordinal()];
        }

        //----------------------------------------------------------------------
        public void SetCamera(Renderer.CameraType cam_type, Camera camera) {
            cameras[cam_type.ordinal()] = camera;
        }
    }

    //----------------------------------------------------------------------
    public static SgMethod RENDER = new SgMethod("render", true, true, false) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            RenderCtx ctx = (RenderCtx)arg;
            return ((GameNode)node).OnMethodRender(ctx);
    }};

    //**************************************************************************
    // METHOD - POST-RENDER
    //**************************************************************************
    public static SgMethod POST_RENDER = new SgMethod("post-render", true, true, true) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            RenderCtx ctx = (RenderCtx)arg;
            return ((GameNode)node).OnMethodPostRender(ctx, pre_children);
    }};

    //**************************************************************************
    // METHOD - PAUSE
    //**************************************************************************
    public static SgMethod PAUSE = new SgMethod("pause", false, true, true) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            return ((GameNode)node).OnMethodPause();
    }};


    //**************************************************************************
    // METHOD - RESUME
    //**************************************************************************
    public static SgMethod RESUME = new SgMethod("resume", true, true, false) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            return ((GameNode)node).OnMethodResume();
    }};

    //**************************************************************************
    // METHOD - SELECT
    //**************************************************************************
    public static class SelectCtx {
        //----------------------------------------------------------------------
        public int screen_x;
        public int screen_y;
        public float distance;
        public Camera camera;
        public Ray ray;
        public GameNode selected_node;
        public HashSet<GameNode> blacklist;

        //----------------------------------------------------------------------
        public SelectCtx Init(int x, int y, Camera camera, HashSet<GameNode> blacklist) {
            this.screen_x = x;
            this.screen_y = y;
            this.distance = Float.MAX_VALUE;
            this.camera = camera;
            this.ray = camera.GetGdxCamera().getPickRay(x, y);
            this.blacklist = blacklist;
            return this;
        }
    }

    //--------------------------------------------------------------------------
    public static SgMethod SELECT = new SgMethod("select", false, true, true) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            SelectCtx ctx = (SelectCtx)arg;
            return ((GameNode)node).OnMethodSelect(ctx);
    }};

    //**************************************************************************
    // STATIC
    //**************************************************************************
    protected static Tweener.UserData no_tween = new Tweener.UserData(0) {
        public boolean IsFinished() {
            return true;
        }
    }; 

    //**************************************************************************
    // GameNode
    //**************************************************************************
    private Vector3 m_local_pos, m_global_pos;
    private Quaternion m_local_rot;
    private Vector3 m_local_scale;
    private GameNodeAttrib.Stack<Matrix4> m_transform;
    private GameNodeAttrib.Stack<Float> m_alpha;
    private boolean m_global_transform_forced_update;

    protected boolean m_do_dbg_render;
    protected boolean m_manage_expiration;
    protected Model m_dbg_axis;

    protected Tweener.UserData m_tween_move;
    protected Tweener.UserData m_tween_rotate;
    protected Tweener.UserData m_tween_scale;
    protected Tweener.UserData m_tween_alpha;

    protected Tweener m_tweener;

    protected Quaternion m_rotation_cache;
    protected float m_rotation_cache_duration;
    protected Tweener.Callback m_rotation_cache_cb;

    //--------------------------------------------------------------------------
    public GameNode(String name, float alpha) {
        super(name);
        // Local transformation
        m_local_pos = new Vector3();
        m_local_rot = new Quaternion();
        m_local_scale = new Vector3(1.0f, 1.0f, 1.0f);

        // Global transformation
        m_global_pos = new Vector3();

        // Transformation attributes
        m_transform = new GameNodeAttrib.Stack<Matrix4>(
          new GameNodeAttrib<Matrix4>(new Matrix4()),   // INIT
          new GameNodeAttrib<Matrix4>(new Matrix4()),   // LOCAL
          new GameNodeAttrib<Matrix4>(new Matrix4()),   // INIT_LOCAL
          new GameNodeAttrib<Matrix4>(new Matrix4()));  // GLOBAL

        // Transparency attributes
        m_alpha = new GameNodeAttrib.Stack<Float>(
          null,                                         // INIT
          new GameNodeAttrib<Float>(new Float(alpha)),  // LOCAL
          null,                                         // INIT_LOCAL
          new GameNodeAttrib<Float>(new Float(1.0f)));  // GLOBAL

        // Tweener
        m_tweener = new Tweener();
        m_tween_move = m_tween_rotate = 
          m_tween_scale = m_tween_alpha = no_tween;

        // Manage node expiration by default
        m_manage_expiration = true;
    }

    //--------------------------------------------------------------------------
    public void SetDbgRender(boolean flag) {
        m_do_dbg_render = flag;
    }

    //--------------------------------------------------------------------------
    public Tweener.UserData GetMoveTween() {
        return m_tween_move;
    }

    //--------------------------------------------------------------------------
    public boolean IsMoving() {
        return !m_tween_move.IsFinished();
    }

    //--------------------------------------------------------------------------
    public boolean IsRotating() {
        return !m_tween_rotate.IsFinished();
    }

    //--------------------------------------------------------------------------
    public boolean IsScaling() {
        return !m_tween_scale.IsFinished();
    }

    //--------------------------------------------------------------------------
    public boolean IsFading() {
        return !m_tween_alpha.IsFinished();
    }

    //--------------------------------------------------------------------------
    public boolean IsStill() {
        return !IsMoving() && !IsRotating() && !IsScaling();
    }

    //--------------------------------------------------------------------------
    public void TweenPos(Vector3 pos, float duration, final Tweener.Callback cb) {
        final boolean is_hero = (Main.inst.level.GetHero() == this);

        // Instant move
        if(duration <= 0.0f) {
            m_tween_move = new Tweener.UserData(0);
            GetLocalPos(true).set(pos);
            if(cb != null) {
                cb.OnComplete();
            }
            return;
        }

        // Tween move
        Tweener.Callback cb_new = new Tweener.Callback(this) {
            @Override public void OnComplete() {
                // Run callback
                m_tween_move = no_tween;
                if(cb != null) {
                    cb.OnComplete();
                }
                if(is_hero) {
                    Logger.d("Stopping hero movement");
                }
            }
        };
        m_tween_move = m_tweener.Start(
          this, null, GameNodeTweener.POS, duration, null, cb_new, pos);
        if(is_hero) {
            Logger.d("Starting hero movement");
        }
    }

    //--------------------------------------------------------------------------
    public void TweenRot(Quaternion rot, float duration, final Tweener.Callback cb) {
        // Cache new rotation if old one is not over yet
        if(IsRotating()) {
            m_rotation_cache = rot;
            m_rotation_cache_duration = duration;
            m_rotation_cache_cb = cb;
            return;
        }

        // Instant rotate
        if(duration <= 0.0f) {
            m_tween_rotate = no_tween;
            GetLocalRot(true).set(rot);
            return;
        }

        // Tween rotate
        Tweener.Callback cb_new = new Tweener.Callback(this) {
            @Override public void OnComplete() {
                // Run callback
                m_tween_rotate = no_tween;
                if(cb != null) {
                    cb.OnComplete();
                }

                // Run cached rotation
                if(m_rotation_cache != null) {
                    TweenRot(m_rotation_cache, m_rotation_cache_duration, 
                      m_rotation_cache_cb);
                    m_rotation_cache = null;
                }
            }
        };
        m_tween_rotate = m_tweener.Start(
          this, null, GameNodeTweener.ROT, duration, null, cb_new, rot); 
    }


    //--------------------------------------------------------------------------
    public void TweenScale(Vector3 scale, float duration, final Tweener.Callback cb) {
        // Instant move
        if(duration <= 0.0f) {
            m_tween_scale = new Tweener.UserData(0);
            GetLocalScale(true).set(scale);
            return;
        }

        // Tween move
        Tweener.Callback cb_new = new Tweener.Callback(this) {
            @Override public void OnComplete() {
                m_tween_scale = no_tween;
                if(cb != null) {
                    cb.OnComplete();
                }
            }
        };
        m_tween_scale = m_tweener.Start(
          this, null, GameNodeTweener.SCALE, duration, null, cb_new, scale); 
    }

    //--------------------------------------------------------------------------
    public void TweenAlpha(float dest, float duration, final Tweener.Callback cb) {
        // Instant fading
        if(duration <= 0.0f) {
            m_tween_alpha = new Tweener.UserData(0);
            SetLocalAlpha(dest);
            return;
        }

        // Tween move
        Tweener.Callback cb_new = new Tweener.Callback(this) {
            @Override public void OnComplete() {
                m_tween_alpha = no_tween;
                if(cb != null) {
                    cb.OnComplete();
                }
            }
        };

        // Tween fading
        m_tween_alpha = m_tweener.Start(this, GameNode.class, GameNodeTweener.ALPHA, 
          duration, null, cb_new, dest);
    }

    //--------------------------------------------------------------------------
    public void TweenAlpha(Float dest[], float duration, Tweener.Callback cb) {
        // Instant fading
        if(duration <= 0.0f) {
            SetLocalAlpha(dest[dest.length - 1]);
            return;
        }

        // Tween fading
        m_tweener.StartTimeline(this, GameNode.class, 
          GameNodeTweener.ALPHA, duration, cb, dest);
    }

    //--------------------------------------------------------------------------
    public void SetInitTransform(Matrix4 m) {
        m_transform.init.SetDirty().value.set(m);
    }

    //--------------------------------------------------------------------------
    public Vector3 GetLocalPos() {
        return m_local_pos;
    }

    //--------------------------------------------------------------------------
    public Vector3 GetLocalPos(boolean set_dirty) {
        if(set_dirty) {
            m_transform.local.SetDirty();
        }
        return m_local_pos;
    }

    //--------------------------------------------------------------------------
    public Quaternion GetLocalRot() {
        return m_local_rot;
    }

    //--------------------------------------------------------------------------
    public Quaternion GetLocalRot(boolean set_dirty) {
        if(set_dirty) {
            m_transform.local.SetDirty();
        }
        return m_local_rot;
    }

    //--------------------------------------------------------------------------
    public Vector3 GetLocalScale() {
        return m_local_scale;
    }

    //--------------------------------------------------------------------------
    public Vector3 GetLocalScale(boolean set_dirty) {
        if(set_dirty) {
            m_transform.local.SetDirty();
        }
        return m_local_scale;
    }

    //--------------------------------------------------------------------------
    public void SetGlobalTransformForcedUpdate() {
        m_global_transform_forced_update = true;
    }

    //--------------------------------------------------------------------------
    public boolean IsGlobalTransformDirty() {
        return m_transform.global.IsDirty();
    }

    //--------------------------------------------------------------------------
    public Matrix4 GetGlobalTransform() {
        return m_transform.global.value;
    }

    //--------------------------------------------------------------------------
    public Vector3 GetGlobalPos() {
        return m_global_pos;
    }

    //--------------------------------------------------------------------------
    public float GetLocalAlpha() {
        return m_alpha.local.value;
    }

    //--------------------------------------------------------------------------
    public float SetLocalAlpha(float value) {
        // Get old value
        GameNodeAttrib<Float> alpha_attr = m_alpha.local;
        float old = alpha_attr.value;

        // Set new
        alpha_attr.SetDirty().value = value;

        // Return old
        return old;
    }

    //--------------------------------------------------------------------------
    public boolean IsGlobalAlphaDirty() {
        return m_alpha.global.IsDirty();
    }

    //--------------------------------------------------------------------------
    public float GetGlobalAlpha() {
        return m_alpha.global.value;
    }

    //--------------------------------------------------------------------------
    public void CopyParamsTo(GameNode dest) {
        dest.GetLocalPos(true).set(m_local_pos);
        dest.GetLocalRot(true).set(m_local_rot);
        dest.GetLocalScale(true).set(m_local_scale);
        dest.SetLocalAlpha(m_alpha.local.value);
    }

    //--------------------------------------------------------------------------
    public void UpdateGlobalPos() {
        m_transform.global
          .SetDirty().value.getTranslation(m_global_pos);
    }

    //--------------------------------------------------------------------------
    public void UpdateTransform(GameNode parent) {
        // Get attributes
        GameNodeAttrib<Matrix4> att_init = m_transform.init;
        GameNodeAttrib<Matrix4> att_local = m_transform.local;
        GameNodeAttrib<Matrix4> att_init_local = m_transform.init_local;
        GameNodeAttrib<Matrix4> att_global = m_transform.global;

        // Get dirty flags
        boolean is_parent_dirty = (parent != null) ? 
          parent.IsGlobalTransformDirty() : false;
        boolean is_init_dirty = att_init.IsDirty();
        boolean is_local_dirty = att_local.IsDirty();
        boolean is_init_local_dirty = (is_local_dirty || is_init_dirty);
        boolean is_global_dirty = (is_parent_dirty || is_init_local_dirty);

        // Update init
        if(is_init_dirty) {
            att_init.ResetDirty();
        }

        // Update local
        if(is_local_dirty) {
            att_local.ResetDirty().value
              .set(m_local_pos, m_local_rot, m_local_scale);
        }

        // Update init-local
        att_init_local.ResetDirty();
        if(is_init_local_dirty) {
            att_init_local.value
              .set(att_local.value)
              .mul(att_init.value);
        }

        // Update global
        att_global.ResetDirty();
        if(is_global_dirty || m_global_transform_forced_update) {
            Matrix4 global = att_global.value
              .set(att_init_local.value);

            // Apply parent's transform 
            if(parent != null) {
                global.mulLeft(parent.GetGlobalTransform());
            }

            // Read global position
            UpdateGlobalPos();

            // Reset forced update
            m_global_transform_forced_update = false;
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateAlpha(GameNode parent) {
        // Get attributes
        GameNodeAttrib<Float> att_local = m_alpha.local;
        GameNodeAttrib<Float> att_global = m_alpha.global;

        // Get dirty flags
        boolean is_parent_dirty = (parent != null) ? 
          parent.IsGlobalAlphaDirty() : false;
        boolean is_local_dirty = att_local.IsDirty();
        boolean is_global_dirty = (is_parent_dirty || is_local_dirty);

        // Update local
        if(is_local_dirty) {
            att_local.ResetDirty();
        }

        // Update global
        att_global.SetDirty(is_global_dirty);
        if(is_global_dirty) {
            if(parent != null) {
                att_global.value = parent.GetGlobalAlpha() * att_local.value;
            } else {
                att_global.value = att_local.value;
            }
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateDebug(RenderCtx ctx) {
        // Debug renderer
        if(!m_do_dbg_render) {
            return;
        }

        // Create debug axis
        if(m_dbg_axis == null) {
            m_dbg_axis = (Model)RenderableObjectType.AXIS.Create();
        }

        // Update debug axis
        RendererModel model = m_dbg_axis.GetModelStack().Update();
        if(IsGlobalTransformDirty()) {
            model.inst.transform.set(GetGlobalTransform());
        }

        // Add to debug renderables
        ctx.AddLayerObject(model, m_dbg_axis.GetRendererLayer(), true, 0);
    }

    //--------------------------------------------------------------------------
    public boolean IsVisibleByCamera(Camera camera) {
        return true;
    }

    //--------------------------------------------------------------------------
    public void FadeIn() {
        FadeIn(1.0f);
    }
    //--------------------------------------------------------------------------
    public void FadeIn(float duration_factor) {
        float orig_alpha = SetLocalAlpha(0.0f);
        TweenAlpha(
          orig_alpha, Main.inst.cfg.model_fade_in_duration * duration_factor, null);
    }

    //--------------------------------------------------------------------------
    public void FadeOut() {
        FadeOut(1.0f);
    }

    //--------------------------------------------------------------------------
    public void FadeOut(float duration_factor) {
        TweenAlpha(
          0.0f, Main.inst.cfg.model_fade_out_duration * duration_factor, null);
    }

    //--------------------------------------------------------------------------
    public float GetDistanceToTarget(GameNode target, boolean do_sqrt) {
        Vector3 own_pos = GetGlobalPos();
        Vector3 target_pos = target.GetGlobalPos();
        float dist_x = target_pos.x - own_pos.x;
        float dist_z = target_pos.z - own_pos.z;
        float dist2 = dist_x * dist_x + dist_z * dist_z;
        return do_sqrt ? (float)Math.sqrt(dist2) : dist2;
    }

    //--------------------------------------------------------------------------
    private static Vector3 pos_cur = new Vector3();
    private static Vector3 pos_dest = new Vector3();
    private static Vector3 pos_diff = new Vector3();
    public Vector3 GetPosDiff(GameNode target) {
        GetGlobalTransform().getTranslation(pos_cur);
        target.GetGlobalTransform().getTranslation(pos_dest);
        return pos_diff.set(pos_dest).sub(pos_cur);
    }

    //--------------------------------------------------------------------------
    public long OnPreDelete(boolean delete_instantly) {
        m_manage_expiration = false;
        return 0;
    }

    //--------------------------------------------------------------------------
    public boolean IsExpired() {
        return false;
    }

    //**************************************************************************
    // SgNode
    //**************************************************************************
    @Override public SgNode SgRelocateChild(SgNode child) {
        GameNode node = (GameNode)child;
        GameNode old_parent = (GameNode)child.SgGetParent();
        GameNode new_parent = this;

        // Finish all child tweens when relocating
        node.m_tweener.Finish();

        // Recalculate local-pos of the node so that it keeps same global-pos
        // after being re-added to new parent
        node.GetLocalPos(true).add(
          new_parent.GetPosDiff(old_parent));

        // XXX: Recalculate rotation & alpha
        return super.SgRelocateChild(child);
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    public boolean OnMethodJson(JsonCtx ctx) {
        super.OnMethodJson(ctx);

        if(ctx.pre_children && 
           ctx.targets.contains(JsonTarget.COMMON)) {
            ctx.Write(
              "\"local-pos\" : {\"x\":\"%.1f\", \"y\":\"%.1f\", \"z\":\"%.1f\"}, ",
              m_local_pos.x, m_local_pos.y, m_local_pos.z);
            ctx.Write(
              "\"global-pos\" : {\"x\":\"%.1f\", \"y\":\"%.1f\", \"z\":\"%.1f\"}, ",
              m_global_pos.x, m_global_pos.y, m_global_pos.z);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodResize(int width, int height) {
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodRender(RenderCtx ctx) {
        // Update tweener
        m_tweener.Update();

        // Update attributes
        GameNode parent = (GameNode)SgGetParent();
        UpdateTransform(parent);
        UpdateAlpha(parent);
        UpdateDebug(ctx);

        // Schedule node for deletion when expired
        if(m_manage_expiration && IsExpired()) {
            Logger.d("Deleting expired node :: node=%s", SgGetNameId());
            Main.inst.sg_man.ScheduleForDeletion(this);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodPause() {
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodResume() {
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodSelect(SelectCtx ctx) {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_local_pos = null;
        m_local_rot = null;
        m_global_pos = null;
        m_transform.OnCleanup();
        m_transform = null;
        m_alpha.OnCleanup();
        m_alpha = null;
        m_tweener.OnCleanup();
        m_tweener = null;
        return true;
    }
}
