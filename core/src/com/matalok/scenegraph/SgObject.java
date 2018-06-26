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
package com.matalok.scenegraph;

//------------------------------------------------------------------------------
import java.util.Comparator;

import com.matalok.scenegraph.SgUtils.IManaged;
import com.matalok.scenegraph.SgUtils.INode;
import com.matalok.scenegraph.SgUtils.IPlatform;

//------------------------------------------------------------------------------
public class SgObject
  implements IPlatform, IManaged, INode {
    //**************************************************************************
    // PriorityComparator
    //**************************************************************************
    public static class PriorityComparator
      implements Comparator<SgObject> {
        //----------------------------------------------------------------------
        @Override public int compare(SgObject o1, SgObject o2) {
            int p1 = o1.SgGetPriority(), p2 = o2.SgGetPriority();
            if(p1 < p2) {
                return -1;
            } else if(p1 > p2) {
                return +1;
            } else {
                return 0;
            }
        }
    };

    //**************************************************************************
    // SgObjectNoLog 
    //**************************************************************************
    public static class SgObjectNoLog 
      extends SgObject {
        //----------------------------------------------------------------------
        public SgObjectNoLog(int id, String name) {
            super(id, name);
        }

        //**********************************************************************
        // SgUtils.IPlatform
        //**********************************************************************
        @Override public void Dbg(String fmt, Object... args) { }
        @Override public void Inf(String fmt, Object... args) { }
        @Override public void Err(String fmt, Object... args) { }
        @Override public void Assert(boolean statement, String fmt, Object... args) { }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    protected static PriorityComparator priority_comparator = 
      new PriorityComparator();

    //--------------------------------------------------------------------------
    public static IPlatform platform = null;
    public static void SetPlatform(IPlatform plat) {
        SgObject.platform = plat;
    }

    //**************************************************************************
    // SgObject
    //**************************************************************************
    protected int m_sg_id;
    protected String m_sg_name;
    protected String m_sg_name_id;
    protected int m_sg_priority;

    //--------------------------------------------------------------------------
    public SgObject(int id, String name) {
        m_sg_id = id;
        m_sg_name = name;
        m_sg_name_id = String.format("%s:%d", m_sg_name, m_sg_id);

        // Id is default priority
        SgSetPriority(id);
    }

    //--------------------------------------------------------------------------
    public int SgGetPriority() {
        return m_sg_priority;
    }

    //--------------------------------------------------------------------------
    public void SgSetPriority(int priority) {
        m_sg_priority = priority;
    }

    //**************************************************************************
    // SgUtils.INode
    //**************************************************************************
    @Override public int SgGetId() {
        return m_sg_id;
    }

    //--------------------------------------------------------------------------
    @Override public String SgGetName() {
        return m_sg_name;
    }

    //--------------------------------------------------------------------------
    @Override public String SgGetNameId() {
        return m_sg_name_id;
    }

    //**************************************************************************
    // SgUtils.IPlatform
    //**************************************************************************
    @Override public void Dbg(String fmt, Object... args) {
        SgObject.platform.Dbg(fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Inf(String fmt, Object... args) {
        SgObject.platform.Inf(fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Err(String fmt, Object... args) {
        SgObject.platform.Err(fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Assert(boolean statement, String fmt, Object... args) {
        SgObject.platform.Assert(statement, fmt, args);
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
//        m_sg_name = null;
//        m_sg_name_id = null;
    }
}
