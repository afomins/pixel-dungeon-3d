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

package android.view;

public class MotionEvent {

    public static final int ACTION_MASK = 0;
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_POINTER_DOWN = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_POINTER_UP = 3;
    public static final int ACTION_UP = 4;

    public int getAction() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Integer getPointerId(int i) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getActionIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPointerCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void recycle() {
        // TODO Auto-generated method stub
        
    }

    public float getX(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    public float getY(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static MotionEvent obtain(MotionEvent event) {
        // TODO Auto-generated method stub
        return null;
    }

}
