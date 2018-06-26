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

import com.matalok.pd3d.Main;
import com.matalok.pd3d.Scheduler;
import com.matalok.pd3d.engine.gui.*;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class Engine 
  extends GameNode
  implements Scheduler.IClient {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static final HashMap<String, Class<? extends Scene>> scenes;
    static {
        scenes = new HashMap<String, Class<? extends Scene>>();
        scenes.put("scene-connect", SceneConnect.class);
        scenes.put("scene-disconnect", SceneDisconnect.class);
        scenes.put("scene-about", SceneAbout.class);
        scenes.put("scene-badges", SceneBadges.class);
        scenes.put("scene-game", SceneGame.class);
        scenes.put("scene-inter-level", SceneInterLevel.class);
        scenes.put("scene-intro", SceneIntro.class);
        scenes.put("scene-rankings", SceneRankings.class);
        scenes.put("scene-start", SceneStart.class);
        scenes.put("scene-title", SceneTitle.class);
    }

    //**************************************************************************
    // Engine
    //**************************************************************************
    private Scene m_scene;
    private String m_cur_hero;

    public EngineWndDebug wnd_debug;
    public EngineWndInfo wnd_info;
    public EngineWndQuest wnd_quest;
    public EngineWndInventory wnd_inv;
    public EngineWndItem wnd_item;
    public EngineWndTitle wnd_title;
    public EngineWndStart wnd_start;
    public EngineWndInterLevel wnd_inter_level;
    public EngineWndHeroDesc wnd_hero_desc;
    public EngineWndMainMenu wnd_main_menu;
    public EngineWndSettings wnd_settings;
    public EngineWndGame wnd_game;
    public EngineWndBanner wnd_banner;

    //--------------------------------------------------------------------------
    public Engine() {
        super("engine", 1.0f);

        // Create gui
        wnd_debug = new EngineWndDebug();
        wnd_info = new EngineWndInfo(false);
        wnd_quest = new EngineWndQuest();
        wnd_inv = new EngineWndInventory();
        wnd_item = new EngineWndItem();
        wnd_title = new EngineWndTitle();
        wnd_start = new EngineWndStart();
        wnd_inter_level = new EngineWndInterLevel();
        wnd_hero_desc = new EngineWndHeroDesc();
        wnd_main_menu = new EngineWndMainMenu();
        wnd_settings = new EngineWndSettings();
        wnd_game = new EngineWndGame();
        wnd_banner = new EngineWndBanner();

        // Register scheduler
        Main.inst.scheduler.RegisterClient(Scheduler.Event.SWITCH_ENGINE_SCENE, this);

        // First activation 
        SceneTitle.is_first_activation = true;

        // Default hero
        m_cur_hero = "warrior";
    }

    //--------------------------------------------------------------------------
    private void SwitchScene(String new_scene_name) {
        String old_scene_name = (m_scene == null) ? 
          "none" : m_scene.SgGetName(); 
        Logger.d("Switching engine scene :: %s -> %s", old_scene_name, new_scene_name);

        // Deactivate old scene
        if(m_scene != null) {
            Logger.d("Deactivating old engine scene :: scene=%s", 
              m_scene.SgGetNameId());
            m_scene.OnDeactivateScene();
            Main.inst.sg_man.ScheduleForDeletion(m_scene);
        }

        // Reset current scene
        m_scene = null;

        // Check if new scene is valid 
        if(!Engine.scenes.containsKey(new_scene_name)) { 
            Logger.e("Failed to switch scene, unknown new scene :: %s -> %s",
              old_scene_name, new_scene_name);
            return;
        }

        // Create new scene
        try {
            m_scene = (Scene)SgAddChild(
              Engine.scenes.get(new_scene_name).newInstance());
        } catch (Exception e) {
            Utils.LogException(e, "Failed to switch scene, exception :: %s -> %s",
              old_scene_name, new_scene_name);
            return;
        }

        // Activate new scene
        Logger.d("Activating new engine scene :: scene=%s", m_scene.SgGetNameId());
        Main.inst.proxy_client.SetListener(m_scene);
        m_scene.OnActivateScene();
    }

    //--------------------------------------------------------------------------
    public Scene GetScene() {
        return m_scene;
    }

    //--------------------------------------------------------------------------
    public Scene.State GetState() {
        return m_scene.GetState();
    }

    //--------------------------------------------------------------------------
    public SceneGame GetGameScene(boolean do_assert) {
        boolean is_game_scene = (m_scene != null && 
          m_scene.getClass() == SceneGame.class);

        if(do_assert) {
            Utils.Assert(is_game_scene, "Failed to get game scene");
            return (SceneGame)m_scene;
        } else if(is_game_scene) {
            return (SceneGame)m_scene;
        } else {
            return null;
        }
    }

    //--------------------------------------------------------------------------
    public EngineWndGame GetGameWindow(boolean do_assert) {
        SceneGame game = GetGameScene(do_assert);
        return (EngineWndGame)((game == null) ? null : game.GetStateInit().wnd);
    }

    //--------------------------------------------------------------------------
    public void PopState(int num) {
        for(int i = 0; i < num; i++) {
            m_scene.PopState();
        }
    }

    //--------------------------------------------------------------------------
    public void PushState(Scene.State state) {
        m_scene.PushState(state);
    }

    //--------------------------------------------------------------------------
    public void SetCurHero(String hero) {
        m_cur_hero = hero;
    }

    //--------------------------------------------------------------------------
    public String GetCurHero() {
        return m_cur_hero;
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

        // Bring debug window to front
//        wnd_debug.toFront();
        return true;
    }

    // *************************************************************************
    // ISchedulerClient
    // *************************************************************************
    @Override public void OnEvent(Scheduler.Event event, String arg) {
        switch(event) {
        //......................................................................
        case SWITCH_ENGINE_SCENE: {
            SwitchScene(arg);
        } break;

        //......................................................................
        // Forward event to current scene
        default: {
            if(m_scene != null) {
                m_scene.OnEvent(event, arg);
            }
        } break;
        }
    }
}
