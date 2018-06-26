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

// -----------------------------------------------------------------------------
package com.matalok.pd3d;

// -----------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.INode;

// -----------------------------------------------------------------------------
public class InputMan 
  extends GameNode {
    // *************************************************************************
    // IClient
    // *************************************************************************
    public interface IClient
      extends INode {
        //......................................................................
        public InputProcessor GetInputProcessor();
        public GestureDetector GetGestureDetector();
        public void Process();
    }

    // *************************************************************************
    // InputMan
    // *************************************************************************
    private InputMultiplexer m_mplx;
    private HashMap<String, LinkedList<InputMan.IClient>> m_clients;

    // -------------------------------------------------------------------------
    public InputMan() {
        super("input-manager", 1.0f);

        m_mplx = new InputMultiplexer();
        m_clients = new HashMap<String, LinkedList<InputMan.IClient>>();

        Gdx.input.setInputProcessor(m_mplx);
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
    }

    // -------------------------------------------------------------------------
    public void RegisterClient(InputMan.IClient client, String tag) {
        boolean has_gesture_detector = (client.GetGestureDetector() != null);
        Logger.d("Registering input-client :: client=%s tag=%s gestures=%s", 
          client.SgGetNameId(), tag, Boolean.toString(has_gesture_detector));

        // Initialize tag
        if(!m_clients.containsKey(tag)) {
            m_clients.put(tag, new LinkedList<InputMan.IClient>());
        }

        // Register key-mouse client
        LinkedList<InputMan.IClient> clients = m_clients.get(tag);

        // Make sure that same client is not registered twice
        Utils.Assert(!clients.contains(client.GetInputProcessor()), 
          "Failed to registed input-client, key-mouse client already present :: client=%s", 
          client.SgGetNameId());

        // Register gesture detector client
        if(has_gesture_detector) {
            m_mplx.addProcessor(client.GetGestureDetector());
        }

        // Register key-mouse client
        m_mplx.addProcessor(client.GetInputProcessor());
        clients.add(client);
    }

    // -------------------------------------------------------------------------
    public void UnregisterClient(InputMan.IClient client, String tag) {
        boolean has_gesture_detector = (client.GetGestureDetector() != null);
        Logger.d("Unregistering input client :: client=%s tag=%s gestures=%s", 
          client.SgGetNameId(), tag, Boolean.toString(has_gesture_detector));

        Utils.Assert(m_clients.containsKey(tag), 
          "Failed to unregister input-client, tag is unknown :: client=%s tag=%s",
          client.SgGetNameId(), tag);

        // Remove key-mouse from multiplexor
        m_mplx.removeProcessor(client.GetInputProcessor());

        // Remove gesture-detector from multiplexor
        if(has_gesture_detector) {
            m_mplx.removeProcessor(client.GetGestureDetector());
        }

        // Remove from clients table
        if(!m_clients.get(tag).remove(client)) {
            Utils.Assert(false, 
              "Failed to unregister input-client, client is unknown :: client=%s tag=%s",
              client.SgGetNameId(), tag);
        }
    }

    // -------------------------------------------------------------------------
    public void UnregisterClient(String tag) {
        Logger.d("Unregistering input-clients by tag :: tag=%s", tag);

        LinkedList<InputMan.IClient> client_list = m_clients.get(tag);
        if(client_list == null) {
            return;
        }

        while(client_list.size() > 0) {
            UnregisterClient(client_list.getFirst(), tag);
        }
    }

    // -------------------------------------------------------------------------
    public int GetClientNum(String tag) {
        return !m_clients.containsKey(tag) ? 0 : m_clients.get(tag).size();
    }

    // -------------------------------------------------------------------------
    public boolean IsKeyPressed(int key) {
        return Gdx.input.isKeyPressed(key);
    }

    // -------------------------------------------------------------------------
    public boolean IsKeyPressed(int[] keys) {
        for(int key : keys) {
            if(Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        super.OnMethodPostRender(ctx, pre_children);

        // Ignore post-children
        if(!pre_children) {
            return false;
        }

        // Process all clients
        for(LinkedList<IClient> clients : m_clients.values()) {
            for(IClient c : clients) {
                c.Process();
            }
        }
        return false;
    }
}
 