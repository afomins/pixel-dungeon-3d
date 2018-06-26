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
import com.badlogic.gdx.files.FileHandle;

//------------------------------------------------------------------------------
public class SgWriterFile 
  implements SgUtils.IWriter {
    //**************************************************************************
    // SgWriterLog
    //**************************************************************************
    private FileHandle h_file;

    //--------------------------------------------------------------------------
    public SgWriterFile(FileHandle h_file) {
        this.h_file = h_file;
    }

    //**************************************************************************
    // IWriter
    //**************************************************************************
    @Override public void Write(String str, Object... args) {
        h_file.writeString(String.format(str, args) + "\n", true);
    }

    //--------------------------------------------------------------------------
    @Override public void Flush() {
    }

    //**************************************************************************
    // Object
    //**************************************************************************
    @Override public String toString() {
        return h_file.path();
    }
}
