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
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescStringInst;
import com.matalok.pd3d.desc.DescHeroStats;
import com.matalok.pd3d.desc.DescItem;
import com.matalok.pd3d.engine.Scene;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.engine.input.EngineInputHeroMove;
import com.matalok.pd3d.gui.Gui;
import com.matalok.pd3d.gui.Gui.TablePad;
import com.matalok.pd3d.gui.GuiButton;
import com.matalok.pd3d.gui.GuiGameStatus;
import com.matalok.pd3d.gui.GuiImage;
import com.matalok.pd3d.gui.GuiLog;
import com.matalok.pd3d.gui.GuiMinimap;
import com.matalok.pd3d.gui.GuiProgressBar;
import com.matalok.pd3d.level.LevelTarget;
import com.matalok.pd3d.level.object.LevelObjectChar;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.MsgGetInventory;
import com.matalok.pd3d.msg.MsgHeroInteract;
import com.matalok.pd3d.msg.MsgSwitchScene;
import com.matalok.pd3d.renderable.RenderableMan;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class EngineWndGame 
  extends EngineWnd {
    //**************************************************************************
    // ENUMS
    //**************************************************************************
    public enum Buttons {
        INVENTORY, SLEEP, SEARCH, NEXT_TARGET, MOVE_FORWARD, 
        RESUME, QUICK0, QUICK1,
    };

    //**************************************************************************
    // EngineWndGame
    //**************************************************************************
    private Cell<GuiProgressBar> m_hp_progress, m_exp_progress;
    private Cell<GuiButton> m_hero;
    private Cell<GuiButton> m_inventory_cell;
    private Cell<GuiButton> m_sleep_cell;
    private Cell<GuiButton> m_search_cell;
    private Cell<GuiButton> m_next_target_cell;
    private Cell<GuiButton> m_resume_movement_cell;
    private Cell<GuiButton> m_move_forward_cell;
    private Cell<GuiButton> m_menu_cell;
    private Cell<GuiButton>[] m_quickslot_cells;
    private Cell<GuiMinimap> m_minimap_cell;
    private Cell<GuiGameStatus> m_busy_cell;
    private Cell<GuiLog> m_log_cell;
    private Cell<GuiImage>[] m_buff_cells;
    private Cell<VisLabel> m_fps_cells;
    private GuiButton[] m_buttons;
    private UtilsClass.PeriodicTask m_gui_update_task;

    //--------------------------------------------------------------------------
    public EngineWndGame() {
        super(null, false, false, 1.0f, 1.0f, 
          InputAction.IGNORE,   // OnTouchInArea
          InputAction.IGNORE,   // OnTouchOutArea
          InputAction.IGNORE,   // OnKeyPress
          InputAction.IGNORE,   // OnBack
          0.7f, MapEnum.ItemType.HIDDEN, 0.0f);

        defaults().space(0.0f, 0.0f, 0.0f, 0.0f);
    }

    //--------------------------------------------------------------------------
    public void UpdateMinimap() {
        if(m_minimap_cell == null) {
            return;
        }

        LevelObjectChar hero = Main.inst.level.GetHero();
        if(hero != null) {
            m_minimap_cell.getActor().Update(
              Main.inst.level.GetMinimap(), 
              90.0f - hero.GetDirection().GetAngle());
        }
    }

    //--------------------------------------------------------------------------
    public SceneGame GetGame() {
        return Main.inst.engine.GetGameScene(true);
    }

    //--------------------------------------------------------------------------
    public EngineInputHeroMove GetMove(SceneGame game) {
        return ((SceneGame.StateAlive)game.GetState()).move;
    }

    //--------------------------------------------------------------------------
    public void UpdateHero() {
        DescHeroStats stats = Main.inst.engine.GetGameScene(true).GetHeroStats();
        float hp_progress = (stats == null) ? 0.7f : (float)stats.hp / stats.ht;
        float exp_progress = (stats == null) ? 0.3f : (float)stats.exp / stats.exp_max;
        int level = (stats == null) ? 0 : stats.level;

        if(m_hp_progress != null) {
            m_hp_progress.getActor().Update(hp_progress);
        }
        if(m_exp_progress != null) {
            m_exp_progress.getActor().Update((exp_progress == 0.0f) ? 0.05f : exp_progress);
        }
        if(m_hero != null) {
            GuiButton btn = m_hero.getActor(); 
            btn.SetBottomRightLabel(Integer.toString(level));

            LevelObjectChar hero_obj = Main.inst.level.GetHero();
            if(stats != null && hero_obj != null) {
                btn.GetImage().SetSprite(
                  MapEnum.CharType.Get(hero_obj.GetMainObject().GetObjectId()))
                    .getColor().set(1.0f, hp_progress, hp_progress, 1.0f);
            }
        }

        // Dungeon depth
        m_menu_cell.getActor().SetBottomLeftLabel(
          Integer.toString(Main.inst.level.GetDungeonDepth()));

        UpdateBuffs();
    }

    //--------------------------------------------------------------------------
    public void UpdateSearchButton(Enum<?> sprite) {
        if(m_search_cell == null) {
            return;
        }

        if(sprite == null) {
            sprite = MapEnum.ToolbarType.SEARCH;
        }

        m_search_cell.getActor().GetImage().SetSprite(sprite);
    }

    //--------------------------------------------------------------------------
    public void UpdateTarget() {
        if(m_next_target_cell == null) {
            return;
        }

        // Get selected char
        LevelTarget target = Main.inst.level.GetTarget();
        LevelObjectChar selected_char = target.GetSelectedChar();

        // Show button
        GuiButton btn = m_next_target_cell.getActor();
        if(selected_char == null) {
            btn.setVisible(false);

        // Hide button
        } else {
            btn.setVisible(true);
            btn.GetImage().SetSprite(
              MapEnum.CharType.Get(
                selected_char.GetMainObject().GetObjectId()));
            btn.SetBottomRightLabel(target.GetDesc());
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateResumeMovement(boolean do_show) {
        if(m_resume_movement_cell == null) {
            return;
        }

        // Resume movement to target
        GuiButton btn = m_resume_movement_cell.getActor();
        btn.setVisible(do_show);
    }

    //--------------------------------------------------------------------------
    public void UpdateQuickSlots() {
        if(m_quickslot_cells == null) {
            return;
        }
        for(int i = 0; i < m_quickslot_cells.length; i++) {
            UpdateQuickSlot(i);
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateQuickSlot(int idx) {
        if(m_quickslot_cells == null) {
            return;
        }

        DescItem item = Main.inst.engine.GetGameScene(true).GetQuickSlotItem(idx);
        GuiButton btn = m_quickslot_cells[idx].getActor();

        btn.GetImage().SetSprite(
          (item == null) ? null : MapEnum.ItemType.Get(item.sprite_id));

        if(item != null) {
            DescStringInst txt_desc = item.txt_top_left;
            if(txt_desc != null) {
                btn.SetTopLeftLabel(txt_desc.text)
                  .getColor().set(txt_desc.color);
            } else {
                btn.SetTopLeftLabel("");
            }
        } else {
            btn.SetTopLeftLabel("");
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateBuffs() {
        LevelObjectChar hero = Main.inst.level.GetHero();
        if(hero == null) {
            return;
        }

        int idx = 0;
        if(hero.GetBuffs() != null) {
            for(Integer buff_id : hero.GetBuffs()) {
                GuiImage img = m_buff_cells[idx].getActor();
                img.setVisible(true);
                img.SetSprite(MapEnum.BuffType.Get(buff_id));

                if(++idx >= m_buff_cells.length) {
                    break;
                }
            }
        }

        for(int i = idx; i < m_buff_cells.length; i++) {
            m_buff_cells[i].getActor().setVisible(false);
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateBusyIndicator(boolean do_rotate) {
        if(m_busy_cell != null) {
            m_busy_cell.getActor().SetBusy(do_rotate);
        }
    }

    //--------------------------------------------------------------------------
    public void UpdateGameState(String text, int game_step) {
        if(m_busy_cell != null) {
            m_busy_cell.getActor().SetText(text, game_step);
        }
    }

    //--------------------------------------------------------------------------
    public void WriteLog(String txt) {
        if(m_log_cell == null) {
            return;
        }
        m_log_cell.getActor().Write(txt);
    }

    //--------------------------------------------------------------------------
    public boolean IsToolbarCoord(int x, int y) {
        if(m_inventory_cell == null) {
            return false;
        }

        // Get coordinates of inventory button
        Vector2 pos = m_inventory_cell.getActor()
          .localToStageCoordinates(new Vector2(0, m_inventory_cell.getActorHeight()));
        return (y <= pos.y);
    }

    //--------------------------------------------------------------------------
    private Cell<GuiButton> SetToolbarButton(Cell<VisTable> dest, Enum<?> back, 
      Enum<?> front, String label, float size, boolean do_expand, 
      Gui.TablePad img_pad, Gui.TablePad text_pad, boolean draw_background, 
      ClickListener listener) {
        RenderableMan rm = Main.inst.renderable_man;
        Cell<GuiButton> cell = AddCellButtonOverlay(dest,
          rm.GetTextureRegion(back, null), label,
          size, size, img_pad, text_pad, draw_background, listener).align(Align.right);
        cell.getActor().GetImage().SetSprite(front);

        if(do_expand) {
            cell.expandX();
        }
        return cell;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @SuppressWarnings("unchecked")
    @Override public EngineWnd OnPostReset() {
        //......................................................................
        // Param
        boolean is_real = false;
        Gui g = Main.inst.gui;
        float minimap_size = GetSquareSize(Main.inst.cfg.minimap_size);
        float log_width = g.GetWidth(is_real, 1.0f);
        float btn_size = GetIconSize();
        float busy_size = g.GetSmallest(is_real, 0.05f);
        float hero_btn_size = GetIconSize();
        float buff_btn_size = g.GetSmallest(is_real, 0.05f);
        float menu_btn_size = hero_btn_size * 0.63f;
        float buff_btn_offset = buff_btn_size * 0.2f;
        float progress_width = g.GetWidth(true, 1.0f) - (hero_btn_size + menu_btn_size);
        float progress_height = g.GetSmallest(is_real, 0.017f);
        int col_num = 2;

        TablePad txt_pad = Gui.TablePad.CreateRel(btn_size, 0.2f, 0.1f, 0.1f, 0.1f);
        TablePad img_pad = Gui.TablePad.CreateRel(btn_size, 0.1f);

        //.......................................................................
        // Status table
        Cell<VisTable> status_table = AddCellTable(null, 
          g.GetWidth(true, 1.0f), hero_btn_size, false).colspan(col_num).align(Align.top);

        // Hero button
        m_hero = SetToolbarButton(status_table,
          MapEnum.StatusPaneType.HERO, null, null, 
          hero_btn_size, false, 
          Gui.TablePad.CreateRel(hero_btn_size, 0.2f, 0.35f, 0.2f, 0.35f), 
          Gui.TablePad.CreateRel(hero_btn_size, 0.08f), false,
          new ClickListener(this) {
              @Override public void OnPressedLong() {
                  Main.inst.engine.wnd_debug.Toggle();
          }}).align(Align.topLeft);

        // Hero HP & XP progress bars
        Cell<VisTable> progress_table = 
          AddCellTable(status_table, null, null, false).expandX().align(Align.topLeft);
        m_exp_progress = AddCellProgressBar(progress_table, progress_width, progress_height, 
          Color.YELLOW, new Color(Color.YELLOW).mul(0.5f));
        progress_table.getActor().row();
        m_hp_progress = AddCellProgressBar(progress_table, progress_width, progress_height, 
          Color.GREEN, Color.RED);

        // Separator between progress and buffs
        progress_table.getActor().row();
        AddCellLabel(progress_table, "", null, buff_btn_offset);

        // Buff table
        progress_table.getActor().row();
        m_buff_cells = new Cell[(int)(progress_width / (buff_btn_size + buff_btn_offset))];
        Cell<VisTable> buff_table = 
          AddCellTable(progress_table, progress_width, buff_btn_size, false);
        for(int i = 0; i < m_buff_cells.length; i++) {
            m_buff_cells[i] = AddCellImage(buff_table, 
              MapEnum.BuffType.Get(i), buff_btn_size, buff_btn_size).align(Align.left);
            AddCellLabel(buff_table, "", buff_btn_offset, null);
        }
        AddCellLabel(buff_table, "", null, null).expandX();

        // Fps
        progress_table.getActor().row();
        m_fps_cells = AddCellLabel(progress_table, "fps", null, null).align(Align.right);

        // Menu button
        m_menu_cell = SetToolbarButton(status_table,
          MapEnum.StatusPaneType.MENU, null, null, 
          menu_btn_size, false, null, 
          Gui.TablePad.CreateRel(hero_btn_size, 0.12f), false, 
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.engine
                    .PushState(new Scene.StateShowMainMenu());
          }}).align(Align.topRight);

        //.......................................................................
        // Middle table
        row();
        Cell<VisTable> middle_table = AddCellTable(null, 
          null, g.GetHeight(true, 1.0f) - hero_btn_size - btn_size, false).align(Align.left);

        // Busy indicator
        m_busy_cell = middle_table.getActor().add(
          new GuiGameStatus(MapEnum.IconType.BUSY, busy_size)).align(Align.left);

        // Minimap
        middle_table.getActor().row();
        m_minimap_cell = AddCellMinimap(middle_table, minimap_size, minimap_size)
          .align(Align.topLeft).colspan(2);
        m_minimap_cell.getActor().toBack();
        UpdateMinimap();

        // Separator
        middle_table.getActor().row();
        AddCellLabel(middle_table, "", null, null).fill().expand().colspan(2);

        // Log
        middle_table.getActor().row();
        m_log_cell = AddCellLog(middle_table, 4, log_width - btn_size, null)
          .align(Align.bottomLeft).colspan(2);

        //.......................................................................
        // Right table
        Cell<VisTable> right_table = AddCellTable(null, btn_size, null, false)
          .align(g.IsLandscape() ? Align.bottomRight : Align.right).expand();

        // Resume movement button
        m_resume_movement_cell = SetToolbarButton(right_table,
          MapEnum.ToolbarType.INFO, MapEnum.IconType.RESUME, null,
          btn_size, false, 
          Gui.TablePad.CreateRel(btn_size, 0.20f), txt_pad, true,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  // Alive
                  SceneGame game = GetGame();
                  if(game.GetStateType() == SceneGame.StateAlive.class) {
                      GetMove(game).ResumeTarget();

                  // Dead
                  } else if(game.GetStateType() == SceneGame.StateDead.class) {
                      Main.inst.proxy_client.Send(
                        MsgSwitchScene.CreateRequest("scene-start"));
                  }
          }}).align(Align.bottom);

        // Next target button
        right_table.getActor().row();
        m_next_target_cell = SetToolbarButton(right_table,
          MapEnum.ToolbarType.INFO, null, null,
          btn_size, false, 
          Gui.TablePad.CreateRel(btn_size, 0.15f), txt_pad, true,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.level.SelectNextTarget();
                  UpdateTarget();
          }}).align(Align.bottom);

        // Move forward button
        right_table.getActor().row();
        m_move_forward_cell = SetToolbarButton(right_table,
          MapEnum.ToolbarType.INFO, MapEnum.IconType.COMPASS, null,
          btn_size, false, 
          Gui.TablePad.CreateRel(btn_size, 0.25f), txt_pad, true,
          new ClickListener(this) {
              @Override public void OnPressed() {
                  SceneGame game = GetGame();
                  if(game.GetStateType() == SceneGame.StateAlive.class) {
                      GetMove(game).MoveForward(true);
                  }
              }
              @Override public void OnPressedLong() {
                  SceneGame game = GetGame();
                  if(game.GetStateType() == SceneGame.StateAlive.class) {
                      GetMove(game).VirtualKeyDown(1);
                  }
              }
              @Override public void OnReleasedLong() {
                  SceneGame game = Main.inst.engine.GetGameScene(false);
                  if(game != null && game.GetStateType() == SceneGame.StateAlive.class) {
                      GetMove(game).VirtualKeyUp(1);
                  }
          }}).align(Align.bottom);

        //......................................................................
        // Bottom table
        row();
        Cell<VisTable> bottom_table = AddCellTable(null, 
          g.GetWidth(true, 1.0f), btn_size, false).colspan(col_num).align(Align.bottom);

        // Quickslots buttons
        m_quickslot_cells = new Cell[2];
        for(int i = 0; i < m_quickslot_cells.length; i++) {
            final int slot_idx = i;
            m_quickslot_cells[i] = 
              SetToolbarButton(bottom_table, MapEnum.ToolbarType.QUICK, null, null, btn_size, 
                (i == 0), 
                Gui.TablePad.CreateRel(btn_size, 0.15f), txt_pad, true,
                new ClickListener(this) {
                    @Override public void OnPressedLong() {
                        SceneGame game = Main.inst.engine.GetGameScene(true);
                        if(game.GetStateType() == SceneGame.StateAlive.class) {
                            Main.inst.proxy_client.Send(
                              MsgGetInventory.CreateRequest(null, "quickslot", "quickslot", slot_idx));
                        }
                    }
                    @Override public void OnReleased() {
                        // Throw at selected target
                        SceneGame game = Main.inst.engine.GetGameScene(true);
                        if(game.GetStateType() == SceneGame.StateThrowItem.class) {
                            game.ThrowAtSelection();

                        // Run default action for item in quickslot
                        } else {
                            DescItem item = game.GetQuickSlotItem(slot_idx);
                            if(item != null) {
                                int quickslot_item_idx = -slot_idx - 1;
                                game.RunItemAction(item, quickslot_item_idx, slot_idx, null);
                            }
                        }
                }});
        }

        //......................................................................
        // Inventory button
        m_inventory_cell = SetToolbarButton(bottom_table, 
          MapEnum.ToolbarType.INFO, MapEnum.ToolbarType.INVENTORY, 
          null, btn_size, false, 
          img_pad, null, true,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.proxy_client.Send(
                    MsgGetInventory.CreateRequest(null, "all", "generic", null));
          }});

        // Sleep button
        m_sleep_cell = SetToolbarButton(bottom_table,
          MapEnum.ToolbarType.INFO, MapEnum.ToolbarType.WAIT, null, 
          btn_size, false, 
          img_pad, null, true,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.proxy_client.Send(
                    MsgHeroInteract.CreateRequest("rest", null));
              }
              @Override public void OnPressedLong() {
                  Main.inst.proxy_client.Send(
                    MsgHeroInteract.CreateRequest("rest-till-healthy", null));
              }
          });

        //......................................................................
        // Search button
        m_search_cell = SetToolbarButton(bottom_table,
          MapEnum.ToolbarType.INFO, null, null, 
          btn_size, false, img_pad, null, true,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  // Alive
                  SceneGame game = Main.inst.engine.GetGameScene(true);
                  if(game.GetStateType() == SceneGame.StateAlive.class) {
                      Main.inst.proxy_client.Send(
                        MsgHeroInteract.CreateRequest("search", null));

                  // Return from throw-state 
                  } else if(game.GetStateType() == SceneGame.StateThrowItem.class) {
                      game.PopState();
                  }
          }});

        // Bring status table to the front
        status_table.getActor().toFront();

        m_buttons = new GuiButton[] { 
          m_inventory_cell.getActor(), m_sleep_cell.getActor(), 
          m_search_cell.getActor(), m_next_target_cell.getActor(),  
          m_move_forward_cell.getActor(), m_resume_movement_cell.getActor(),
          m_quickslot_cells[0].getActor(), m_quickslot_cells[1].getActor()};
        return this;
    }

    //--------------------------------------------------------------------------
    public GuiButton GetButton(Buttons id) {
        return m_buttons[id.ordinal()];
    }

    //--------------------------------------------------------------------------
    public void DisableButtons(Buttons... buttons) {
        for(Buttons b : (buttons.length == 0) ? Buttons.values() : buttons) {
            GetButton(b).SetDisabled(true);
        }
    }

    //--------------------------------------------------------------------------
    public void EnableButtons(Buttons... buttons) {
        for(Buttons b : (buttons.length == 0) ? Buttons.values() : buttons) {
            GetButton(b).SetEnabled();
        }
    }

    //**************************************************************************
    // Actor
    //**************************************************************************
    @Override public void act(float delta) {
        super.act(delta);

        long cur_time = Main.inst.timer.GetCur();
        if(m_gui_update_task == null) {
            m_gui_update_task = new UtilsClass.PeriodicTask(
              cur_time, Utils.SecToMsec(0.3f), 
                new UtilsClass.Callback() {
                    @Override public Object Run(Object... args) {
                        int fps = Main.inst.renderer.GetFps();
                        int model_num = Main.inst.renderer.GetModelNum(false);
                        int model_num_visible = Main.inst.renderer.GetModelNum(true);
                        int particle_num = Main.inst.renderer.GetParticleNum(false);
                        int particle_num_visible = Main.inst.renderer.GetParticleNum(true);
                        int billboard_num = Main.inst.renderer.GetBillboardNum(false);
                        int billboard_num_visible = Main.inst.renderer.GetBillboardNum(true);
                        m_fps_cells.getActor().setText(
                          String.format("fps=%d m=%d/%d p=%d/%d b=%d/%d", 
                            fps, model_num_visible, model_num,
                            particle_num_visible, particle_num,
                            billboard_num_visible, billboard_num));

                        SceneGame game = Main.inst.engine.GetGameScene(false);
                        if(game != null) {
                            DescHeroStats stats = game.GetHeroStats();
                            String status_text = "ready";
                            int status_step = 0;
                            if(stats != null) {
                                status_text = stats.status;
                                status_step = Math.round(stats.time_global);
                            }
                            m_busy_cell.getActor().SetText(status_text, status_step);
                        }
                        return null;
            }});
        }
        m_gui_update_task.Run(cur_time);
    }
}
