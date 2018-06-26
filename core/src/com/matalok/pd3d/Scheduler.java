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
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgUtils.INode;

//------------------------------------------------------------------------------
public class Scheduler 
  extends GameNode {
    //**************************************************************************
    // Event
    //**************************************************************************
    public enum Event {
        QUIT,
        SWITCH_ENGINE_SCENE,
        SAVE_SNAPSHOT,
        LOAD_NEXT_SNAPSHOT,
        LOG_SCENE_GRAPH,
    }

    //**************************************************************************
    // EventCtx
    //**************************************************************************
    private class EventCtx {
        //----------------------------------------------------------------------
        public Event id;
        public String arg;

        //----------------------------------------------------------------------
        public EventCtx(Event id, String arg) {
            this.id = id; this.arg = arg;
        }
    }

    //**************************************************************************
    // IClinet
    //**************************************************************************
    public interface IClient 
      extends INode {
        public void OnEvent(Event event, String arg);
    }

    //**************************************************************************
    // Scheduler
    //**************************************************************************
    private UtilsClass.TimeQueue<EventCtx> m_event_queue, m_event_queue_copy;
    private UtilsClass.Callback m_event_queue_cb;
    private HashMap<Event, LinkedList<IClient>> m_clients;
    private LinkedList<IClient> m_clients_all;

    //--------------------------------------------------------------------------
    public Scheduler() {
        super("scheduler", 1.0f);

        m_event_queue = new UtilsClass.TimeQueue<EventCtx>();
        m_event_queue_copy = new UtilsClass.TimeQueue<EventCtx>();
        m_clients = new HashMap<Event, LinkedList<IClient>>();
        m_clients_all = new LinkedList<IClient>();
    }

    //--------------------------------------------------------------------------
    public void ScheduleEvent(Event evt_id) {
        ScheduleEvent(evt_id, null, 0, true);
    }

    //--------------------------------------------------------------------------
    public void ScheduleEvent(Event evt_id, String arg) {
        ScheduleEvent(evt_id, arg, 0, true);
    }

    //--------------------------------------------------------------------------
    public void ScheduleEvent(Event evt_id, String arg, long timeout) {
        ScheduleEvent(evt_id, arg, timeout, true);
    }

    //--------------------------------------------------------------------------
    public void ScheduleEvent(Event evt_id, String arg, long timeout, Boolean relative_time) {
        EventCtx event = new EventCtx(evt_id, arg);
        long trigger_time = timeout + ((relative_time) ? Main.inst.timer.GetCur() : 0);
        m_event_queue.Put(new EventCtx(evt_id, arg), trigger_time);

        Logger.d("Scheduling event :: event=%s args=%s fire_time=%d queue_size=%d", 
            event.id, arg, trigger_time, m_event_queue.GetSize());
    }

    //--------------------------------------------------------------------------
    public void RegisterClient(IClient client) {
        RegisterClient(null, client); // Register to all events
    }

    //--------------------------------------------------------------------------
    public void RegisterClient(Event evt_id, IClient client) {
        LinkedList<IClient> client_list = null;
        if(evt_id == null) {
            // Register client for all events
            client_list = m_clients_all;
        } else if(!m_clients.containsKey(evt_id)) {
            // Register client for new event
            client_list = new LinkedList<IClient>();
            m_clients.put(evt_id, client_list);
        } else {
            // Register client for existing event
            client_list = m_clients.get(evt_id);
        }
        client_list.add(client);
        Logger.d("Registering scheduler client :: name=%s event=%s", 
            client.SgGetNameId(), evt_id);
    }

    //--------------------------------------------------------------------------
    private void FireEvent(EventCtx event, Long trigger_time) {
        // Fire event to clients that registered for all events
        FireEvent(event, m_clients_all, trigger_time);

        // Fire event to clients that registered this event 
        if(m_clients.containsKey(event.id)) {
            FireEvent(event, m_clients.get(event.id), trigger_time);
        }
    }

    //--------------------------------------------------------------------------
    private void FireEvent(EventCtx event, List<IClient> clients, Long trigger_time) {
        ListIterator<IClient> it = clients.listIterator();
        while(it.hasNext()) {
            IClient client = it.next();
            Logger.d("Firing event :: client=%s event=%s args=%s trigger_time=%d(%d) queue_size=%d", 
                    client.SgGetNameId(), event.id, 
                    event.arg, trigger_time, Main.inst.timer.GetCur(), m_event_queue_copy.GetSize());

            client.OnEvent(event.id, event.arg);
        }
    }

    //--------------------------------------------------------------------------
    private void SwapEventQueues() {
        UtilsClass.TimeQueue<EventCtx> tmp = m_event_queue;
        m_event_queue = m_event_queue_copy;
        m_event_queue_copy = tmp;
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Swap 'event_queue_copy' <--- 'm_event_queue'
        SwapEventQueues();

        if(m_event_queue_cb == null) {
            m_event_queue_cb = new UtilsClass.Callback() {
                @Override public Object Run(Object... args) {
                    FireEvent((EventCtx)args[0], (Long)args[1]);
                    return null;
                }
            };
        }
        m_event_queue_copy.Run(Main.inst.timer.GetCur(), m_event_queue_cb);

        // Swap back 'event_queue_copy' ---> 'm_event_queue'
        SwapEventQueues();

        // Reschedule new events
        m_event_queue.Put(m_event_queue_copy);

        // Cleanup temporary event queue
        m_event_queue_copy.Clear();
        return true;
    }
}
