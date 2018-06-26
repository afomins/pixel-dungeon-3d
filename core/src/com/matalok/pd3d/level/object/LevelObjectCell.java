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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.level.LevelDirection;
import com.matalok.pd3d.level.LevelTrashBin;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass.Vector2i;
import com.matalok.scenegraph.SgNode;

//------------------------------------------------------------------------------
public class LevelObjectCell 
  extends LevelObject {
    // *************************************************************************
    // LevelObjectCell
    // *************************************************************************
    private LevelObjectCell m_cache_neighbors[];
    private LevelObjectCache m_cache_all;
    private Vector2i m_level_pos;

    //--------------------------------------------------------------------------
    public LevelObjectCell(String name, int pd_id, int x, int y, LevelObjectCache cache) {
        super(name, pd_id);
        m_cache_all = cache;
        m_level_pos = new Vector2i(x, y);
    }

    //--------------------------------------------------------------------------
    public void InitNeighbors(int width) {
        // Clear old neighbors
        if(m_cache_neighbors != null) {
            for(int i = 0; i < m_cache_neighbors.length; i++) {
                m_cache_neighbors[i] = null;
            }
        } else {
            m_cache_neighbors = new LevelObjectCell[LevelDirection.GetDirNum()];
        }

        // Initialize cell neighbors
        int idx = GetPdId();
        HashMap<LevelDirection, Integer> neighbors = new HashMap<LevelDirection, Integer>();
        neighbors.put(LevelDirection.EAST,          idx + 1);
        neighbors.put(LevelDirection.NORTH_EAST,    idx + 1 - width);
        neighbors.put(LevelDirection.NORTH,         idx - width);
        neighbors.put(LevelDirection.NORTH_WEST,    idx - width - 1);
        neighbors.put(LevelDirection.WEST,          idx - 1);
        neighbors.put(LevelDirection.SOUTH_WEST,    idx + width - 1);
        neighbors.put(LevelDirection.SOUTH,         idx + width);
        neighbors.put(LevelDirection.SOUTH_EAST,    idx + width + 1);

        // Link cell to it's neighbors
        for(Entry<LevelDirection, Integer> e : neighbors.entrySet()) {
            LevelDirection dir = e.getKey();
            idx = e.getValue();
            m_cache_neighbors[dir.ordinal()] = (LevelObjectCell)m_cache_all.Get(idx);
        }
    }

    //--------------------------------------------------------------------------
    public LevelObjectCell GetNeighbor(LevelDirection dir) {
        return m_cache_neighbors[dir.ordinal()];
    }

    //--------------------------------------------------------------------------
    public LevelDirection IsNeighbor(LevelObjectCell cell) {
        for(int i = 0; i < m_cache_neighbors.length; i++) {
            if(m_cache_neighbors[i] == cell) {
                return LevelDirection.dir_array[i];
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public LevelObjectTerrain GetNeighborTerrain(LevelDirection dir) {
        LevelObjectCell neigh = GetNeighbor(dir);
        if(neigh == null) {
            return null;
        }

        LevelObjectTerrain terrain = neigh.GetTerrain();
        Utils.Assert(terrain != null, 
          "Failed to get neighbor terrain, no terrain :: neighbor=%s", 
          neigh.SgGetNameId());
        return terrain;
    }

    //--------------------------------------------------------------------------
    public Vector2i GetLevelPos() {
        return m_level_pos;
    }

    //--------------------------------------------------------------------------
    public LevelObjectCell GetCell(int id) {
        return (LevelObjectCell)m_cache_all.Get(id);
    }

    //--------------------------------------------------------------------------
    public LevelObjectTerrain GetTerrain() {
        Iterator<SgNode> it = SgGetChildren();
        while(it.hasNext()) {
            SgNode child = it.next();
            if(child instanceof LevelObjectTerrain) {
                // Return first terrain instance
                return (LevelObjectTerrain)child;
            }
        }
        return null;
    }

    // *************************************************************************
    // LevelObject
    // *************************************************************************
    @Override public void OnUpdate(IUpdateCtx ctx, LevelTrashBin trash_bin) {
        super.OnUpdate(ctx, trash_bin);

        // Cell inherits initial position from terrain's update context
        Vector3 pos = ((LevelObjectTerrain.UpdateCtx)ctx).pos;
        GetLocalPos(true).set(pos);
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        for(int i = 0; i < m_cache_neighbors.length; i++) {
            m_cache_neighbors[i] = null;
        }
        m_cache_neighbors = null;
        m_cache_all = null;
        return true;
    }
}
