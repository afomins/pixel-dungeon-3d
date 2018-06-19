//------------------------------------------------------------------------------
package com.matalok.pd3d.level;

//------------------------------------------------------------------------------
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.level.object.LevelObject;
import com.matalok.pd3d.level.object.LevelObjectCache;
import com.matalok.pd3d.level.object.LevelObjectCell;
import com.matalok.pd3d.level.object.LevelObjectChar;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class LevelTarget {
    //**************************************************************************
    // LevelTarget
    //**************************************************************************
    private RenderableObject m_obj;
    private LevelObjectChar m_hero;
    private LinkedList<LevelObjectChar> m_chars;
    private LevelObjectChar m_selected_char;

    //--------------------------------------------------------------------------
    public LevelTarget() {
        m_chars = new LinkedList<LevelObjectChar>();
    }

    //--------------------------------------------------------------------------
    public LevelObjectChar GetSelectedChar() {
        return m_selected_char;
    }

    //--------------------------------------------------------------------------
    public String GetDesc() {
        int idx = m_chars.indexOf(m_selected_char);
        return (idx == -1) ? "no target" : String.format("%d/%d", idx + 1, m_chars.size());
    }

    //--------------------------------------------------------------------------
    public boolean UpdateChars(LevelObjectCache char_cache) {
        // Reset previous values
        m_chars.clear();
        m_hero = null;

        // Save available chars in local array ...
        boolean selected_char_is_present = false;
        for(Entry<Integer, LevelObject> e : char_cache.GetEntries()) {
            // Ignore hero
            LevelObjectChar char_obj = (LevelObjectChar)e.getValue();
            if(char_obj.GetPdId() == Main.inst.cfg.lvl_hero_id) {
                m_hero = char_obj;
                continue;
            }

            // Ignore dead chars
            if(!char_obj.IsAlive()) {
                continue;
            }
            m_chars.add(char_obj);

            // Detect if this char was previously selected
            if(char_obj == m_selected_char) {
                selected_char_is_present = true;
            }
        }

        Utils.Assert(m_hero != null, "Failed update target chars, no hero");

        // ... and sort it
        Collections.sort(m_chars, new Comparator<LevelObjectChar>() {
            @Override public int compare(LevelObjectChar obj0, LevelObjectChar obj1) {
                float val0 = obj0.GetPdId(), val1 = obj1.GetPdId();
//                float val0 = m_hero.GetDistanceToTarget(obj0, false);
//                float val1 = m_hero.GetDistanceToTarget(obj1, false);
                return (val0 < val1) ? -1 : (val0 > val1) ? +1 : 0;
            }
        });

        // Reset previously selected char if it's not present anymore
        if(!selected_char_is_present) {
            m_selected_char = null;
        }

        // Return true if selected char is present 
        return (m_chars.size() > 0 && m_selected_char == null);
    }

    //--------------------------------------------------------------------------
    private LevelObjectChar Finalize(LevelObjectChar selected_char) {
        LevelObjectCell selected_char_cell = (selected_char != null) ? 
          selected_char.GetParentCell() : null;
        LevelObjectCell current_target_cell = (m_obj != null) ? 
          (LevelObjectCell)m_obj.SgGetParent(LevelObjectCell.class) : null;

        // Create new target model
        if(m_obj == null && selected_char_cell != null) {
            m_obj = (RenderableObject)selected_char_cell.SgAddChild(
              RenderableObjectType.TARGET.Create());

        // Delete old target model
        } else if(m_obj != null && selected_char_cell == null) {
            Main.inst.sg_man.ScheduleForDeletion(m_obj);
            m_obj = null;

        // Update position of the target
        } else if(selected_char_cell != null && selected_char_cell != current_target_cell) {
            selected_char_cell.SgRelocateChild(m_obj);
            m_obj.TweenPos(new Vector3(), 
              Main.inst.cfg.model_move_duration, null);
        }
        return (m_selected_char = selected_char);
    }

    //--------------------------------------------------------------------------
    public LevelObjectChar SelectTarget(LevelObjectChar char_obj) {
        Utils.Assert(m_chars.contains(char_obj), 
          "Failed to set unknown target :: char=%s", char_obj.SgGetNameId());
        return Finalize(char_obj);
    }

    //--------------------------------------------------------------------------
    public LevelObjectChar SelectAutoTarget() {
        // Select same char if it's already selected
        if(m_selected_char != null) {
            return Finalize(m_selected_char);

        // Select first char in the list
        } else if(m_chars.size() > 0) {
            return Finalize(m_chars.getFirst());

        // Select none
        } else {
            return Finalize(null);
        }
    }

    //--------------------------------------------------------------------------
    public LevelObjectChar SelectNextTarget() {
        // No targets
        if(m_chars.size() == 0) {
            return Finalize(null);

        // Select first char as target
        } else if(m_selected_char == null || m_selected_char == m_chars.getLast()) {
            return Finalize(m_chars.getFirst());

        // Select next char in list
        } else {
            boolean found_selected = false;
            for(LevelObjectChar c : m_chars) {
                if(found_selected) {
                    return Finalize(c);
                }
                if(c == m_selected_char) {
                    found_selected = true;
                }
            }
        }
        return Finalize(null);
    }

    //--------------------------------------------------------------------------
    public int GetTargetNum() {
        return m_chars.size();
    }
}
