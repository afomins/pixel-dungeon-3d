//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Scheduler;
import com.matalok.pd3d.desc.DescEvent;
import com.matalok.pd3d.desc.DescPfxMutator;
import com.matalok.pd3d.gui.GuiAttrib;
import com.matalok.pd3d.gui.GuiAttribBlending;
import com.matalok.pd3d.gui.GuiAttribColor;
import com.matalok.pd3d.gui.GuiAttribDirLight;
import com.matalok.pd3d.gui.GuiAttribFloat;
import com.matalok.pd3d.gui.GuiAttribInt;
import com.matalok.pd3d.gui.GuiAttribTexture;
import com.matalok.pd3d.gui.GuiSpinnerFloat;
import com.matalok.pd3d.gui.GuiSpinnerVector3;
import com.matalok.pd3d.level.LevelCamera;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.MsgCommand;
import com.matalok.pd3d.renderer.RendererAttrib;
import com.matalok.pd3d.renderer.RendererAttribStack;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class EngineWndDebug 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndDebug
    //**************************************************************************
    private HashSet<String> m_show_group;
    private String m_save_name;
    private VisSelectBox<String> m_savegame_names;
    private VisTextField m_savegame_new_name;

    //--------------------------------------------------------------------------
    public EngineWndDebug() {
        super("DEBUG", false, false, 0.0f, 0.0f, 
          InputAction.CONSUME,  // OnTouchInArea
          InputAction.IGNORE,   // OnTouchOutArea
          InputAction.IGNORE,   // OnKeyPress
          InputAction.IGNORE,   // OnBack
          0.7f, null, 0.0f);
        m_show_group = new HashSet<String>();
    }

    //--------------------------------------------------------------------------
    private Cell<VisTable> AddGroup(final String name) {
        final boolean show_group = m_show_group.contains(name);
        String text = "----[ " + name.toUpperCase() + " ]---- " + (show_group ? "[X]" : "[.]");

        // Create group selection button
        row();
        AddCellButton(null, text, null, null,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  if(show_group) {
                      m_show_group.remove(name);
                  } else {
                      m_show_group.add(name);
                  }
                  Rebuild();
          }}).expandX().fillX();

        // Return group table
        if(!show_group) {
            return null;
        } else {
            row();
            return AddCellTable(null, null, null, true)
              .expandX().fillX();
        }
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        float pref_width = Main.inst.gui.GetSmallest(false, 1.0f);
        float width = Main.inst.gui.GetWidth(true);
        SetFixedSize(pref_width / width, 0.0f);

        //......................................................................
        // Scene graph
        Cell<VisTable> group = AddGroup("Scene graph");
        if(group != null) {
            // Log scene
            AddCellButton(group, "Log", null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.scheduler.ScheduleEvent(
                        Scheduler.Event.LOG_SCENE_GRAPH);
              }}).expandX().fillX();

            // Write snapshot
            AddCellButton(group, "Write snapshot", null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.scheduler.ScheduleEvent(
                        Scheduler.Event.SAVE_SNAPSHOT);
              }}).expandX().fillX();

            // Read snapshot
            AddCellButton(group, "Read snapshot", null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.scheduler.ScheduleEvent(
                        Scheduler.Event.LOAD_NEXT_SNAPSHOT);
              }}).expandX().fillX();
        }

        //......................................................................
        // Commands
        group = AddGroup("Commands");
        if(group != null) {
            // Toggle god mode
            String text = "iddqd: " + (Main.inst.cfg.cmd_iddqd? "[X]" : "[ ]");
            AddCellButton(group, text, null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      MsgCommand msg = MsgCommand.CreateRequest();
                      msg.iddqd = !Main.inst.cfg.cmd_iddqd;
                      Main.inst.proxy_client.Send(msg);
              }}).expandX().fillX();

            // Toggle extended info
            text = "item-info-ext: " + (Main.inst.cfg.cmd_item_info_ext? "[X]" : "[ ]");
            AddCellButton(group, text, null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      MsgCommand msg = MsgCommand.CreateRequest();
                      msg.item_info_ext = !Main.inst.cfg.cmd_item_info_ext;
                      Main.inst.proxy_client.Send(msg);
              }}).expandX().fillX();
        }

        //......................................................................
        // Tiles
        group = AddGroup("Tiles");
        if(group != null) {
            // Toggle tile packing
            String text = "Pack: " + 
              (Main.inst.cfg.lvl_pack ? "[X]" : "[ ]");
            AddCellButton(group, text, null, null,
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.cfg.lvl_pack = 
                        !Main.inst.cfg.lvl_pack;
                      Main.inst.SaveConfig();
                      Main.inst.level.UpdatePackedTileSprite();
                      Rebuild();
             }}).expandX().fillX();

            //......................................................................
            // Toggle tile coloring
            text = "Color: " + 
              (Main.inst.cfg.lvl_pack_color ? "[X]" : "[ ]");
            AddCellButton(group, text, null, null,
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.cfg.lvl_pack_color = 
                        !Main.inst.cfg.lvl_pack_color;
                      Main.inst.SaveConfig();
                      Main.inst.level.UpdatePackedTileSprite();
                      Rebuild();
              }}).expandX().fillX();
        }

        //......................................................................
        // Camera
        group = AddGroup("Camera");
        if(group != null) {
            final LevelCamera camera = Main.inst.level_camera;
            final LevelCamera.Param param = Main.inst.level_camera.GetParam();

            // Distance
            Cell<VisTable> table = AddCellTable(group, null, null, false)
             .expandX().fillX();
            AddCellSpinnerFloat(table, null, null, "dist", 
              param.distance, 0.02f, 
              new GuiSpinnerFloat.Listener() {
                  @Override public void OnValueChanged(float value) {
                      param.distance = value;
                      camera.SwitchCamera(0);
                  };
            });

            // Vertical angle
            AddCellSpinnerFloat(table, null, null, "v-angle", 
              param.angle_v, 1.0f, 
              new GuiSpinnerFloat.Listener() {
                  @Override public void OnValueChanged(float value) {
                      param.angle_v = value;
                      camera.SwitchCamera(0);
                  };
            });

            // Fov
            AddCellSpinnerFloat(table, null, null, "fov", 
              param.fov, 1.0f, 
              new GuiSpinnerFloat.Listener() {
                  @Override public void OnValueChanged(float value) {
                      param.fov = value;
                      camera.SwitchCamera(0);
                  };
            });

            // Position
            group.getActor().row();
            AddCellSpinnerVector3(group, null, null, "pos ", 
              param.pos, 0.1f, 
              new GuiSpinnerVector3.Listener() {
                  @Override public void OnValueChanged() {
                      camera.SwitchCamera(0);
                  };
            }).expandX().fillX();

            // Look at
            group.getActor().row();
            AddCellSpinnerVector3(group, null, null, "look-at ", 
              param.look_at, 0.1f, 
              new GuiSpinnerVector3.Listener() {
                  @Override public void OnValueChanged() {
                      camera.SwitchCamera(0);
                  };
            }).expandX().fillX();
        }

        //......................................................................
        // Renderer
        group = AddGroup("Renderer");
        if(group != null) {
            // Select attribute stacks
            AddCellLabel(group, "Attributes:", null, null);
            final VisSelectBox<String> stacks = AddCellSelectBox(group, 
              Main.inst.cfg.renderer_attrib_stacks.keySet().toArray(new String[]{}), 
              null, null, null, null).expandX().fillX().getActor();

            // Create new attribute
            String[] create_new = new String[RendererAttrib.type_array.length + 1];
            create_new[0] = "Create new";
            for(int i = 0; i < RendererAttrib.type_array.length; i++) {
                create_new[i + 1] = RendererAttrib.type_array[i].toString();
            }
            final VisSelectBox<String> new_attrib = AddCellSelectBox(group, create_new, 
              null, null, null, null).expandX().fillX().getActor();

            // Attributes
            group.getActor().row();
            final VisTable attrib_table = AddCellTable(group, null, null, false)
              .expandX().fillX().colspan(3).getActor();
            final ChangeListener update_cb = new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    // Clear attributes
                    attrib_table.clear();

                    RendererAttribStack.Cfg attrib_stack_cfg = 
                      Main.inst.cfg.renderer_attrib_stacks.get(stacks.getSelected());

                    for(RendererAttrib.Type atrib_type : RendererAttrib.Type.values()) {
                        @SuppressWarnings("unchecked")
                        ArrayList<? extends RendererAttrib.Cfg> attribs = 
                          attrib_stack_cfg.attribs[atrib_type.ordinal()];

                        // Refresh GUI
                        GuiAttrib.Listener refresh_gui = 
                          new GuiAttrib.Listener(atrib_type, attrib_stack_cfg) {
                            @Override public void OnResetAll() {
                                changed(null, null); // Reset self
                            }
                        };

                        for(int i = 0; i < attribs.size(); i++) {
                            GuiAttrib gui_attrib = null;
                            switch(atrib_type) {
                            //..................................................
                            case COLOR: {
                                gui_attrib = new GuiAttribColor(
                                  (RendererAttrib.AColor.Cfg)attribs.get(i), refresh_gui);
                            } break;

                            //..................................................
                            case DIR_LIGHT: {
                                gui_attrib = new GuiAttribDirLight(
                                  (RendererAttrib.ADirLight.Cfg)attribs.get(i), refresh_gui);
                            } break;

                            //..................................................
                            case TEXTURE: {
                                gui_attrib = new GuiAttribTexture(
                                  (RendererAttrib.ATexture.Cfg)attribs.get(i), refresh_gui);
                            } break;

                            //..................................................
                            case INT: {
                                gui_attrib = new GuiAttribInt(
                                  (RendererAttrib.AInt.Cfg)attribs.get(i), refresh_gui);
                            } break;

                            //..................................................
                            case FLOAT: {
                                attrib_table.add(
                                  new GuiAttribFloat((RendererAttrib.AFloat.Cfg)attribs.get(i), 
                                    refresh_gui)).expandX().fillX();
                            } break;

                            //..................................................
                            case BLENDING: {
                                gui_attrib = new GuiAttribBlending(
                                  (RendererAttrib.ABlending.Cfg)attribs.get(i), refresh_gui);
                            } break; }

                            if(gui_attrib != null) {
                                attrib_table.add(gui_attrib)
                                  .expandX().fillX();
                            }
                            attrib_table.row();
                        }
                    }
                    pack();
                }
            };

            // New attribute listener
            new_attrib.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    int idx = new_attrib.getSelectedIndex();
                    if(idx == 0) {
                        return;
                    }
                    RendererAttrib.Type t = RendererAttrib.type_array[idx - 1];
                    RendererAttribStack.Cfg attrib_stack_cfg = 
                      Main.inst.cfg.renderer_attrib_stacks.get(stacks.getSelected());
                    attrib_stack_cfg.AddAttrib(t, t.CreateAttribCfg());
                    new_attrib.setSelectedIndex(0);
                    update_cb.changed(null, null);
            }});

            stacks.addListener(update_cb);
            update_cb.changed(null, null);
        }

        //......................................................................
        // Pfx
        group = AddGroup("Event");
        if(group != null) {
            // 1st row
            final VisSelectBox<String> events = AddCellSelectBox(group, 
              Utils.GetEnumNames(MapEnum.EventType.values()), null, null, null, null)
              .getActor();
            final VisSelectBox<String> images = AddCellSelectBox(group, 
              Utils.GetEnumNames(MapEnum.PfxImage.values()), null, null, null, null)
              .colspan(2).expandX().fillX().getActor();

            // 2nd row
            group.getActor().row();
            final VisTextField pos = AddCellTextField(group, null, null, "42").getActor();
            ClickListener ger_hero_pos_cb = new ClickListener(this) {
                @Override public void OnReleased() {
                    pos.setText(Integer.toString(
                      Main.inst.level.GetHero().GetParentCell().GetPdId()));
            }};
            ger_hero_pos_cb.OnReleased();

            AddCellButton(group, "hero pos", null, null, ger_hero_pos_cb)
              .expandX().fillX();
            AddCellButton(group, "run", null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.level.HandleEvent(new DescEvent()
                        .SetEventId(MapEnum.EventType.Get(events.getSelectedIndex()))
                        .SetPfxMutator(DescPfxMutator.Field.IMAGE_ID, images.getSelectedIndex())
                        .SetCellId(Utils.StrToInt(pos.getText())));
              }}).expandX().fillX();
        }

        //......................................................................
        // Save game
        group = AddGroup("SAVE/LOAD");
        if(group != null) {
            final UtilsClass.Callback refresh_cb = new UtilsClass.Callback() {
                private int depth = 0;
                @Override public Object Run(Object... args) {
                    boolean preserve_old_selection = (Boolean)args[0];
                    depth++;

                    // Update names
                    String names[] = Main.inst.save_game.Refresh();
                    if(names.length != m_savegame_names.getItems().size) {
                        // This will trigger recursive call to refresh_cb()
                        if(names.length == 0) {
                            m_savegame_names.clearItems();
                        } else {
                            m_savegame_names.setItems(names);
                        }
                    }

                    // Do not update name when running recursively
                    if(depth == 1) {
                        if(preserve_old_selection) {
                            if(m_save_name != null && 
                              m_savegame_names.getItems().contains(m_save_name, false)) {
                                m_savegame_names.setSelected(m_save_name);
                            }
                        }

                        // Update currently selected name
                        m_save_name = m_savegame_names.getSelected();
                    }
                    depth--;
                    return null;
                }
            };

            // Quick save button
            final String cur_hero = Main.inst.engine.GetCurHero();
            AddCellButton(group, "quick save", null, null, 
              new ClickListener(this) {
                @Override public void OnReleased() {
                    Main.inst.save_game.SaveStart(cur_hero, "__quick-save");
                    Close(false);
                }
            }).expandX().fillX();

            // Quick load button
            AddCellButton(group, "quick-load", null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.save_game.Load("__quick-save");
                      Close(false);
                  }
            }).colspan(2).expandX().fillX();

            // Names select-box
            group.getActor().row();
            m_savegame_names = AddCellSelectBox(group, 
              null, null, null, null,
              new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    refresh_cb.Run(false);
                }
            }).expandX().fillX().getActor();

            // Load button
            AddCellButton(group, "load", null, null, 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.save_game.Load(m_save_name);
                      Close(false);
                  }
            });

            // Delete button
            AddCellButton(group, "delete", null, null, 
              new ClickListener(this) {
                @Override public void OnReleased() {
                    Main.inst.save_game.Delete(m_save_name);
                    refresh_cb.Run(false);
                }
            });

            // New name text-box
            group.getActor().row();
            m_savegame_new_name = AddCellTextField(group, null, null, cur_hero + "-xxx")
              .expandX().fillX().getActor();

            // Copy button
            AddCellButton(group, "copy", null, null, 
              new ClickListener(this) {
                @Override public void OnReleased() {
                    if(m_save_name != null) {
                        m_savegame_new_name.setText(m_save_name);
                    }
                }
            });

            // Save button
            AddCellButton(group, "save", null, null, 
              new ClickListener(this) {
                @Override public void OnReleased() {
                    Main.inst.save_game.SaveStart(cur_hero, 
                      m_savegame_new_name.getText());
                    Close(false);
                }
            });
            refresh_cb.Run(true);
        }
        return this;
    }
}
