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
package android.content.res;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

//------------------------------------------------------------------------------
public class AssetFileDescriptor {
    //**************************************************************************
    // AssetFileDescriptor
    //**************************************************************************
    private String m_name;
    private FileHandle m_handle;

    //--------------------------------------------------------------------------
    public AssetFileDescriptor(String name) {
        m_name = name;
    }

    //--------------------------------------------------------------------------
    public Object getFileDescriptor() {
        if(m_handle == null) {
            m_handle = Gdx.files.internal(m_name);
        }
        return m_handle;
    }

    //--------------------------------------------------------------------------
    public Object getStartOffset() {
        return null;
    }

    //--------------------------------------------------------------------------
    public Object getLength() {
        return null;
    }

    //--------------------------------------------------------------------------
    public void close() {
        m_handle = null;
    }
}
