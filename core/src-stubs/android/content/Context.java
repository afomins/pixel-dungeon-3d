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

package android.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.matalok.pd3d.shared.Logger;

import android.content.res.AssetManager;
import android.content.res.Resources;

public class Context {
    public static final String VIBRATOR_SERVICE = "vibrator";
    public static final int MODE_PRIVATE = 0;

    public Resources getResources() {
        return Resources.stub;
    }

    public AssetManager getAssets() {
        return AssetManager.stub;
    }

    public File getFilesDir() {
        return null;
    }

    public FileInputStream openFileInput(String name) throws IOException {
        Logger.d("IO - reading file :: name=%s", name);
        return new FileInputStream(Gdx.files.local(name).file());
    }

    public FileOutputStream openFileOutput(String name, int mode) throws IOException {
        Logger.d("IO - writing file :: name=%s", name);
        return new FileOutputStream(Gdx.files.local(name).file());
    }
    
    public boolean deleteFile(String name) {
        Logger.d("IO - deleting file :: name=%s", name);
        return Gdx.files.local(name).delete();
    }
}
