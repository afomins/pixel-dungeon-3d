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

package android.content.pm;

public class PackageManager {
    public static PackageManager stub = new PackageManager();

    @SuppressWarnings("serial")
    public class NameNotFoundException 
      extends Exception {
    }

    public PackageInfo getPackageInfo(String packageName, int i) 
      throws NameNotFoundException{
        // TODO Auto-generated method stub
        return PackageInfo.stub;
    }
}
