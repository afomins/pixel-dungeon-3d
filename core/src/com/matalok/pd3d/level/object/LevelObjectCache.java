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
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class LevelObjectCache
  extends UtilsClass.Cache<Integer, LevelObject>
  implements IManaged {
    // *************************************************************************
    // LevelObjectCache
    // *************************************************************************
    public LevelObjectCache(String name) {
        super(name, false);
    }

    //--------------------------------------------------------------------------
    public LevelObject Put(LevelObject obj) {
        return Put(obj.GetPdId(), obj);
    }

    //--------------------------------------------------------------------------
    public LevelObject Delete(LevelObject obj) {
        return Delete(obj.GetPdId(), false);
    }

    // *************************************************************************
    // UtilsClass.Cache
    // *************************************************************************
    @Override protected String ToString(LevelObject obj) {
        return obj.SgGetNameId();
    }

    // *************************************************************************
    // IManaged
    // *************************************************************************
    @Override public void OnCleanup() {
        Clear();
    }
}
