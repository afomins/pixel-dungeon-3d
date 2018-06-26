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

public class View {

    public interface OnTouchListener {

        boolean onTouch(View view, MotionEvent event);
    }

    public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 0;
    public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 0;
    public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 0;
    public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 0;
    public static final int SYSTEM_UI_FLAG_FULLSCREEN = 0;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 0;
    
    public static View stub = new View();

    public void setSystemUiVisibility(int i) {
        // TODO Auto-generated method stub
        
    }

}
