// -----------------------------------------------------------------------------
package com.matalok.pd3d.level;

// *****************************************************************************
// ModelDirection
// *****************************************************************************
public enum LevelDirection {
    EAST        ("e",     0.0f),
    NORTH_EAST  ("ne",   45.0f),
    NORTH       ("n",    90.0f),
    NORTH_WEST  ("nw",  135.0f),
    WEST        ("w",   180.0f),
    SOUTH_WEST  ("sw",  225.0f),
    SOUTH       ("s",   270.0f),
    SOUTH_EAST  ("se",  315.0f);

    //--------------------------------------------------------------------------
    public static final LevelDirection[] dir_array = LevelDirection.values();

    //--------------------------------------------------------------------------
    public static float GetStepSize() {
        return 360.0f / dir_array.length;
    }

    //--------------------------------------------------------------------------
    public static int GetDirNum() {
        return dir_array.length;
    }

    //--------------------------------------------------------------------------
    private String m_short;
    private float m_angle;

    //--------------------------------------------------------------------------
    private LevelDirection(String short_name, float angle) {
        m_short = short_name;
        m_angle = angle;
    }

    //--------------------------------------------------------------------------
    public float GetAngle() {
        return m_angle;
    }

    //--------------------------------------------------------------------------
    public int GetDiff(LevelDirection target) {
        int diff = target.ordinal() - ordinal();
        int half_rot = dir_array.length / 2;
        if(diff > half_rot) {
            diff -= dir_array.length;
        } else if(diff < half_rot) {
            diff += dir_array.length;
        }
        return diff;
    }

    //--------------------------------------------------------------------------
    public boolean IsDiagonal() {
        return (ordinal() % 2 == 1);
    }

    //--------------------------------------------------------------------------
    public LevelDirection Rotate(int dir) {
        int idx = (ordinal() + dir) % dir_array.length;
        if(idx < 0) idx += dir_array.length;
        return dir_array[idx];
    }

    //--------------------------------------------------------------------------
    public LevelDirection Opposite() {
        return Rotate(dir_array.length / 2);
    }

    //--------------------------------------------------------------------------
    @Override public String toString() {
      return m_short;
    }
}
