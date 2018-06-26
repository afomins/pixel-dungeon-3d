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
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class RendererAttribStack {
    //**************************************************************************
    // IOwner
    //**************************************************************************
    public interface IOwner {
        //----------------------------------------------------------------------
        public void SetAttrib(Object attrib);
        public RendererAttribStack GetAttribStack();
        public void ClearAttrib(long attrib_mask);
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static final Long reset_all = Long.valueOf(-1);

    //--------------------------------------------------------------------------
    public static boolean IsResetAll(HashSet<Long> changes) {
        return (changes.size() == 1 && changes.iterator().next() == reset_all);
    }

    //**************************************************************************
    // Cfg
    //**************************************************************************
    public static class Cfg {
        //----------------------------------------------------------------------
        public String name;

        @SuppressWarnings("rawtypes")
        public ArrayList[] attribs;

        @SuppressWarnings("rawtypes")
        private Set[] m_updated;
        private int m_update_cnt;

        //----------------------------------------------------------------------
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Cfg(String name, Cfg inherit_from, RendererAttrib.Cfg[]... cfgs) {
            this.name = name;
            Utils.Assert(cfgs.length == RendererAttrib.type_array.length, 
              "Failed to create attrib-owner config, wrong attrib count");

            // Create array for storing updates
            m_updated = new Set[RendererAttrib.Type.values().length];
            for(int i = 0; i < m_updated.length; i++) {
                m_updated[i] = new HashSet<Long>();
            }

            // Create and configure attributes
            attribs = new ArrayList[RendererAttrib.type_array.length];
            for(RendererAttrib.Type t : RendererAttrib.type_array) {
                // Initialize
                int t_idx = t.ordinal();
                ArrayList attrib_array = 
                  attribs[t_idx] = t.CreateAttribCfgArray();

                // Inherit
                if(inherit_from != null) {
                    ArrayList inherit_attribs = inherit_from.attribs[t_idx];
                    if(inherit_attribs != null) {
                        attrib_array.addAll(inherit_attribs);
                    }
                }

                // Set
                RendererAttrib.Cfg[] cfg = cfgs[t_idx];
                if(cfg != null) {
                    attrib_array.addAll(Arrays.asList(cfg));
                }
            }
        }

        //--------------------------------------------------------------------------
        public void RegisterUpdate(RendererAttrib.Type type, long id, boolean do_reset_all) {
            @SuppressWarnings("unchecked")
            HashSet<Long> updates = 
              (HashSet<Long>)m_updated[type.ordinal()];

            // Increment update counter
            m_update_cnt++;

            // Ignore if all attributes should be reset
            if(IsResetAll(updates)) {
                return;
            }

            // Reset all attributes
            if(do_reset_all) {
                Logger.d("Registering rendering attribute reset :: cfg-name=%s id=%s", 
                  name, type.name);
                updates.clear();
                updates.add(reset_all);

            // Update single attribute
            } else {
                Logger.d("Registering rendering attribute update :: cfg-name=%s id=%s", 
                  name, type.GetShortName(id));
                updates.add(id);
            }
        }

        //--------------------------------------------------------------------------
        public void ResetUpdates() {
            if(m_update_cnt == 0) {
                return;
            }

            for(int i = 0; i < m_updated.length; i++) {
                m_updated[i].clear();
            }
            m_update_cnt = 0;
        }

        //----------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public RendererAttrib.Cfg GetAttrib(RendererAttrib.Type type, long id) {
            return GetAttrib(attribs[type.ordinal()], id);
        }

        //----------------------------------------------------------------------
        public RendererAttrib.Cfg GetAttrib(ArrayList<? extends RendererAttrib.Cfg> arr, 
          long id) {
            // XXX: do lookup in map to improve performance
            for(int i = 0; i < arr.size(); i++) {
                if(arr.get(i).id == id) {
                    return arr.get(i);
                }
            }
            return null;
        }

        //----------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public void AddAttrib(RendererAttrib.Type type, RendererAttrib.Cfg cfg) {
            attribs[type.ordinal()].add(cfg);
        }

        //----------------------------------------------------------------------
        public void DelAttrib(RendererAttrib.Type type, RendererAttrib.Cfg cfg) {
            attribs[type.ordinal()].remove(cfg);
        }
    }

    //**************************************************************************
    // RendererAttribStack
    //**************************************************************************
    private Cfg m_cfg;
    private IOwner m_owner;

    @SuppressWarnings("rawtypes")
    private RendererAttrib[] m_attribs;

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RendererAttribStack(IOwner owner, Cfg cfg) {
        m_cfg = cfg;
        m_owner = owner;

        // Create attributes
        m_attribs = new RendererAttrib[cfg.attribs.length];
        for(RendererAttrib.Type t : RendererAttrib.Type.values()) {
            int t_idx = t.ordinal();
            m_attribs[t_idx] = 
              t.CreateAttrib().Refresh(cfg.attribs[t_idx], this);
        }
    }

    //--------------------------------------------------------------------------
    public void ApplyUpdates() {
        if(m_cfg.m_update_cnt == 0) {
            return;
        }

        // Process all updates
        for(int i = 0; i < m_cfg.m_updated.length; i++) {
            RendererAttrib.Type type = RendererAttrib.type_array[i];

            // Ignore if attributes were not updated
            @SuppressWarnings("unchecked")
            HashSet<Long> updated = (HashSet<Long>)m_cfg.m_updated[i];
            if(updated.isEmpty()) {
                continue;
            }

            // Refresh all attributes
            if(IsResetAll(updated)) {
                Logger.d("Resetting rendering attributes :: name=%s id=%s", 
                  m_cfg.name, type.name);
                RefreshAttribs(type);

            // Update modified attributes
            } else {
                Iterator<Long> it = updated.iterator();
                while(it.hasNext()) {
                    long id = it.next();
                    Logger.d("Updating rendering attribute :: name=%s id=%s", 
                      m_cfg.name, type.GetShortName(id));

                    RendererAttrib.Cfg cfg = GetAttribCfg(type, id);
                    if(cfg == null) {
                        Logger.e("Failed to update rendering attribute");
                        continue;
                    }
                    UpdateAttrib(type, cfg);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    public IOwner GetOwner() {
        return m_owner;
    }

    //--------------------------------------------------------------------------
    public RendererAttrib.Cfg GetAttribCfg(RendererAttrib.Type type, long id) {
        return m_cfg.GetAttrib(type, id);
    }

    //--------------------------------------------------------------------------
    public void UpdateAttrib(RendererAttrib.Type type, RendererAttrib.Cfg cfg) {
        m_attribs[type.ordinal()].Set(cfg, this);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void RefreshAttribs(RendererAttrib.Type type) {
        int type_idx = type.ordinal();
        m_attribs[type_idx].Refresh(m_cfg.attribs[type_idx], this);
    }
}
