// -----------------------------------------------------------------------------
package com.matalok.pd3d.level;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.MathUtils;
import com.matalok.pd3d.level.object.LevelObjectCell;
import com.matalok.pd3d.level.object.LevelObjectTerrain;

//------------------------------------------------------------------------------
public class LevelSmartRotateHelper {
    // **************************************************************************
    // LevelSmartRotateHelper
    // **************************************************************************
    private int m_prev_rotation;
    private int[] m_weight;
    private int[] m_rotation;
    private boolean m_avoid_chasm;

    //--------------------------------------------------------------------------
    public LevelSmartRotateHelper() {
        m_weight = new int[LevelDirection.dir_array.length];
        m_rotation = new int[LevelDirection.dir_array.length];
        SetAvoidChasm(true);
    }

    //--------------------------------------------------------------------------
    public void SetAvoidChasm(boolean value) {
        m_avoid_chasm = value;
    }

    //--------------------------------------------------------------------------
    public void SetPrevRotation(int prev_rotation) {
        m_prev_rotation = MathUtils.clamp(prev_rotation, -1, +1);
    }

    //--------------------------------------------------------------------------
    public int GetBestRotation(LevelObjectCell origin, LevelDirection forward) {
        // Initialize neighbor cells
        InitNeighborWeight(origin, LevelDirection.NORTH,      forward, 0);    // front
        InitNeighborWeight(origin, LevelDirection.NORTH_WEST, forward, -1);   // front-left
        InitNeighborWeight(origin, LevelDirection.WEST,       forward, -2);   // left
        InitNeighborWeight(origin, LevelDirection.SOUTH_WEST, forward, -3);   // back-left
        InitNeighborWeight(origin, LevelDirection.NORTH_EAST, forward, +1);   // front-right
        InitNeighborWeight(origin, LevelDirection.EAST,       forward, +2);   // right
        InitNeighborWeight(origin, LevelDirection.SOUTH_EAST, forward, +3);   // back-right
        InitNeighborWeight(origin, LevelDirection.SOUTH,      forward, +4);   // back

        // Find cell with max weight
        int max_weight_idx = 0;
        for(int i = 0 ; i < m_weight.length; i++) {
            if(m_weight[i] > m_weight[max_weight_idx]) {
                max_weight_idx = i;
            }
        }

        // Return rotation towards cell with max weight 
        return MathUtils.clamp(m_rotation[max_weight_idx], -1, +1);
    }

    //--------------------------------------------------------------------------
    private void InitNeighborWeight(LevelObjectCell origin, LevelDirection neighbor_dir,
      LevelDirection forward_dir, int forward_rot) {
        // Rotate towards neighbor
        forward_dir = forward_dir.Rotate(forward_rot);

        // Get neighbor cell
        LevelObjectCell neighbor = origin.GetNeighbor(forward_dir);

        // Initial weight
        int weight = 0;

        // Set weight of terrain
        LevelObjectTerrain neighbor_terrain = neighbor.GetTerrain();
        if(neighbor_terrain != null) {
            // Increase weight for passable terrain or door
            if(neighbor_terrain.IsTrap() || neighbor_terrain.IsPassable() || 
              neighbor_terrain.HasDoor(-1) || neighbor_terrain.HasWell() || 
              (!m_avoid_chasm && neighbor_terrain.IsChasm())) {
                weight += 100 - Math.abs(forward_rot) * 10;
            }

            // Increase weight for open door or locked boss-door
            if(neighbor_terrain.HasDoor(+1) || 
               neighbor_terrain.HasDoor(+2)) {
                weight += 2;
            }
        }

        // Increase weight if previous rotation matches current one
        if(m_prev_rotation == MathUtils.clamp(forward_rot, -1, +1)) {
            weight += 1;
        }

        // Save weight of neighbor cell and amount of rotations needed to face it
        int neighbor_idx = neighbor_dir.ordinal();
        m_rotation[neighbor_idx] = forward_rot;
        m_weight[neighbor_idx] = weight;
    }
}
