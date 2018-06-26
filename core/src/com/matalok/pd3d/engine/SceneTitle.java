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
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Scheduler.Event;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class SceneTitle 
  extends Scene {
    //**************************************************************************
    // StateInit
    //**************************************************************************
    public class StateInit
      extends Scene.StateInit {
        //----------------------------------------------------------------------
        public StateInit() {
            super(Main.inst.engine.wnd_title);

            // Default destructor quits game
            SetDestructor(new UtilsClass.Callback() {
                @Override public Object Run(Object... args) {
                    Main.inst.scheduler.ScheduleEvent(Event.QUIT);
                    return null;
                }
            });
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static boolean is_first_activation = true;

    //**************************************************************************
    // SceneTitle
    //**************************************************************************
    public SceneTitle() {
        super("scene-title");
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    @Override public void OnActivateScene() {
        super.OnActivateScene();

        // Start snapshot auto-switching during first activation
        if(is_first_activation) {
            Main.inst.snapshot.StartAutoswitch();
            is_first_activation = false;
        }

        // Start rotation around hero
        Main.inst.level_camera.StartCircleRotation(false);

        // Show title window
        PushState(new StateInit());
    }
}
