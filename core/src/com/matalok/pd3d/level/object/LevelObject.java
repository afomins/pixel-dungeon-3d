//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import java.util.LinkedList;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.desc.Desc;
import com.matalok.pd3d.level.LevelTrashBin;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.node.GameNodeTweener;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class LevelObject 
  extends GameNode {
    // *************************************************************************
    // IUpdateCtx
    // *************************************************************************
    public interface IUpdateCtx {
        //----------------------------------------------------------------------
        public int GetCellId();
        public int GetPdId();
        public int GetModelId();
        public Desc GetDescriptor();
        public boolean IsDirty();
    }

    // *************************************************************************
    // LevelObject
    // *************************************************************************
    protected int m_pd_id;
    protected boolean m_is_updated;
    protected IUpdateCtx m_update_ctx;
    protected Float m_elevation;

    protected Tweener.UserData m_tween_elevation;

    // Animation chain
    protected LinkedList<String> m_anim_chain;
    protected String m_anim_default;
    protected String m_anim_cur;

    //--------------------------------------------------------------------------
    public LevelObject(String name, int pd_id) {
        super(name, 1.0f);
        m_pd_id = pd_id;
        m_anim_chain = new LinkedList<String>();

        m_tween_elevation = no_tween;
    }

    //--------------------------------------------------------------------------
    public void SetElevation(float value) {
        m_elevation = value;
        SetGlobalTransformForcedUpdate();
    }

    //--------------------------------------------------------------------------
    public float GetElevation() {
        Utils.Assert(m_elevation != null, "Failed to get empty elevation");
        return m_elevation;
    }

    //--------------------------------------------------------------------------
    public void UnsetElevation() {
        m_elevation = null;
        SetGlobalTransformForcedUpdate();
    }

    //--------------------------------------------------------------------------
    public boolean HasElevation() {
        return (m_elevation != null);
    }

    //--------------------------------------------------------------------------
    public boolean IsElevating() {
        return !m_tween_elevation.IsFinished();
    }

    //--------------------------------------------------------------------------
    public void TweenElevation(float dest, float duration, final Tweener.Callback cb) {
        // Instant fading
        if(duration <= 0.0f) {
            m_tween_elevation = new Tweener.UserData(0);
            SetElevation(dest);
            return;
        }

        // Tween elevation
        Tweener.Callback cb_new = new Tweener.Callback(this) {
            @Override public void OnComplete() {
                m_tween_elevation = no_tween;
                if(cb != null) {
                    cb.OnComplete();
                }
            }
        };

        // Tween elevation
        m_tween_elevation = m_tweener.Start(this, GameNode.class, 
          GameNodeTweener.ELEVATION, duration, null, cb_new, dest);
    }

    //--------------------------------------------------------------------------
    public void SetDefaultAnim(String anim) {
        m_anim_default = anim;
    }

    //--------------------------------------------------------------------------
    public boolean IsDefaultAnimRunning() {
        return (m_anim_chain.size() == 0 && 
          m_anim_cur != null && m_anim_default != null && 
          m_anim_cur.equals(m_anim_default));
    }

    //--------------------------------------------------------------------------
    public int GetPdId() {
        return m_pd_id;
    }

    //--------------------------------------------------------------------------
    public boolean TestUpdateFlag() {
        boolean is_updated = m_is_updated;
        m_is_updated = false;
        return is_updated;
    }

    //--------------------------------------------------------------------------
    public void AddAnimToChain(String anim) {
        // Ignore default animation
        if(m_anim_default == null || anim.equals(m_anim_default)) {
            return;
        }

        // Ignore duplicate animation
        if(m_anim_chain.size() > 0 && m_anim_chain.getLast().equals(anim)) {
            return;
        }
        m_anim_chain.add(anim);
        return;
    }

    //--------------------------------------------------------------------------
    private boolean UpdateAnim() {
        // Ignore if no default animation
        if(m_anim_default == null) {
            return false;
        }

        // Select next animation
        String next = null;
        boolean next_is_default = false;
        if(m_anim_cur == null || IsAnimOver(m_anim_cur)) {
            // Chain is empty - select default
            if(m_anim_chain.isEmpty()) {
                next = m_anim_default;
                next_is_default = true;

            // Chain is not empty - select next in chain
            } else {
                next = m_anim_chain.pop();
            }
        }

        // Ignore when not ready for next animation
        // or default animation is already running
        if(next == null || 
          (next_is_default && m_anim_cur != null && m_anim_cur.equals(next))) { 
            return false;
        }

        // Start next animation
        m_anim_cur = next;
        OnStartAnim(m_anim_cur);
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean IsAnimOver(String name) {
        return false;
    }

    //--------------------------------------------------------------------------
    public IUpdateCtx GetUpdateCtx() {
        return m_update_ctx;
    }

    //--------------------------------------------------------------------------
    public void OnStartAnim(String name) {
    }

    //--------------------------------------------------------------------------
    public void OnPreUpdate(IUpdateCtx ctx, LevelTrashBin trash_bin) {
    }

    //--------------------------------------------------------------------------
    public void OnUpdate(IUpdateCtx ctx, LevelTrashBin trash_bin) {
        m_is_updated = true;
        m_update_ctx = ctx;
    }

    //--------------------------------------------------------------------------
    public void OnDelete(LevelTrashBin trash_bin) {
        trash_bin.Put(this, false);
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
    public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Apply elevation
        if(HasElevation() && IsGlobalTransformDirty()) {
            GetGlobalTransform().translate(0.0f, m_elevation, 0.0f);
            UpdateGlobalPos();
        }

        // Update animation
        UpdateAnim();
        return true;
    }
}
