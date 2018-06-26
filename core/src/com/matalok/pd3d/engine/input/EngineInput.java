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
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.Map.Entry;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.matalok.pd3d.InputMan;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.engine.gui.EngineWndGame;
import com.matalok.pd3d.shared.Logger;

//------------------------------------------------------------------------------
public class EngineInput
  implements InputMan.IClient, InputProcessor, GestureListener {
    //**************************************************************************
    // Events
    //**************************************************************************
    public enum Event {
        NULL, PRESSED, HOLD, RELEASED, 
        PRESSED_LONG, HOLD_LONG, 
        TAP, TAP_MULTI, SWIPE_LEFT, SWIPE_RIGHT, SWIPE_UP, SWIPE_DOWN, 
        SWIPE_DOWN_TOOLBAR, BLOCKED,
    }

    //--------------------------------------------------------------------------
    public static final int VIRTUAL_KEY = 100000;

    //**************************************************************************
    // EngineInput
    //**************************************************************************
    protected GestureDetector m_gesture_detector;
    protected SceneGame m_game;
    protected HashMap<Integer, Event> m_key_events;
    protected Event m_touch_event;
    protected HashMap<EngineWndGame.Buttons, Event> m_btn_events;
    private boolean m_pan_stopped_at_toolbar;
    private boolean m_touch_up;

    //--------------------------------------------------------------------------
    public EngineInput(SceneGame game, boolean detect_keys, boolean detect_touch, 
      boolean detect_gestures) {
        m_game = game;

        if(detect_keys) {
            m_key_events = new HashMap<Integer, Event>();
        }

        if(detect_touch) {
            m_touch_event = Event.NULL;
        }

        if(detect_gestures) {
            m_gesture_detector = new GestureDetector(this);
            m_gesture_detector.setLongPressSeconds(0.3f);
            m_gesture_detector.setTapCountInterval(0.2f);
        }
    }

    //--------------------------------------------------------------------------
    private void SetTouchEvent(Event touch) {
        Logger.d("Touch event :: owner=%s transition=%s -> %s", 
          getClass().getSimpleName(), m_touch_event, touch);
        m_touch_event = touch;
    }

    //--------------------------------------------------------------------------
    protected Event GetKeyEvent(int keycode) {
        return (m_key_events == null) ? null : m_key_events.get(keycode);
    }

    //--------------------------------------------------------------------------
    protected Event GetTouchEvent() {
        return m_touch_event;
    }

    //--------------------------------------------------------------------------
    public void BlockKeys() {
        Logger.d("Blocking keys");
        if(m_key_events != null && m_key_events.size() > 0) {
            for(Integer key : m_key_events.keySet()) {
                m_key_events.put(key, Event.BLOCKED);
            }
        }
    }

    //--------------------------------------------------------------------------
    public void ResetKeys() {
        Logger.d("Resetting keys");
        if(m_key_events != null && m_key_events.size() > 0) {
            m_key_events.clear();
        }
    }

    //--------------------------------------------------------------------------
    public void BlockTouch() {
        Logger.d("Blocking touch");
        SetTouchEvent(Event.BLOCKED);
    }

    //--------------------------------------------------------------------------
    public void ResetTouch() {
        Logger.d("Resetting touch");
        SetTouchEvent(Event.NULL);
    }

    //--------------------------------------------------------------------------
    public void VirtualKeyDown(int id) {
        keyDown(VIRTUAL_KEY + id);
    }

    //--------------------------------------------------------------------------
    public void VirtualKeyUp(int id) {
        keyUp(VIRTUAL_KEY + id);
    }

    //**************************************************************************
    // InputMan.IClient
    //**************************************************************************
    @Override public InputProcessor GetInputProcessor() {
        return this;
    }

    //--------------------------------------------------------------------------
    @Override public GestureDetector GetGestureDetector() {
        return m_gesture_detector;
    }

    //--------------------------------------------------------------------------
    @Override public void Process() {
        // Update key event
        if(m_key_events != null && m_key_events.size() > 0) {
            for(Entry<Integer, Event> e : m_key_events.entrySet()) {
                Integer key = e.getKey();
                Event event = e.getValue();

                // PRESSED -> HOLD
                if(event == Event.PRESSED) {
                    m_key_events.put(key, Event.HOLD);

                // RELEASED -> NULL
                } else if(event == Event.RELEASED || event == Event.BLOCKED) {
                    m_key_events.put(key, null);
                }
            }
        }

        if(m_touch_event != null) {
            // Stop touch events
            if(m_touch_up) {
                SetTouchEvent(Event.NULL);
                m_touch_up = false;

            // PRESSED -> HOLD
            } else if(m_touch_event == Event.PRESSED) {
                SetTouchEvent(Event.HOLD);

            // PRESSED_LONG -> HOLD_LONG
            } else if(m_touch_event == Event.PRESSED_LONG) {
                SetTouchEvent(Event.HOLD_LONG);
            }
        }
    }

    //**************************************************************************
    // InputProcessor
    //**************************************************************************
    @Override public boolean keyDown(int keycode) {
        if(m_key_events == null) {
            return false;
        }

        // ANY -> PRESSED
        m_key_events.put(keycode, Event.PRESSED);
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean keyUp(int keycode) {
        if(m_key_events == null) {
            return false;
        }

        // ANY -> RELEASED
        m_key_events.put(keycode, Event.RELEASED);
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean keyTyped(char character) {
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(m_touch_event == null) {
            return false;
        }
        SetTouchEvent(Event.PRESSED);
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(m_touch_event == null) {
            return false;
        }
        m_touch_up = true;
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean scrolled(int amount) {
        return false;
    }

    // *************************************************************************
    // GestureListener
    // *************************************************************************
    @Override public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean tap(float x, float y, int count, int button) {
        if(m_touch_event == null) {
            return false;
        }
        if(m_touch_event == Event.HOLD) {
            SetTouchEvent((count == 1) ? Event.TAP : Event.TAP_MULTI);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean longPress(float x, float y) {
        if(m_touch_event == null) {
            return false;
        }
        if(m_touch_event == Event.HOLD) {
            SetTouchEvent(Event.PRESSED_LONG);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean fling(float velocityX, float velocityY, int button) {
        if(m_touch_event == null) {
            return false;
        }

        if(m_touch_event != Event.HOLD) {
            return false;
        }

        // Horizontal swipe
        if(Math.abs(velocityX) > Math.abs(velocityY)) {
            SetTouchEvent(velocityX > 0 ? 
              Event.SWIPE_RIGHT : Event.SWIPE_LEFT);

        // Vertical swipe
        } else {
            if(velocityY > 0) {
                SetTouchEvent(m_pan_stopped_at_toolbar ? 
                  Event.SWIPE_DOWN_TOOLBAR : Event.SWIPE_DOWN);
            } else {
                SetTouchEvent(Event.SWIPE_UP);
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean panStop(float x, float y, int pointer, int button) {
        if(m_touch_event == null) {
            return false;
        }

        // Was finger lifted at toolbar level?
        EngineWndGame game_wnd = Main.inst.engine.GetGameWindow(true);
        int screen_height = Main.inst.renderer.GetHeight();
        m_pan_stopped_at_toolbar = 
          (game_wnd != null && game_wnd.IsToolbarCoord((int)x, screen_height - (int)y));
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, 
      Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    // -------------------------------------------------------------------------
    @Override public void pinchStop() {
    }

    //**************************************************************************
    // INode
    //**************************************************************************
    @Override public int SgGetId() {
        return 0;
    }

    // -------------------------------------------------------------------------
    @Override public String SgGetName() {
        return getClass().getSimpleName();
    }

    // -------------------------------------------------------------------------
    @Override public String SgGetNameId() {
        return SgGetName();
    }
}
