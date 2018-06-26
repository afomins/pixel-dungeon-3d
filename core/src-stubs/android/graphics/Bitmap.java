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

package android.graphics;

public class Bitmap {
    public static class Config {
        public static final String ARGB_8888 = null;
    }

    public static Bitmap stub = new Bitmap();

    public static Bitmap createBitmap(int length, int i, String argb8888) {
        return stub;
    }

    public void setPixel(int i, int j, int k) {
        // TODO Auto-generated method stub
        
    }

    public int getWidth() {
        return 256;
    }

    public int getHeight() {
        return 256;
    }

    public void recycle() {
        // TODO Auto-generated method stub
        
    }

    public void eraseColor(int color) {
        // TODO Auto-generated method stub
        
    }

    public void getPixels(int[] pixels, int i, int w, int j, int k, int w2, int h) {
        // TODO Auto-generated method stub
        
    }

    public int getPixel(int pos, int j) {
        return 0xffffffff;
    }

}
