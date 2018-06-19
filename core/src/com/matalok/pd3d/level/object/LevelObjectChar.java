//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.desc.Desc;
import com.matalok.pd3d.desc.DescChar;
import com.matalok.pd3d.desc.DescSpriteInst;
import com.matalok.pd3d.desc.DescStringInst;
import com.matalok.pd3d.level.LevelDirection;
import com.matalok.pd3d.level.LevelSmartRotateHelper;
import com.matalok.pd3d.level.LevelTrashBin;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderable.billboard.Billboard;
import com.matalok.pd3d.renderable.model.Model;
import com.matalok.pd3d.renderable.model.ModelStack;
import com.matalok.pd3d.renderable.particle.Particle;
import com.matalok.pd3d.shared.Logger;

//------------------------------------------------------------------------------
public class LevelObjectChar 
  extends LevelObjectDynamic {
    //**************************************************************************
    // UpdateCtx
    //**************************************************************************
    public static class UpdateCtx
      implements LevelObject.IUpdateCtx {
        //**********************************************************************
        // UpdateCtx
        //**********************************************************************
        public DescChar desc;

        //----------------------------------------------------------------------
        public UpdateCtx(DescChar desc) {
            this.desc = desc;
        }

        //**********************************************************************
        // LevelObject.IUpdateCtx
        //**********************************************************************
        @Override public int GetCellId() {
            return desc.pos;
        }

        //----------------------------------------------------------------------
        @Override public int GetPdId() {
            return desc.id;
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

    //**************************************************************************
    // AutoRot
    //**************************************************************************
    public enum AutoRotation {
        //----------------------------------------------------------------------
        OFF, SIMPLE, SMART;
    }

    //**************************************************************************
    // BuffHandler
    //**************************************************************************
    public static class BuffHandler {
        //----------------------------------------------------------------------
        public void Run(LevelObjectChar ch, boolean is_active, 
          LevelTrashBin trash_bin) {
            boolean was_active = IsActive(ch);
            if(is_active && !was_active) {
                OnActivate(ch);
            } else if(is_active && was_active) {
                OnProcess(ch);
            } else if(!is_active && was_active) {
                OnDeactivate(ch, trash_bin);
            }
        }

        //----------------------------------------------------------------------
        public boolean IsActive(LevelObjectChar ch) { return true; };
        public void OnActivate(LevelObjectChar ch) { };
        public void OnProcess(LevelObjectChar ch) { };
        public void OnDeactivate(LevelObjectChar ch, LevelTrashBin trash_bin) { };
    }

    //**************************************************************************
    // FireBuffHandler
    //**************************************************************************
    public static class FireBuffHandler 
      extends BuffHandler {
        //----------------------------------------------------------------------
        private Particle m_pfx;

        //----------------------------------------------------------------------
        @Override public boolean IsActive(LevelObjectChar ch) { 
            return (m_pfx != null); 
        };

        //----------------------------------------------------------------------
        @Override public void OnActivate(LevelObjectChar ch) { 
            m_pfx = (Particle)ch.SgAddChild(
              RenderableObjectType.PFX_FIRE.Create());
        };

        //----------------------------------------------------------------------
        @Override public void OnDeactivate(LevelObjectChar ch, LevelTrashBin trash_bin) { 
            trash_bin.Put(m_pfx, false);
            m_pfx = null;
        };
    }

    //**************************************************************************
    // InvisibleBuffHandler
    //**************************************************************************
    public static class InvisibleBuffHandler 
      extends BuffHandler {
        //----------------------------------------------------------------------
        @Override public boolean IsActive(LevelObjectChar ch) { 
            return (ch.GetLocalAlpha() < 1.0f); 
        };

        //----------------------------------------------------------------------
        @Override public void OnActivate(LevelObjectChar ch) { 
            ch.TweenAlpha(0.4f, 1.0f, null);
        };

        //----------------------------------------------------------------------
        @Override public void OnDeactivate(LevelObjectChar ch, 
          LevelTrashBin trash_bin) {
            ch.TweenAlpha(1.0f, 1.0f, null);
        };
    }

    //**************************************************************************
    // LevitationBuffHandler
    //**************************************************************************
    public static class LevitationBuffHandler 
      extends BuffHandler {
        //----------------------------------------------------------------------
        private Particle m_pfx;

        //----------------------------------------------------------------------
        @Override public boolean IsActive(LevelObjectChar ch) { 
            return (m_pfx != null); 
        };

        //----------------------------------------------------------------------
        @Override public void OnActivate(LevelObjectChar ch) {
            if(!ch.HasElevation()) {
                ch.SetElevation(0.0f);
            }

            ch.TweenElevation(0.4f, 0.5f, null);
            m_pfx = (Particle)ch.SgAddChild(
              RenderableObjectType.PFX_LEVITATION.Create());
            m_pfx.GetLocalPos(true).y = 0.3f;
        };

        //----------------------------------------------------------------------
        @Override public void OnDeactivate(LevelObjectChar ch, 
          LevelTrashBin trash_bin) { 
            ch.TweenElevation(0.0f, 0.5f, null);

            trash_bin.Put(m_pfx, false);
            m_pfx = null;
        };
    }

    //**************************************************************************
    // LevelObjectChar
    //**************************************************************************
    private int m_hp;
    private int m_ht;
    private boolean m_is_bleeding;
    private boolean m_is_hp_changed;
    private LevelSmartRotateHelper m_smart_rotate_helper;
    private Billboard m_hp_bar;
    private Billboard m_emotion;
    private LinkedList<Integer> m_buffs;
    private LinkedList<Object> m_fading_status_queue;
    private long m_fading_status_next;
    private AutoRotation m_auto_rot;
    private BuffHandler[] m_buff_handlers;

    //--------------------------------------------------------------------------
    public LevelObjectChar(String name, int pd_id) {
        super(name, pd_id);
        m_hp = 0;
        m_ht = 1;

        // Auto-rotation is disabled by default
        SetAutoRotation(AutoRotation.OFF);

        // Hero moves 30% faster
        if(IsHero()) {
            m_smart_rotate_helper = new LevelSmartRotateHelper();
            m_model_move_duration *= 0.7f;
        }

        // Fading status
        m_fading_status_queue = new LinkedList<Object>();
        m_fading_status_next = 0;

        // Buff handlers
        m_buff_handlers = new BuffHandler[MapEnum.BuffType.GetSize()];
    }

    //--------------------------------------------------------------------------
    public Enum<?> GetEmotionSprite(String name) {
        return
          name == null ? null : 
          name.equals("alert") ? MapEnum.PfxImage.ICON_ALERT :
          name.equals("sleep") ? MapEnum.PfxImage.ICON_SLEEP : 
                                 MapEnum.PfxImage.SPECK_QUESTION;
    }

    //--------------------------------------------------------------------------
    public boolean IsBuffActive(MapEnum.BuffType buff) {
        BuffHandler h = m_buff_handlers[buff.ordinal()];
        return (h != null && h.IsActive(this));
    }

    //--------------------------------------------------------------------------
    public void AddFadingStatus(Desc status) {
        if(m_fading_status_queue.size() > 2) {
            m_fading_status_queue.removeLast();
        }
        m_fading_status_queue.add(status);
    }

    //--------------------------------------------------------------------------
    public void AddFadingStatusText(String text, Color color) {
        Billboard billboard = (Billboard)Main.inst.renderable_man
          .CreateBillboardText(text);
        billboard.GetBillboard().inst.setColor(color);
        AddFadingStatus((RenderableObject)GetMainObject().SgAddChild(billboard));
    }

    //--------------------------------------------------------------------------
    public void AddFadingStatusSprite(Enum<?> sprite, Color color) {
        Billboard billboard = (Billboard)Main.inst.renderable_man
          .CreateBillboardSprite(sprite);
        billboard.GetBillboard().inst.setColor(color);
        AddFadingStatus((RenderableObject)GetMainObject().SgAddChild(billboard));
    }

    //--------------------------------------------------------------------------
    public void AddFadingStatus(final RenderableObject robj) {
        // Place text above the model 
        robj.GetLocalPos(true).add(0.0f, 0.0f, -1.0f);

        // Begin fading animation
        robj.TweenAlpha(0.0f, Main.inst.cfg.model_text_fade_duration, null);
        robj.TweenPos(new Vector3(
                robj.GetLocalPos()).add(0.0f, 0.0f, -0.5f), 
          Main.inst.cfg.model_text_fade_duration * 1.2f,
          new Tweener.Callback(robj) {
            @Override public void OnComplete() {
                Main.inst.sg_man.ScheduleForDeletion(robj);
            }
        });
    }

    //--------------------------------------------------------------------------
    public boolean IsHero() {
        return (m_pd_id == Main.inst.cfg.lvl_hero_id);
    }

    //--------------------------------------------------------------------------
    public int GetHp() {
        return m_hp;
    }

    //--------------------------------------------------------------------------
    public int GetHt() {
        return m_ht;
    }

    //--------------------------------------------------------------------------
    public float GetHpRel() {
        return (float)m_hp / (float)m_ht;
    }

    //--------------------------------------------------------------------------
    public boolean IsAlive() {
        return (m_hp > 0);
    }

    //--------------------------------------------------------------------------
    public boolean IsFlying() {
        return IsBuffActive(MapEnum.BuffType.LEVITATION);
    }

    //--------------------------------------------------------------------------
    public LinkedList<Integer> GetBuffs() {
        return m_buffs;
    }

    //--------------------------------------------------------------------------
    public LevelObjectChar SetAutoRotation(AutoRotation auto_rot) {
        m_auto_rot = auto_rot;
        return this;
    }

    //--------------------------------------------------------------------------
    public LevelDirection RunAutoRotation(LevelObjectCell prev_cell, boolean forward) {
        // Get initial direction
        LevelDirection dir = GetDirection(forward);

        // Ignore 
        if(m_auto_rot == AutoRotation.OFF) {
            return dir;
        }

        // Parent cell should be present
        LevelObjectCell cell = GetParentCell();
        if(cell == null) {
            Logger.e("Failed to rotate before moving forward, no parent cell :: char=%s", 
              SgGetNameId());
            return dir;
        }

        // Do smart rotation
        int best_rot = 0;
        if(m_auto_rot == AutoRotation.SMART) {
            m_smart_rotate_helper.SetAvoidChasm(
              !IsBuffActive(MapEnum.BuffType.LEVITATION));
            best_rot = m_smart_rotate_helper.GetBestRotation(cell, dir);

        // Do simple rotation
        } else if(m_auto_rot == AutoRotation.SIMPLE) {
            if(prev_cell != null) {
                Rotate(GetRotationToTarget(prev_cell) + 4);
            }
        }

        // Rotate hero towards best cell
        if(best_rot != 0) {
            Rotate(best_rot);
            dir = GetDirection(forward);
        }
        return dir;
    }

    //**************************************************************************
    // LevelObjectDynamic
    //**************************************************************************
    @Override public void Rotate(int dir, float duration, final Tweener.Callback cb) {
        super.Rotate(dir, duration, cb);

        if(m_smart_rotate_helper != null) {
            m_smart_rotate_helper.SetPrevRotation(dir);
        }

        if(IsHero() && Main.inst.engine.GetGameScene(false) != null) {
            Main.inst.engine.GetGameWindow(false).UpdateMinimap();
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnUpdateBegin(IUpdateCtx ctx) {
        // Char is bleeding if his HP is decreasing
        DescChar desc = ((UpdateCtx)ctx).desc;
        m_is_bleeding = (m_hp > desc.hp);
        m_is_hp_changed = (m_hp > 0 && m_hp != desc.hp);

        // Set health
        m_hp = desc.hp;
        m_ht = desc.ht;

        // Set buffs
        m_buffs = desc.buffs;
    }

    //--------------------------------------------------------------------------
    @Override public void OnUpdateEnd(IUpdateCtx ctx, LevelTrashBin trash_bin) {
        // Update HP bar of the enemy
        if(!IsHero()) {
            // Show HP bar
            if(m_hp > 0 && (m_is_hp_changed || m_hp < m_ht)) {
                // Delete old HP bar
                int hp_template_id = Main.inst.renderable_man
                  .GetHpTemplateId((float)m_hp / m_ht);
                if(m_hp_bar != null && 
                  m_hp_bar.GetObjectId() != hp_template_id) {
                    Main.inst.sg_man.ScheduleForDeletion(m_hp_bar);
                    m_hp_bar = null;
                }

                // Create new HP bar
                if(m_hp_bar == null) {
                    m_hp_bar = (Billboard)SgAddChild(
                      RenderableObjectType.BILLBOARD_HP.Create(hp_template_id));
                }

            // Hide HP bar
            } else if(m_hp_bar != null) {
                Main.inst.sg_man.ScheduleForDeletion(m_hp_bar);
                m_hp_bar = null;
            }
        }

        // Bleeding
        if(m_is_bleeding) {
            Main.inst.level.CreateMarker(
              GetParentCell(), RenderableObjectType.MARKER_BLOOD);
        }

        // Set default animation
        SetDefaultAnim(
          IsFlying() ? "fly" : 
          IsAlive()  ? "idle" : "die");

        // Update animation
        DescChar desc = ((UpdateCtx)ctx).desc;
        if(desc.anims != null) {
            for(String anim : desc.anims) {
                AddAnimToChain(anim);
            }
        }

        // Update emotion billboard
        Enum<?> old_emotion_sprite = (m_emotion != null) ? 
          (Enum<?>)m_emotion.GetBillboardKey() : null;
        Enum<?> new_emotion_sprite = (m_hp > 0) ? 
          GetEmotionSprite(desc.emotion) : null;
        if(old_emotion_sprite != new_emotion_sprite) {
            if(m_emotion != null) {
                Main.inst.sg_man.ScheduleForDeletion(m_emotion);
                m_emotion = null;
            }

            if(new_emotion_sprite != null) {
                m_emotion = (Billboard)SgAddChild(
                  Main.inst.renderable_man
                    .CreateBillboardEmotion(new_emotion_sprite));
            }
        }

        // Handle buffs
        DescChar char_desc = (DescChar)ctx.GetDescriptor();
        for(int buff_id = 0; buff_id < MapEnum.BuffType.GetSize(); buff_id++) {
            // Buff is active
            BuffHandler h = m_buff_handlers[buff_id];
            if(char_desc.buffs != null && char_desc.buffs.contains(buff_id)) {
                if(h == null) {
                    h = m_buff_handlers[buff_id] = 
                      (buff_id == MapEnum.BuffType.FIRE.ordinal()) ? new FireBuffHandler() :
                      (buff_id == MapEnum.BuffType.INVISIBLE.ordinal()) ? new InvisibleBuffHandler() : 
                      (buff_id == MapEnum.BuffType.LEVITATION.ordinal()) ? new LevitationBuffHandler() :
                      (buff_id == MapEnum.BuffType.STARVATION.ordinal()) ? new BuffHandler() :
                      null;
                }
                if(h != null) {
                    h.Run(this, true, trash_bin);
                }

            // Buff is inactive
            } else if(h != null) {
                h.Run(this, false, trash_bin);
                m_buff_handlers[buff_id] = null;
            }
        }
    }

    //--------------------------------------------------------------------------
    @Override public RenderableObject OnNewObject(IUpdateCtx ctx) {
        // Clear pointer to old child models (if any)
        m_hp_bar = null;
        m_emotion = null;

        // Create new model
        return RenderableObjectType.CHAR.Create(ctx.GetModelId());
    }

    //--------------------------------------------------------------------------
    @Override public void OnNewCell(IUpdateCtx desc, LevelObjectCell new_cell) {
        // Get old cell
        LevelObjectCell old_cell = GetParentCell();

        // Create ripples on water
        if(old_cell.GetTerrain().HasWater()) {
            Main.inst.level.CreateRipple(old_cell, 0.0f);
        }
        if(new_cell.GetTerrain().HasWater()) {
            Main.inst.level.CreateRipple(new_cell, 0.7f);
        }

        // Relocate to new cell
        super.OnNewCell(desc, new_cell);

        // Rotate to avoid obstacles
        RunAutoRotation(old_cell, true);

        if(IsHero()) {
            Main.inst.level_camera.SwingCamera();
            Main.inst.level.GetSkybox().Rotate();
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnStartMovementAnim() {
        if(IsStill() && IsAlive()) {
            AddAnimToChain("run");
        }
    }

    //**************************************************************************
    // LevelObject
    //**************************************************************************
    @Override public boolean IsAnimOver(String name) {
        Model model = (Model)GetMainObject();
        ModelStack batch = model.GetModelStack();
        if(model == null || !batch.HasAnim(name)) {
            return true;
        }

        // There are more than one animation in chain
        switch(name) {
            // Always over
            case "idle" :
            case "fly"  : {
                return true;
            }

            // Run animation is over when not moving
            case "run": {
                return IsStill();
            }

            // Other animation types are over only when last frame has been shown
            default: {
                return !batch.GetAnim(name).IsRunning();
            }
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnStartAnim(String name) {
        Model model = (Model)GetMainObject();
        ModelStack batch = model.GetModelStack();
        if(model == null || !batch.HasAnim(name)) {
            return;
        }
        batch.StartAnim(name);
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Create fading status when previous status is gone
        long cur_time = Main.inst.timer.GetCur();
        if(m_fading_status_queue.size() > 0 && cur_time > m_fading_status_next) {
            Object status = m_fading_status_queue.removeFirst();
            m_fading_status_next = cur_time + 300;

            // String
            if(status.getClass() == DescStringInst.class) {
                DescStringInst desc = (DescStringInst)status;
                AddFadingStatusText(desc.text, new Color(desc.color));

            // Icon
            } else if(status.getClass() == DescSpriteInst.class) {
                DescSpriteInst desc = (DescSpriteInst)status;
                AddFadingStatusSprite(MapEnum.GetTypeByName(desc.type, desc.id), 
                  (desc.color != null) ? new Color(desc.color) : Color.WHITE);
            }
        }
        return true;
    }
}
