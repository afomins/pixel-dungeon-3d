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
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.shared.Logger;

//------------------------------------------------------------------------------
public class PlatformUtils {
    //--------------------------------------------------------------------------
    public static PlatformAPI api = null;

    //--------------------------------------------------------------------------
    public static FileHandle OpenInternalFile(String name, boolean try_pd3d) {
        FileHandle h_file = null;
        if(try_pd3d) {
            h_file = Gdx.files.internal("pd3d_" + name);
        }

        if(h_file == null || !h_file.exists()) {
            h_file = Gdx.files.internal(name);
        }
        return h_file;
    }

    //--------------------------------------------------------------------------
    public static FileHandle OpenLocalFile(String name, boolean do_clear) {
        FileHandle h_file = Gdx.files.local(name);
        if(do_clear) {
            h_file.writeString("", false);
        }
        return h_file;
    }

    //--------------------------------------------------------------------------
    public static void LogFile(String name) {
        // Open file
        FileHandle h_file = OpenLocalFile(name, false);
        Logger.i("Logging local file :: name=%s exists=%s", 
          name, h_file.exists() ? "yes" : "no");
        if(!h_file.exists()) {
            return;
        }

        // Line line-by-line
        Logger.i("vvvvvv");
        for(String line : h_file.readString().split("[\\r\\n]+")) {
            Logger.i(">>>" + line);
        }
        Logger.i("^^^^^^");
    }
}
