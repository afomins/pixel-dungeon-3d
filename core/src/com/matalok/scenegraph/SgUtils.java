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
public class SgUtils {
    //**************************************************************************
    // IPlatform
    //**************************************************************************
    public static interface IPlatform {
        //----------------------------------------------------------------------
        public void Dbg(String fmt, Object... args);
        public void Inf(String fmt, Object... args);
        public void Err(String fmt, Object... args);
        public void Assert(boolean statement, String fmt, Object... args);
    };

    //**************************************************************************
    // IManaged
    //**************************************************************************
    public interface IManaged {
        //----------------------------------------------------------------------
        public void OnCleanup();
    }

    //**************************************************************************
    // INode
    //**************************************************************************
    public interface INode {
        //----------------------------------------------------------------------
        public int SgGetId();
        public String SgGetName();
        public String SgGetNameId();
    }

    //**************************************************************************
    // IWriter
    //**************************************************************************
    public interface IWriter {
        //----------------------------------------------------------------------
        public void Write(String str, Object... args);
        public void Flush();
    }
}
