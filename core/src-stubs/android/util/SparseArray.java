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
package android.util;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.TreeSet;

//------------------------------------------------------------------------------
public class SparseArray<T> {
    //--------------------------------------------------------------------------
    // XXX: replace with fast and efficient implementation
    private HashMap<Integer, T> m_hash;

    //--------------------------------------------------------------------------
    public SparseArray() {
        m_hash = new HashMap<Integer, T>();
    }

    //--------------------------------------------------------------------------
    public int size() {
        return m_hash.size();
    }

    //--------------------------------------------------------------------------
    public int keyAt(int idx) {
        return new TreeSet<Integer>(m_hash.keySet())
          .toArray(new Integer[0])[idx];
    }

    //--------------------------------------------------------------------------
    public T valueAt(int idx) {
        int i = 0;
        for(T value : m_hash.values()) {
            if(i++ == idx) {
                return value;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public void put(int key, T val) {
        m_hash.put(key, val);
    }

    //--------------------------------------------------------------------------
    public T get(int key) {
        return m_hash.get(key);
    }

    //--------------------------------------------------------------------------
    public void clear() {
        m_hash.clear();
    }

    //--------------------------------------------------------------------------
    public void remove(int key) {
        delete(key);
    }

    //--------------------------------------------------------------------------
    public void delete(int key) {
        m_hash.remove(key);
    }
}
