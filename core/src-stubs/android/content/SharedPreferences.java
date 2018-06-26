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
package android.content;

//------------------------------------------------------------------------------
import java.util.HashMap;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class SharedPreferences {
    //**************************************************************************
    // Editor
    //**************************************************************************
    public static class Editor {
        //----------------------------------------------------------------------
        private HashMap<String, Object> m_map;

        //----------------------------------------------------------------------
        public Editor() {
//            m_map = new HashMap<String, Object>();
            m_map = Main.inst.cfg.pd_preferences;
        }

        //----------------------------------------------------------------------
        public Editor putInt(String key, int value) {
            m_map.put(key, value);
            return this;
        }

        //----------------------------------------------------------------------
        public void commit() {
        }

        //----------------------------------------------------------------------
        public Editor putBoolean(String key, boolean value) {
            m_map.put(key, value);
            return this;
        }

        //----------------------------------------------------------------------
        public Editor putString(String key, String value) {
            m_map.put(key, value);
            return this;
        }

        //----------------------------------------------------------------------
        public Object ReadKey(String key) {
            return m_map.get(key);
        }

        //----------------------------------------------------------------------
        public boolean HasKey(String key, Class<?> c) {
            return m_map.containsKey(key) && 
              ReadKey(key).getClass() == c;
        }
    }

    //**************************************************************************
    // SharedPreferences
    //**************************************************************************
    private Editor m_editor = new Editor();

    //--------------------------------------------------------------------------
    public int getInt(String key, int defValue) {
        return m_editor.HasKey(key, Integer.class) ? 
          (int)m_editor.ReadKey(key) : defValue;
    }

    //--------------------------------------------------------------------------
    public boolean getBoolean(String key, boolean defValue) {
        return m_editor.HasKey(key, Boolean.class) ? 
          (boolean)m_editor.ReadKey(key) : defValue;
    }

    //--------------------------------------------------------------------------
    public String getString(String key, String defValue) {
        return m_editor.HasKey(key, String.class) ? 
          (String)m_editor.ReadKey(key) : defValue;
    }

    //--------------------------------------------------------------------------
    public Editor edit() {
        return m_editor;
    }
}
