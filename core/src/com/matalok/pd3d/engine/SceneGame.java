/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.matalok.pd3d.InputMan;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescBag;
import com.matalok.pd3d.desc.DescChar;
import com.matalok.pd3d.desc.DescEvent;
import com.matalok.pd3d.desc.DescHeap;
import com.matalok.pd3d.desc.DescHeroStats;
import com.matalok.pd3d.desc.DescItem;
import com.matalok.pd3d.desc.DescQuestAction;
import com.matalok.pd3d.desc.DescSceneGame;
import com.matalok.pd3d.engine.gui.EngineWnd.ClickListener;
import com.matalok.pd3d.engine.gui.EngineWndBanner;
import com.matalok.pd3d.engine.gui.EngineWndGame;
import com.matalok.pd3d.engine.input.*;
import com.matalok.pd3d.level.Level;
import com.matalok.pd3d.level.LevelMinimap;
import com.matalok.pd3d.level.object.LevelObjectCell;
import com.matalok.pd3d.level.object.LevelObjectChar;
import com.matalok.pd3d.level.object.LevelObjectChar.AutoRotation;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.msg.MsgGetInventory;
import com.matalok.pd3d.msg.MsgQuestStart;
import com.matalok.pd3d.msg.MsgHeroInteract;
import com.matalok.pd3d.msg.MsgRunItemAction;
import com.matalok.pd3d.msg.MsgQuestAction;
import com.matalok.pd3d.msg.MsgUpdateScene;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class SceneGame 
  extends Scene {
    //**************************************************************************
    // IGameState
    //**************************************************************************
    private interface IGameState {
        //----------------------------------------------------------------------
        public void Update();
        public void UpdateGui(EngineWndGame wnd);
    }

    //**************************************************************************
    // StateAlive
    //**************************************************************************
    public class StateAlive
      extends Scene.StateInit 
      implements IGameState {
        //----------------------------------------------------------------------
        public EngineInputHeroMove move;

        //----------------------------------------------------------------------
        public StateAlive() {
            super(Main.inst.engine.wnd_game);
        }

        //----------------------------------------------------------------------
        public void WriteLog(String msg) {
            ((EngineWndGame)wnd).WriteLog(msg);
        }

        //----------------------------------------------------------------------
        public void BlockMovement() {
            move.BlockKeys();
            move.BlockTouch();
        }

        //----------------------------------------------------------------------
        private void UpdateTouchMode() {
            move.SetTouchMode(Main.inst.cfg.lvl_hero_auto_move ? 
              EngineInputHeroMove.TouchMode.MOVE_TARGET :
              EngineInputHeroMove.TouchMode.MOVE_FORWARD);
        }

        //----------------------------------------------------------------------
        @Override public LinkedList<InputMan.IClient> GetInput() {
            move = new EngineInputHeroMove((SceneGame)scene);

            LinkedList<InputMan.IClient> list = super.GetInput();
            list.add(new EngineInputMain((SceneGame)scene));
            list.add(move);
            list.add(new EngineInputHeroRotate((SceneGame)scene));
            list.add(new EngineInputHeroAction((SceneGame)scene));
            list.add(new EngineInputCtrlCamera((SceneGame)scene));

            UpdateTouchMode();
            return list;
        }

        //----------------------------------------------------------------------
        @Override public void OnActivateState() {
            super.OnActivateState();

            // Stop switching snapshots
            Main.inst.snapshot.StopAutoswitch();

            // Stop camera rotation
            Main.inst.level_camera.StopCircleRotation();

            // Reset hidden banner window
            EngineWndBanner banner = Main.inst.engine.wnd_banner;
            banner.Rebuild();
            banner.UpdateImage(null, null, null, null);

            // Start logging
            WriteLog("...");

            // Reset terrain detection
            Level.dbg_detect_terrain_done = false;
        }

        //----------------------------------------------------------------------
        @Override public void OnResumeState() {
            super.OnResumeState();

            UpdateGui((EngineWndGame)wnd);
            ((EngineWndGame)wnd).UpdateResumeMovement(!move.IsTargetReached());
        }

        //----------------------------------------------------------------------
        @Override public void OnDeactivateState() {
            super.OnDeactivateState();

            // Close banner window
            Main.inst.engine.wnd_banner.Close(true);
        }

        //----------------------------------------------------------------------
        @Override public void Update() {
            // Update touch mode
            UpdateTouchMode();

            // Update gui
            UpdateGui((EngineWndGame)wnd);

            // Die-e-e-e-e!!!!11
            if(!Main.inst.level.GetHero().IsAlive()) {
                scene.PushState(new StateDead());
            }
        }

        //----------------------------------------------------------------------
        @Override public void UpdateGui(EngineWndGame wnd) {
            wnd.EnableButtons();
            wnd.UpdateSearchButton(((SceneGame)scene).GetLootItem());
            wnd.UpdateQuickSlots();
            wnd.UpdateHero();
        }
    }

    //**************************************************************************
    // StateDead
    //**************************************************************************
    public class StateDead
      extends Scene.State 
      implements IGameState {
        //----------------------------------------------------------------------
        public StateDead() {
            super(Main.inst.engine.wnd_game, false);
        }

        //----------------------------------------------------------------------
        @Override public LinkedList<InputMan.IClient> GetInput() {
            LinkedList<InputMan.IClient> list = super.GetInput();
            list.add(new EngineInputMain((SceneGame)scene));
            return list;
        }

        //----------------------------------------------------------------------
        @Override public void OnActivateState() {
            super.OnActivateState();

            // Rotate camera around dead body
            Main.inst.level_camera.StartCircleRotation(false);

            // Show GAME-OVER banner
            Main.inst.engine.wnd_banner.UpdateImage(
              MapEnum.BannerType.GAME_OVER, 
              Main.inst.renderer.GetRelSmallest(0.8f), null, null);

            // Show resume button which is used to return to start screen
            ((EngineWndGame)wnd).UpdateResumeMovement(true);
        }

        //----------------------------------------------------------------------
        @Override public void OnResumeState() {
            super.OnResumeState();

            UpdateGui((EngineWndGame)wnd);
        }

        //----------------------------------------------------------------------
        @Override public void UpdateGui(EngineWndGame wnd) {
            wnd.DisableButtons();
            wnd.EnableButtons(EngineWndGame.Buttons.RESUME, 
              EngineWndGame.Buttons.INVENTORY);
            wnd.UpdateQuickSlots();
            wnd.UpdateHero();
        }

        //----------------------------------------------------------------------
        @Override public void Update() {
            UpdateGui((EngineWndGame)wnd);
        }
    }

    //**************************************************************************
    // StateThrowItem
    //**************************************************************************
    public class StateThrowItem
      extends Scene.State 
      implements IGameState {
        //----------------------------------------------------------------------
        public String item_action;
        public Integer item_idx;
        public Integer quickslot_idx;
        public EngineInputItemThrow throw_input;

        //----------------------------------------------------------------------
        public StateThrowItem(String item_action, Integer item_idx, 
          Integer quickslot_idx) {
            super(Main.inst.engine.wnd_game, false);
            this.item_action = item_action;
            this.item_idx = item_idx;
            this.quickslot_idx = quickslot_idx;
        }

        //----------------------------------------------------------------------
        void Throw(LevelObjectCell target) {
            throw_input.Throw(target);
        }

        //----------------------------------------------------------------------
        @Override public LinkedList<InputMan.IClient> GetInput() {
            LinkedList<InputMan.IClient> list = super.GetInput();
            list.add(new EngineInputMain((SceneGame)scene));
            list.add(new EngineInputHeroRotate((SceneGame)scene));
            list.add(new EngineInputCtrlCamera((SceneGame)scene));
            list.add(throw_input = new EngineInputItemThrow(
              (SceneGame)scene, item_action, item_idx));
            return list;
        }

        //----------------------------------------------------------------------
        @Override public void OnResumeState() {
            super.OnResumeState();

            // Update gui
            UpdateGui((EngineWndGame)wnd);
        }

        //----------------------------------------------------------------------
        @Override public void UpdateGui(EngineWndGame wnd) {
            wnd.DisableButtons();
            wnd.EnableButtons(EngineWndGame.Buttons.SEARCH, 
              EngineWndGame.Buttons.NEXT_TARGET);
            wnd.UpdateSearchButton(MapEnum.IconType.CLOSE);
            wnd.UpdateQuickSlots();
            wnd.UpdateHero();

            // Enable quickslot button to throw item to auto-targeted cell
            if(quickslot_idx != null) {
                EngineWndGame.Buttons btn = (quickslot_idx == 0) ? 
                  EngineWndGame.Buttons.QUICK0 : EngineWndGame.Buttons.QUICK1;
                ((EngineWndGame)wnd).GetButton(btn).SetEnabled();
            }
        }

        //----------------------------------------------------------------------
        @Override public void Update() {
        }
    }

    //**************************************************************************
    // StateShowInventory
    //**************************************************************************
    public static class StateShowInventory
      extends Scene.State 
      implements IGameState {
        //----------------------------------------------------------------------
        public StateShowInventory(String title, String mode, String listener, 
          int quickslot_idx, int gold_num, LinkedList<DescBag> bags) {
            super(Main.inst.engine.wnd_inv
              .Init(title, mode, listener, quickslot_idx, gold_num, bags), true);
        }

        //----------------------------------------------------------------------
        @Override public LinkedList<InputMan.IClient> GetInput() {
            // XXX: "EngineInputInventory" is not called because
            //      input events are not propagated in modal window
            //      See InputListener::keyDown() in Window() constructor
            LinkedList<InputMan.IClient> list = super.GetInput();
            list.add(new EngineInputInventory((SceneGame)scene));
            return list;
        }

        //----------------------------------------------------------------------
        @Override public void UpdateGui(EngineWndGame wnd) {
        }

        //----------------------------------------------------------------------
        @Override public void Update() {
        }
    }

    //**************************************************************************
    // StateShowItem
    //**************************************************************************
    public static class StateShowItem
      extends Scene.State 
      implements IGameState {
        //----------------------------------------------------------------------
        public StateShowItem(DescItem item, 
          HashMap<String,ClickListener> actions) {
            super(Main.inst.engine.wnd_item
              .Init(item, actions), true);
        }

        //----------------------------------------------------------------------
        @Override public void UpdateGui(EngineWndGame wnd) {
        }

        //----------------------------------------------------------------------
        @Override public void Update() {
        }
    }
    

    //**************************************************************************
    // StateShowQuest
    //**************************************************************************
    public static class StateShowQuest
      extends Scene.State 
      implements IGameState {
        //----------------------------------------------------------------------
        public StateShowQuest(String title, String text, Enum<?> sprite, 
          Map<String, ClickListener> actions, int btn_row_size) {
            super(Main.inst.engine.wnd_quest
              .Init(title, text, sprite, actions, btn_row_size), true);
        }

        //----------------------------------------------------------------------
        @Override public void UpdateGui(EngineWndGame wnd) {
        }

        //----------------------------------------------------------------------
        @Override public void Update() {
        }
    }

    //**************************************************************************
    // STEP
    //**************************************************************************
    public static class Step {
        //----------------------------------------------------------------------
        public enum State {
            IDLE, WAIT_UPDATE, PROCESS_UPDATE,
        }

        //----------------------------------------------------------------------
        public long start_time;
        public State state;
        public int idx;

        //----------------------------------------------------------------------
        public Step() {
            state = Step.State.IDLE;
        }

        //----------------------------------------------------------------------
        public String GetStr() {
            return state + ":" + idx;
        }

        //----------------------------------------------------------------------
        public long GetDuration() {
            return Main.inst.timer.GetCur() - start_time;
        }

        //----------------------------------------------------------------------
        public void SetState(State new_state) {
            SetState(new_state, idx);
        }

        //----------------------------------------------------------------------
        public void SetState(State new_state, int new_idx) {
            String old_str = GetStr();

            // Update step
            start_time = Main.inst.timer.GetCur();
            state = new_state;
            idx = new_idx;

            // Update GUI spinner
            Main.inst.engine.GetGameWindow(true)
              .UpdateBusyIndicator(state != State.IDLE);

            // Log
            Logger.d("Switching step state :: %s->%s", old_str, GetStr());
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static boolean RecvMsgUpdateScene(
      final SceneGame scene, final MsgUpdateScene msg) {
        Logger.d("Updating game scene");
        DescSceneGame desc = msg.game_scene;

        // First char in list should be hero
        DescChar hero = desc.chars.getFirst();
        if(hero == null || hero.id != Main.inst.cfg.lvl_hero_id) {
            Logger.e("Failed to update scene, first char in list is not hero");
            return false;
        }

        //..................................................................
        //
        // Terrain
        //
        final Level lvl = Main.inst.level;

        // Deserialize map if it's present in message
        if(desc.map_srlz != null) {
            if(!lvl.DeserializeTerrain(
              desc.map_width, desc.map_height, desc.map_srlz)) {
                Logger.e("Failed to update scene, failed to deserialize map");
                return false;
            }
        }

        // Set size of minimap
        final LevelMinimap minimap = lvl.GetMinimap();
        minimap.SetSize(desc.map_width, desc.map_height);

        // Callback which will be called when all level geometry is fully updated
        UtilsClass.Callback update_cb = new UtilsClass.Callback() {
            @Override public Object Run(Object... args) {
                EngineWndGame game_wnd = Main.inst.engine.GetGameWindow(false);
                if(game_wnd == null) {
                    return null;
                }

                // Update minimap texture
                minimap.Update();
                game_wnd.UpdateMinimap();

                // Update target
                lvl.UpdateTarget().SelectAutoTarget();
                game_wnd.UpdateTarget();
                return null;
            }
        };

        // Test level checksum
        if(desc.map_csum != null) {
            lvl.TestChecksum(desc.map_csum);
        }

        // Update terrain
        if(!lvl.UpdateTerrain(hero.pos, desc.dungeon_depth, update_cb)) {
            Logger.e("Failed to update scene, failed to update terrain");
            return false;
        }

        //..................................................................
        //
        // Dynamic objects
        //

        // Chars
        if(desc.chars != null) {
            for(DescChar char_desc : desc.chars) {
                lvl.UpdateChar(char_desc);
            }
        }

        // Items
        if(desc.heaps != null) {
            for(DescHeap heap_desc : desc.heaps) {
                lvl.UpdateItem(heap_desc);
            }
        }

        // Plants
        if(desc.plants != null) {
            for(DescHeap plant_desc : desc.plants) {
                lvl.UpdatePlant(plant_desc);
            }
        }

        //..................................................................
        //
        // Handle game events
        //
        // 
        if(desc.events != null) {
            for(DescEvent event : desc.events) {
                lvl.HandleEvent(event);
            }
        }

        //..................................................................
        //
        // Game scene
        //
        if(scene != null) {
            // Loot item
            scene.SetLootItem((desc.loot_item_id == null) ? null : 
              MapEnum.ItemType.Get(desc.loot_item_id));

            // Quickslot items
            scene.SetQuickSlotItem(0, desc.quickslot0);
            scene.SetQuickSlotItem(1, desc.quickslot1);

            // Hero stats
            scene.SetHeroStats(desc.hero_stats);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public static boolean RecvMsgGetInventory(
      final SceneGame scene, final MsgGetInventory msg) {
        int quickslot_idx = (msg.listener_ex == null) ? -1 : msg.listener_ex;
        scene.PushState(new StateShowInventory(msg.title, msg.mode, 
          msg.listener, quickslot_idx, msg.gold_num, msg.bags));
        return true;
    }

    //--------------------------------------------------------------------------
    public static boolean RecvMsgRunItemAction(
      final SceneGame scene, final MsgRunItemAction msg) {
        // Throw handler
        if(msg.action.equals("throw") || msg.action.equals("zap")) {
            // Action was not performed
            if(msg.dest_cell_idx < 0) {
                return true;
            }

            Level lvl = Main.inst.level;
            lvl.ThrowObject(RenderableObjectType.ITEM.Create(msg.sprite_id), 
              msg.src_cell_idx, msg.dest_cell_idx, msg.do_circle_back);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public static boolean RecvMsgStartQuest(
      final SceneGame scene, final MsgQuestStart msg) {
        // Actions quest actions
        Map<String, ClickListener> actions = null;
        if(msg.quest.actions != null) {
            actions = new LinkedHashMap<String, ClickListener>();
            for(final DescQuestAction desc : msg.quest.actions) {
                String name = (!desc.is_active ? "!!!" : "") + desc.desc;
                actions.put(name, 
                  new ClickListener(null) {
                      @Override public void OnReleased() {
                          // Return from quest state
                          scene.PopState(StateAlive.class);

                          // Send quest action
                          MsgQuestAction msg_action = MsgQuestAction.CreateRequest();
                          msg_action.action = desc.name;
                          Main.inst.proxy_client.Send(msg_action);
                      }
                 });
            }
        }

        // Push quest state
        Enum<?> sprite = MapEnum.GetTypeByName(
          msg.quest.sprite_id.type, msg.quest.sprite_id.id);
        if(sprite == null) {
            Logger.e("Failed to start quest, wrong sprite-id :: type=%s id=%d", 
              msg.quest.sprite_id.type, msg.quest.sprite_id.id);
            return false;
        }
        scene.PushState(new StateShowQuest(
          msg.quest.title, msg.quest.text, sprite, actions, 2));
        return true;
    }

    //**************************************************************************
    // SceneGame
    //**************************************************************************

    // Loot item
    private MapEnum.ItemType m_loot_item;

    // Hero stats
    private DescHeroStats m_hero_stats;

    // Quickslot items
    private DescItem m_quickslot_items[];

    // Step
    private Step m_step;

    // Pending update message
    private MsgUpdateScene m_update_msg;

    //--------------------------------------------------------------------------
    public SceneGame() {
        super("scene-game");

        // Quickslot
        m_quickslot_items = new DescItem[2];

        // Step state
        m_step = new Step();
    }

    //--------------------------------------------------------------------------
    public Step GetStep() {
        return m_step;
    }

    // -------------------------------------------------------------------------
    public void SetHeroStats(DescHeroStats hero_stats) {
        m_hero_stats = hero_stats;
    }

    // -------------------------------------------------------------------------
    public DescHeroStats GetHeroStats() {
        return m_hero_stats;
    }

    // -------------------------------------------------------------------------
    public void SetLootItem(MapEnum.ItemType loot_item) {
        m_loot_item = loot_item;
    }

    // -------------------------------------------------------------------------
    public MapEnum.ItemType GetLootItem() {
        return m_loot_item;
    }

    // -------------------------------------------------------------------------
    public void SetQuickSlotItem(int idx, DescItem item) {
        m_quickslot_items[idx] = item;
    }

    // -------------------------------------------------------------------------
    public DescItem GetQuickSlotItem(int idx) {
        return m_quickslot_items[idx];
    }

    //--------------------------------------------------------------------------
    public void ThrowAtSelection() {
        // Validate state
        if(GetStateType() != StateThrowItem.class) {
            Logger.e("Failed to run item action, wrong state :: state=%s", 
              GetState().GetName());
            return;
        }

        // Validate target
        LevelObjectChar target_char = 
          Main.inst.level.GetTarget().GetSelectedChar();
        if(target_char == null) {
            return;
        }

        // Throw
        ((StateThrowItem)GetState())
          .Throw(target_char.GetParentCell());
    }

    //--------------------------------------------------------------------------
    public void RunItemAction(DescItem item, int item_idx, int quickslot_idx, 
      String action) {
        // Select default action
        if(action == null) {
            if(item.default_action == null) {
                return;
            }
            action = item.default_action;
        }

        // Enter throw state
        if(action.equals("throw") || action.equals("zap")) {
            PushState(new StateThrowItem(action, item_idx, 
              (quickslot_idx < 0) ? null : quickslot_idx));

        // Run item action from current state
        } else {
            Main.inst.proxy_client.Send(
              MsgRunItemAction.CreateRequest(item_idx, action, null));
        }
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    @Override protected void OnServerResponse(Msg msg) {
        boolean run_default_handler = true;

        //......................................................................
        // UPDATE-SCENE
        Class<? extends Msg> msg_class = msg.getClass();
        if(msg_class == MsgUpdateScene.class) {
            // Save update message and process it later in OnMethodPostRender()
            m_update_msg = (MsgUpdateScene)msg;
            run_default_handler = false;

        //......................................................................
        // GET-INVENTORY
        } else if(msg_class == MsgGetInventory.class) {
            SceneGame.RecvMsgGetInventory(this, (MsgGetInventory)msg);

        //......................................................................
        // RUN-ITEM-ACTION
        } else if(msg_class == MsgRunItemAction.class) {
            SceneGame.RecvMsgRunItemAction(this, (MsgRunItemAction)msg);

        //......................................................................
        // START-QUEST
        } else if(msg_class == MsgQuestStart.class) {
            SceneGame.RecvMsgStartQuest(this, (MsgQuestStart)msg);
        }

        // Run default response handler
        if(run_default_handler) {
            super.OnServerResponse(msg);
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnActivateScene() {
        // Become alive
        PushState(new StateAlive());

        // Activate
        super.OnActivateScene();
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        super.OnMethodPostRender(ctx, pre_children);

        // Ignore post-children
        if(!pre_children) {
            return true;
        }

        // Not ready
        if(!HasInitState()) {
            return false;
        }

        // Wait until level is ready
        LevelObjectChar hero = Main.inst.level.GetHero();
        if(hero == null) {
            return false;
        }

        // Process current step
        boolean justdoit = true;
        while(justdoit) {
            justdoit = false;
            switch(m_step.state) {
            //......................................................................
            case IDLE: {
                // Process update message if present
                if(m_update_msg != null) {
                    m_step.SetState(Step.State.WAIT_UPDATE);
                    justdoit = true;
                }
            } break;

            //......................................................................
            case WAIT_UPDATE: {
                // Continue waiting until update is received
                if(m_update_msg == null) {
                    break;
                }

                // Process update message
                SceneGame.RecvMsgUpdateScene(this, m_update_msg);

                // Update current game state
                if((GetState() instanceof IGameState)) {
                    ((IGameState)GetState()).Update();
                }

                // Run default handler
                super.OnServerResponse(m_update_msg);

                // Begin processing update message
                m_step.SetState(Step.State.PROCESS_UPDATE, 
                  m_update_msg.game_scene.step);

                // Interrupt current movement
                String interrupt = m_update_msg.game_scene.interrupt;
                if(interrupt != null) {
                    if(GetState().getClass() == StateAlive.class) {
                        StateAlive state = (StateAlive)GetState();

                        // Show resume button if target was not reached
                        boolean is_target_reached = interrupt.equals("target-reached") || 
                          state.move.IsTargetReached();
                        ((EngineWndGame)(state.wnd)).UpdateResumeMovement(!is_target_reached);

                        // Stop movement
                        state.BlockMovement();

                        // Auto-rotate hero to avoid obstacles
                        hero.SetAutoRotation(AutoRotation.SMART)
                          .RunAutoRotation(null, true);
                    }
                }

                // Forget about current update message
                m_update_msg = null;
            }

            //......................................................................
            case PROCESS_UPDATE: {
                // Wait until char movement is over
                if(!Main.inst.level.IsCharMovementOver()) {
                    break;
                }

                // Become idle if next update message was not pre-fetched
                if(m_update_msg == null) {
                    m_step.SetState(Step.State.IDLE);

                    // Notify server that we are idle now
                    Main.inst.proxy_client.Send(
                      MsgHeroInteract.CreateRequest("idle", null));

                // Process pre-fetched update message
                } else {
                    m_step.SetState(Step.State.WAIT_UPDATE);
                    justdoit = true;
                }
            } break;
            } // switch
        } // while
        return true;
    }
}
