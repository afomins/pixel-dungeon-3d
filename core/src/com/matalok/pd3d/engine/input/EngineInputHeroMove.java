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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.engine.SceneGame.Step;
import com.matalok.pd3d.engine.gui.EngineWndGame;
import com.matalok.pd3d.level.LevelDirection;
import com.matalok.pd3d.level.object.LevelObject;
import com.matalok.pd3d.level.object.LevelObjectCell;
import com.matalok.pd3d.level.object.LevelObjectChar;
import com.matalok.pd3d.level.object.LevelObjectChar.AutoRotation;
import com.matalok.pd3d.msg.MsgHeroInteract;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class EngineInputHeroMove
  extends EngineInput {
    //**************************************************************************
    // Mode
    //**************************************************************************
    public enum TouchMode {
        MOVE_FORWARD,
        MOVE_TARGET,
    }

    //**************************************************************************
    // EngineInputMoveHero
    //**************************************************************************
    private TouchMode m_touch_mode;
    private int m_prev_step_idx;
    private long m_next_move_delay;
    private LevelObject m_target;
    private boolean m_is_auto_moving;

    //--------------------------------------------------------------------------
    public EngineInputHeroMove(SceneGame game) {
        super(game, true, true, true);
        m_prev_step_idx = -1;
        m_next_move_delay = 100;
        SetTouchMode(TouchMode.MOVE_FORWARD);
    }

    //--------------------------------------------------------------------------
    public void SetTouchMode(TouchMode touch_mode) {
        m_touch_mode = touch_mode;
    }

    //--------------------------------------------------------------------------
    public void SetTarget(LevelObject target, boolean is_auto_moving) {
        if(target != null) {
            // Char which stands on target cell becomes target
            if(target.getClass() == LevelObjectCell.class) {
                LevelObjectChar target_char = 
                  (LevelObjectChar)target.SgGetChild(LevelObjectChar.class);
                if(target_char != null) {
                    target = target_char;
                }
            }

            // Validate target type
            Utils.Assert(target.getClass() == LevelObjectCell.class || 
              target.getClass() == LevelObjectChar.class, 
              "Failed to set movement target, wrong type :: type=%s", target.toString());
        }

        m_target = target;
        m_is_auto_moving = is_auto_moving;
    }

    //--------------------------------------------------------------------------
    public LevelObjectCell GetTargetCell() {
        // No target
        if(m_target == null) {
            return null;

        // Cell is target
        } else if(m_target.getClass() == LevelObjectCell.class) {
            return (LevelObjectCell)m_target;

        // Cell is targets parent
        } else if(m_target.getClass() == LevelObjectChar.class) {
            return ((LevelObjectChar)m_target).GetParentCell();

        // Unknown target type
        } else {
            return null;
        }
    }

    //--------------------------------------------------------------------------
    public int GetTargetCellId() {
        LevelObjectCell cell = GetTargetCell();
        return (cell == null) ? -1 : cell.GetPdId();
    }

    //--------------------------------------------------------------------------
    public boolean IsTargetReached() {
        int target_cell_id = GetTargetCellId();
        int hero_cell_id = Main.inst.level.GetHero().GetParentCell().GetPdId();
        return (target_cell_id == -1 || hero_cell_id == target_cell_id);
    }

    //--------------------------------------------------------------------------
    public boolean IsAutoMoving() {
        return m_is_auto_moving;
    }

    //--------------------------------------------------------------------------
    public void DisableAutoMoving() {
        m_is_auto_moving = false;
    }

    //--------------------------------------------------------------------------
    public boolean IsMoveAllowed() {
        // Movement is not allowed because it is not 1st in this step
        Step step = m_game.GetStep();
        if(step.idx == m_prev_step_idx) {
            return false;

        // Allow movement
        } else if(step.state == Step.State.IDLE || 
          step.state == Step.State.PROCESS_UPDATE && 
          step.GetDuration() > m_next_move_delay) {
            m_prev_step_idx = step.idx;
            return true;

        // Forbid movement
        } else {
            return false;
        }
    }

    //--------------------------------------------------------------------------
    public void MoveForward(boolean forward) {
        // Check if we are allowed to make next step
        if(!IsMoveAllowed()) {
            return;
        }

        // Hide resume button when moving
        ((EngineWndGame)(m_game.GetState().wnd)).UpdateResumeMovement(false);
        SetTarget(null, false);

        // Begin smart auto-rotation to avoid obstacles
        LevelDirection hero_dir = Main.inst.level.GetHero()
          .SetAutoRotation(AutoRotation.SMART).RunAutoRotation(null, forward);

        // Send movement request to server
        MsgHeroInteract msg = 
          MsgHeroInteract.CreateRequest("move-" + hero_dir.toString(), null);
        msg.try_interrupt = false;
        Main.inst.proxy_client.Send(msg);
    }

    //--------------------------------------------------------------------------
    public void MoveTarget(int target_cell_id, boolean try_interrupt) {
        // Lock hero movement when holding SHIFT/CTRL buttons
        LevelObjectChar hero = Main.inst.level.GetHero();
        Event lock_move_evt = GetKeyEvent(Keys.SHIFT_LEFT);
        Event lock_rotate_evt = GetKeyEvent(Keys.CONTROL_LEFT);
        if(lock_move_evt != null) {
            LevelObjectCell cell = Main.inst.level.GetCell(target_cell_id);
            if(cell != null) {
                if(lock_rotate_evt == null) {
                    hero.Rotate(cell);
                }
                SetTarget(null, false);
            }
            return;
        }

        // Check if we are allowed to make next step
        if(!IsMoveAllowed() || target_cell_id == -1) {
            return;
        }

        // Hide resume button when moving
        ((EngineWndGame)(m_game.GetState().wnd)).UpdateResumeMovement(false);

        // Begin simple auto-rotation to make sure that hero is always facing
        // direction of movement
        hero.SetAutoRotation(AutoRotation.SIMPLE);

        // Send movement request to server
        MsgHeroInteract msg = 
          MsgHeroInteract.CreateRequest("move-" + target_cell_id, null);
        msg.try_interrupt = try_interrupt;
        Main.inst.proxy_client.Send(msg);
    }

    //--------------------------------------------------------------------------
    public void ResumeTarget() {
        // Unblock key & touch events
        ResetKeys();
        ResetTouch();

        // Resume auto-movement
        LevelObjectCell cell = GetTargetCell();
        if(cell != null) {
            SetTarget(m_target, true);
            MoveTarget(cell.GetPdId(), true);

            // Show marker
            Main.inst.level.CreateMarker(cell, RenderableObjectType.MARKER_SELECT);
        }
    }

    //**************************************************************************
    // InputMan.IClient
    //**************************************************************************
    @Override public void BlockTouch() {
        super.BlockTouch();
        DisableAutoMoving();
    }

    //--------------------------------------------------------------------------
    @Override public void Process() {
        // Hero should be alive
        LevelObjectChar hero = Main.inst.level.GetHero();
        if(hero == null || !hero.IsAlive()) {
            super.Process();
            return;
        }

        //
        // Process keyboard event
        //
        EngineInput.Event btn_fwd_evt = GetKeyEvent(VIRTUAL_KEY + 1);
        EngineInput.Event key_fwd_evt = GetKeyEvent(Keys.UP);
        EngineInput.Event key_back_evt = GetKeyEvent(Keys.DOWN);

        // Move forward
        boolean is_key_event = true;
        if(key_fwd_evt == Event.PRESSED || 
           key_fwd_evt == Event.HOLD || 
           btn_fwd_evt == Event.PRESSED ||
           btn_fwd_evt == Event.HOLD) {
            MoveForward(true);

        // Move backward
        } else if(key_back_evt == Event.PRESSED || 
                  key_back_evt == Event.HOLD) {
            MoveForward(false);

        // Not a key event
        } else {
            is_key_event = false;
        }

        // Ignore touch events if key event was processed
        if(is_key_event) {
            SetTarget(null, false);
            super.Process();
            return;
        }

        //
        // Process touch event
        //
        Event touch_evt = GetTouchEvent();

        // MOVE_FORWARD
        if(m_touch_mode == TouchMode.MOVE_FORWARD) {
            // Move forward
            if(touch_evt == Event.TAP || 
               touch_evt == Event.TAP_MULTI || 
               touch_evt == Event.PRESSED_LONG ||
               touch_evt == Event.HOLD_LONG) {
                MoveForward(true);

            // Move backward
            } else if(touch_evt == Event.SWIPE_DOWN_TOOLBAR) {
                MoveForward(false);
            }

        // MOVE_TARGET
        } else if(m_touch_mode == TouchMode.MOVE_TARGET) {
            // TAP & TAP_MULTI
            if(touch_evt == Event.TAP || 
               touch_evt == Event.TAP_MULTI) {
                // Stop auto-target if it was active
                if(IsAutoMoving()) {
                    DisableAutoMoving();
                    Main.inst.engine.wnd_game.UpdateResumeMovement(true);

                // Start auto-target if it was not active
                } else {
                    // Reset target
                    SetTarget(null, false);

                    // Select cell 
                    LevelObjectCell cell = Main.inst.level.GetSelectedCell(
                      Gdx.input.getX(), Gdx.input.getY(), true, true);

                    // Cell was selected
                    if(cell != null) {
                        // Move one step forward if hero was selected
                        if(cell == hero.GetParentCell()) {
                            MoveForward(true);

                        // Set selected cell as auto-target
                        } else {
                            SetTarget(cell, true);
                            MoveTarget(GetTargetCellId(), true);
                        }
                    }
                }

            // SWIPE_DOWN_TOOLBAR
            } else if(touch_evt == Event.SWIPE_DOWN_TOOLBAR) {
                MoveForward(false);

            // PRESSED_LONG
            } else if(touch_evt == Event.PRESSED_LONG) {
                // Select cell 
                LevelObjectCell cell = Main.inst.level.GetSelectedCell(
                  Gdx.input.getX(), Gdx.input.getY(), true, true);

                // Reset target
                SetTarget(null, false);

                // Target cell was selected
                if(cell != null) {
                    // Move one step forward if hero was selected
                    if(cell == hero.GetParentCell()) {
                        MoveForward(true);

                    // Set non-auto target cell, this will cause movement to stop
                    // when user stops holding the screen
                    } else {
                        SetTarget(cell, false);
                        MoveTarget(GetTargetCellId(), false);
                    }
                }

            // HOLD_LONG
            } else if(touch_evt == Event.HOLD_LONG) {
                // Continue moving forward if not target
                if(GetTargetCellId() == -1) {
                    MoveForward(true);

                // Continue moving towards target
                } else {
                    MoveTarget(GetTargetCellId(), false);
                }

            // NULL
            } else if(touch_evt == Event.NULL) {
                if(IsAutoMoving()) {
                    MoveTarget(GetTargetCellId(), true);
                }
            }
        }
        super.Process();
    }
}
