//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import java.util.Iterator;
import java.util.LinkedList;

import com.matalok.pd3d.InputMan;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Scheduler;
import com.matalok.pd3d.Scheduler.Event;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.engine.gui.EngineWnd;
import com.matalok.pd3d.engine.gui.EngineWndInfo;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.*;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.proxy.Interfaces.IProxyListener;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class Scene 
  extends GameNode
  implements IProxyListener, Scheduler.IClient {
    //**************************************************************************
    // State
    //**************************************************************************
    public static class State {
        //----------------------------------------------------------------------
        public Scene scene;
        public EngineWnd wnd;
        public boolean is_wnd_owner;
        public String name;

        //----------------------------------------------------------------------
        public State(EngineWnd wnd, boolean is_wnd_owner) {
            this.name = getClass().getSimpleName();
            SetWindow(wnd, is_wnd_owner);
        }

        //----------------------------------------------------------------------
        public void SetScene(Scene scene) {
            this.scene = scene;
        }

        //----------------------------------------------------------------------
        private void UnregisterInput() {
            Main.inst.input_man.UnregisterClient("scene-input");
        }

        //----------------------------------------------------------------------
        private void RegisterInput() {
            for(InputMan.IClient client : GetInput()) {
                Main.inst.input_man.RegisterClient(client, "scene-input");
            }
        }

        //----------------------------------------------------------------------
        public void SetWindow(EngineWnd wnd, boolean is_wnd_owner) {
            this.wnd = wnd;
            this.is_wnd_owner = is_wnd_owner;
        }

        //----------------------------------------------------------------------
        public String GetName() {
            return name;
        }

        //----------------------------------------------------------------------
        public LinkedList<InputMan.IClient> GetInput() {
            return new LinkedList<InputMan.IClient>();
        }

        //----------------------------------------------------------------------
        public void OnActivateState() {
        }

        //----------------------------------------------------------------------
        public void OnResumeState() {
            if(wnd != null) {
                // Rebuild window
                EngineWnd.Visibility visibility = wnd.GetVisbilityState();
                if(visibility == EngineWnd.Visibility.INVISIBLE || 
                  visibility == EngineWnd.Visibility.FADING_OUT) {
                    wnd.Reset();
                    OnBuildWindow(wnd);
                    wnd.Finalize();
                }

                // Focus window
                Main.inst.gui.SetKeyboardFocus(wnd);
            }

            // Register new input
            RegisterInput();
        }

        //----------------------------------------------------------------------
        public void OnPauseState() {
            // Unregister old input
            UnregisterInput();
        }

        //----------------------------------------------------------------------
        public void OnDeactivateState() {
            // Close window
            if(wnd != null && is_wnd_owner) {
                wnd.Close(true);
            }
        }

        //----------------------------------------------------------------------
        public void OnBuildWindow(EngineWnd wnd) {
        }
    }

    //**************************************************************************
    // StateInit
    //**************************************************************************
    public static class StateInit
      extends State {
        //----------------------------------------------------------------------
        public UtilsClass.Callback init_destructor;

        //----------------------------------------------------------------------
        public StateInit(EngineWnd wnd) {
            super(wnd, true);
        }

        //----------------------------------------------------------------------
        public void SetDestructor(UtilsClass.Callback destructor) {
            init_destructor = destructor;
        }

        //----------------------------------------------------------------------
        @Override public void OnDeactivateState() {
            super.OnDeactivateState();

            // Run destructor
            if(init_destructor != null) {
                Logger.d("SCENE-STATE :: running init-destructor");
                init_destructor.Run();
            }
        }
    }

    //**************************************************************************
    // StateShowInfo
    //**************************************************************************
    public static class StateShowInfo
      extends State {
        //----------------------------------------------------------------------
        public String info_title;
        public Enum<?> info_sprite;
        public String info_body;

        //----------------------------------------------------------------------
        public StateShowInfo(String title, Enum<?> sprite, String body) {
            this(Main.inst.engine.wnd_info, title, sprite, body);
        }

        //----------------------------------------------------------------------
        public StateShowInfo(EngineWndInfo wnd, String title, 
          Enum<?> sprite, String body) {
            super(wnd, true);
            info_title = title;
            info_sprite = sprite;
            info_body = body;
        }

        //----------------------------------------------------------------------
        @Override public void OnBuildWindow(EngineWnd wnd) {
            super.OnBuildWindow(wnd);

            // Reset info window
            ((EngineWndInfo)wnd).Set(info_title, info_sprite, info_body, null, 0);
        }
    }

    //**************************************************************************
    // StateDummy
    //**************************************************************************
    public static class StateDummy
      extends State {
        //----------------------------------------------------------------------
        public StateDummy() {
            super(null, false);
        }
    }

    //**************************************************************************
    // StateShowMainMenu
    //**************************************************************************
    public static class StateShowMainMenu
      extends Scene.State {
        //----------------------------------------------------------------------
        public StateShowMainMenu() {
            super(Main.inst.engine.wnd_main_menu, true);
        }
    }

    //**************************************************************************
    // StateShowMainsMenuSettings
    //**************************************************************************
    public static class StateShowMainsMenuSettings
      extends Scene.State {
        //----------------------------------------------------------------------
        public StateShowMainsMenuSettings() {
            super(Main.inst.engine.wnd_settings, true);
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static boolean RecvMsgGetScene(Scene scene, MsgGetScene msg) {
        // Check is current scene is in-sync with server
        MsgGetScene msg_get_scene = (MsgGetScene)msg;
        if(!scene.SwitchScene(msg_get_scene.scene_name)) {
            // Current engine-state not compatible with scene-name. 
            // Switch-scene event has already been issued.

        } else {
            // Current engine state is ok
            scene.m_send_update_req = true;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public static boolean RecvMsgUpdateSprites(Scene scene, MsgUpdateSprites msg) {
        // Update all sprites
        MsgUpdateSprites msg_update_sprite = (MsgUpdateSprites)msg;
        for(DescSprite sprite : msg_update_sprite.sprites) {
            if(!Main.inst.renderable_man.UpdateSprite(sprite)) {
                Logger.e("Failed to update sprite");
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public static boolean RecvMsgUpdateScene(Scene scene, MsgUpdateScene msg) {
        // Make sure that current scene is valid
        MsgUpdateScene msg_update_scene = (MsgUpdateScene)msg;
        if(!scene.SwitchScene(msg_update_scene.scene_name)) {
            Logger.e("Bad engine-scene while processing '%s' response", 
              msg.hdr_name);
        }

        // Write game log
        if(msg_update_scene.log_lines != null) {
            for(String log_line : msg_update_scene.log_lines) {
                Main.inst.engine.GetGameWindow(true)
                  .WriteLog(log_line);
            }
        }

        // Read info messages
        if(msg_update_scene.info_lines != null) {
            String info_msg = "";
            for(String info_line : msg_update_scene.info_lines) {
                info_msg += "\n" + info_line;
            }

            // Push info state
            Main.inst.engine.PushState(
              new Scene.StateShowInfo("Info", MapEnum.TerrainType.SIGN, info_msg));
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public static boolean RecvMsgCommand(Scene scene, MsgCommand msg) {
        boolean update_debug_window = true;
        if(msg.iddqd != null) {
            Main.inst.cfg.cmd_iddqd = msg.iddqd;
        }

        if(msg.item_info_ext != null) {
            Main.inst.cfg.cmd_item_info_ext = msg.item_info_ext;
        }

        if(msg.game_op != null && msg.game_op.equals("save")) {
            Main.inst.save_game.Save(
              msg.game_args[0], msg.game_args[1]);
        }

        // Update GUI
        if(update_debug_window) {
            if(Main.inst.engine.wnd_debug.isVisible()) {
                Main.inst.engine.wnd_debug.Rebuild();
            }
        }
        return true;
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    private boolean m_send_update_req;
    private LinkedList<State> m_states;

    //--------------------------------------------------------------------------
    public Scene(String name) {
        super(name, 1.0f);
        m_states = new LinkedList<State>();
    }

    //--------------------------------------------------------------------------
    public String GetStateStack(String push_new) {
        String stack = "";
        Iterator<State> it = m_states.iterator();
        for(;;) {
            String state_name = null;
            if(it.hasNext()) {
                state_name = it.next().GetName();

            } else if(push_new != null) {
                state_name = push_new;
                push_new = null;
            }

            if(state_name == null) {
                break;
            }

            stack = (stack.length() == 0) ? stack : stack + ", ";
            stack += state_name;
        }
        return stack;
    }

    //--------------------------------------------------------------------------
    public boolean HasInitState() {
        return m_states.size() > 0;
    }

    //--------------------------------------------------------------------------
    public StateInit GetStateInit() {
        State first_state = m_states.getFirst();
        Utils.Assert(m_states.size() > 0 && first_state instanceof StateInit, 
          "Failed to get init state :: scene=%s stack=%s", 
          SgGetName(), GetStateStack(null));
        return (StateInit)first_state;
    }

    //--------------------------------------------------------------------------
    public Scene ClearInitDestructor() {
        GetStateInit().SetDestructor(null);
        return this;
    }

    //--------------------------------------------------------------------------
    public Scene PushState(State state) {
        Logger.d("SCENE-STATE :: scene=%s stack=[%s++]", 
          SgGetName(), GetStateStack(state.GetName()));

        Utils.Assert(m_states.size() > 0 || state instanceof StateInit, 
          "Failed to push first state - not an init state");

        // Link state with curent scene
        Utils.Assert(state.scene == null, 
          "Failed to push state, scene pointer is not empty");
        state.SetScene(this);

        // Pause top state
        if(m_states.size() > 0) {
            m_states.getLast().OnPauseState();
        }

        // Activate & resume new state
        m_states.add(state);
        state.OnActivateState();
        state.OnResumeState();
        return this;
    }

    //--------------------------------------------------------------------------
    public Scene PopState() {
        Logger.d("SCENE-STATE :: scene=%s stack=[%s--]", 
          SgGetName(), GetStateStack(null));
        Utils.Assert(m_states.size() > 0, 
          "Failed to pop scene state :: scene=%s", SgGetName());

        // Pause & deactivate old state
        State old_state = m_states.removeLast();
        old_state.OnPauseState();
        old_state.OnDeactivateState();

        // Resume top state
        if(m_states.size() > 0) {
            m_states.getLast().OnResumeState();
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public Scene PopState(Class<?> target) {
        Logger.d("SCENE-STATE, pop until target reached :: scene=%s target=%s", 
          SgGetName(), target.getSimpleName());
        while(m_states.getLast().getClass() != target) {
            PopState();
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public Class<? extends State> GetStateType() {
        return GetState().getClass();
    }

    //--------------------------------------------------------------------------
    public State GetState() {
        Utils.Assert(m_states.size() > 0, "Failed to get scene state, no state");
        return m_states.getLast();
    }

    //--------------------------------------------------------------------------
    public void OnActivateScene() {
        // Switch server to new scene
        switch(SgGetName()) {
        case "scene-connect":
        case "scene-disconnect": {
            // Ignore
        } break;
        default: {
            Main.inst.proxy_client.Send(
              MsgGetScene.CreateRequest());
        }
        }

        // Make sure all previously registered input-clients with same tag are gone
        Utils.Assert(Main.inst.input_man.GetClientNum(SgGetName()) == 0, 
          "Failed to activate scene, old input-client are present :: scene=%s",
          SgGetNameId());
    }

    //--------------------------------------------------------------------------
    public void OnDeactivateScene() {
        // Unwind state stack
        while(!m_states.isEmpty()) {
            PopState();
        }
    }

    //--------------------------------------------------------------------------
    protected void OnServerResponse(Msg msg) {
        m_send_update_req = false;

        //......................................................................
        // GET-SCENE
        Class<? extends Msg> msg_class = msg.getClass();
        if(msg_class == MsgGetScene.class) {
            Scene.RecvMsgGetScene(this, (MsgGetScene)msg);

        //......................................................................
        // UPDATE-SPRITE
        } else if(msg_class == MsgUpdateSprites.class) {
            Scene.RecvMsgUpdateSprites(this, (MsgUpdateSprites)msg);

        //......................................................................
        // UPDATE-SCENE
        } else if(msg_class == MsgUpdateScene.class) {
            Scene.RecvMsgUpdateScene(this, (MsgUpdateScene)msg);

        //......................................................................
        // COMMAND
        } else if(msg_class == MsgCommand.class) {
            Scene.RecvMsgCommand(this, (MsgCommand)msg);

        //......................................................................
        // Responses that don't need update requests
        } else if(msg_class == MsgRunGame.class || 
                  msg_class == MsgSwitchScene.class ||
                  msg_class == MsgGetInventory.class ||
                  msg_class == MsgHeroInteract.class ||
                  msg_class == MsgRunItemAction.class ||
                  msg_class == MsgLocal.class ||
//                  msg_class == MsgSelectQuickslotItem.class ||
                  msg_class == MsgSelectInventoryItem.class ||
                  msg_class == MsgQuestStart.class ||
                  msg_class == MsgQuestAction.class) {
            m_send_update_req = false;

        //......................................................................
        // Responses that need update requests
        } else if(msg_class == MsgSelectQuickslotItem.class) {
            m_send_update_req = true;

        //......................................................................
        // ERROR
        } else {
            Logger.e("Received unexpected response");
        }

        // Update scene after receiving response from server
        if(m_send_update_req) {
            Main.inst.proxy_client.Send(
              MsgUpdateScene.CreateRequest());
        }
    }

    //--------------------------------------------------------------------------
    protected void OnServerRequest(Msg msg) {
        Logger.e("Received unexpected request");
    }

    //--------------------------------------------------------------------------
    private boolean SwitchScene(String scene_name) {
        // If current client-scene is incompatible with server-scene 
        // then switch client to new scene
        String compatible_scene_name = scene_name;
        if(!SgGetName().equals(compatible_scene_name)) {
            Logger.d("Engine scene is INCOMPATIBLE, switching to new scene :: cur-scene=%s req-scene=%s", 
              SgGetName(), compatible_scene_name);

            Main.inst.scheduler.ScheduleEvent(
              Scheduler.Event.SWITCH_ENGINE_SCENE, compatible_scene_name);
            return false;
        }

        // If current client-scene is equal to server-scene
        // then we can continue processing JSON
        Logger.d("Engine scene is GOOD, continuing...");
        return true;
    }

    //**************************************************************************
    // IProxyListener
    //**************************************************************************
    @Override public void OnConnected() {
    }

    //-------------------------------------------------------------------------
    @Override public void OnDisconnected() {
        Main.inst.scheduler.ScheduleEvent(
          Scheduler.Event.SWITCH_ENGINE_SCENE, "scene-disconnect");
    }

    //--------------------------------------------------------------------------
    @Override public void OnRecv(Msg msg) {
        // Ignore invalid messages
        if(!msg.Validate()) {
            Logger.e("Received invalid message :: code=%s text=%s", 
              msg.status_code.toString(), msg.status_text);
            return;
        }

        // Run message handler
        if(msg.IsResponse()) {
            OnServerResponse(msg);
        } else {
            OnServerRequest(msg);
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnError() {
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodResize(int width, int height) {
        if(!HasInitState()) {
            return true;
        }

        // Build list of visible windows
        LinkedList<EngineWnd> visible_window = new LinkedList<EngineWnd>();
        visible_window.add(Main.inst.engine.wnd_debug);
        for(State state : m_states) {
            if(state.wnd != null) {
                visible_window.add(state.wnd);
            }
        }

        // Rebuild visible windows
        for(EngineWnd w : visible_window) {
            if(w.isVisible()) {
                Logger.d("Resizing windows :: name=%s", w.getClass().getSimpleName());
                w.Rebuild();
            }
        }

        // Request update from server which is needed to update window contents
        Main.inst.proxy_client.Send(
          MsgUpdateScene.CreateRequest());
        return true;
    }

    // *************************************************************************
    // ISchedulerClient
    // *************************************************************************
    @Override public void OnEvent(Event event, String arg) {
    }
}
