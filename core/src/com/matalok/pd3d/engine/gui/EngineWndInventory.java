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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescBag;
import com.matalok.pd3d.desc.DescStringInst;
import com.matalok.pd3d.desc.DescItem;
import com.matalok.pd3d.desc.DescQuest;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.gui.Gui;
import com.matalok.pd3d.gui.GuiButton;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.MsgQuestStart;
import com.matalok.pd3d.msg.MsgSelectInventoryItem;
import com.matalok.pd3d.msg.MsgSelectQuickslotItem;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class EngineWndInventory 
  extends EngineWnd {
    //**************************************************************************
    // ENUMS
    //**************************************************************************
    public enum SlotType {
        EQUIP_WEAPON,
        EQUIP_ARMOR,
        EQUIP_RING,
        STUFF,
        GOLD,
    }

    //**************************************************************************
    // EngineWndInventory
    //**************************************************************************
    private Cell<GuiButton> m_equip_weapon;
    private Cell<GuiButton> m_equip_armor;
    private Cell<GuiButton> m_gold;
    private LinkedList<Cell<GuiButton>> m_stuff;
    private LinkedList<Cell<GuiButton>> m_rings;

    private Iterator<Cell<GuiButton>> m_stuff_it;
    private Iterator<Cell<GuiButton>> m_rings_it;

    private Cell<VisTable> m_bags_table;
    private LinkedList<DescBag> m_bags;
    private DescBag m_cur_bag;
    private DescBag m_equipped_bag;

    private String m_inv_listener;
    private int m_quickslot_idx;
    private int m_gold_num;
    private String m_title;

    //--------------------------------------------------------------------------
    public EngineWndInventory() {
        super("inventory", true, true, 0.9f, 0.9f, 
          InputAction.IGNORE,       // OnTouchInArea
          InputAction.POP_STATE,    // OnTouchOutArea
          InputAction.POP_STATE,    // OnKeyPress
          InputAction.POP_STATE,    // OnBack
          0.9f, null, 0.0f);

        m_stuff = new LinkedList<Cell<GuiButton>>();
        m_rings = new LinkedList<Cell<GuiButton>>();
        m_bags = new LinkedList<DescBag>(); 
    }

    //--------------------------------------------------------------------------
    public EngineWndInventory Init(String title, String mode, String listener, 
      int quickslot_idx, int gold_num, LinkedList<DescBag> bags) {
        m_inv_listener = listener;
        m_quickslot_idx = quickslot_idx;
        m_gold_num = gold_num;
        m_bags = bags;
        m_title = title;
        SelectBag(0);
        return this;
    }

    //--------------------------------------------------------------------------
    public void SelectBag(int bag_idx) {
        // Select bag
        if(bag_idx < 0 || bag_idx >= m_bags.size()) {
            Logger.e("Failed to select inventory bag, wrong index :: idx=%d", bag_idx);
            return;
        }
        m_cur_bag = m_bags.get(bag_idx);
        SetTitle(m_title == null ? m_cur_bag.name : m_title, true);
    }

    //--------------------------------------------------------------------------
    private Cell<GuiButton> GetSlot(SlotType type) {
        try {
            switch(type) {
            case EQUIP_WEAPON:
                return m_equip_weapon;
            case EQUIP_ARMOR:
                return m_equip_armor;
            case EQUIP_RING:
                return m_rings_it.next();
            case STUFF:
                return m_stuff_it.next();
            case GOLD:
                return m_gold;
            }
        } catch(NoSuchElementException ex) {
            Utils.LogException(ex, 
              "Failed to get next cell from inventory :: type=%s", type);
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public void SetSlot(SlotType slot_type, final DescItem item, 
      EventListener event_listenter) {
        Cell<GuiButton> slot = GetSlot(slot_type);
        if(slot == null) {
            Logger.e("Failed to update inventory, no slot :: type=%s txt=%s", 
              slot_type, item.name);
            return;
        }

        // Update button
        GuiButton btn = slot.getActor();
        btn.GetImage().SetSprite(MapEnum.ItemType.Get(item.sprite_id));
        DescStringInst txt_desc = item.txt_top_left;
        if(txt_desc != null) {
            btn.SetTopLeftLabel(txt_desc.text)
              .getColor().set(txt_desc.color);
        }
        txt_desc = item.txt_top_right;
        if(txt_desc != null) {
            btn.SetTopRightLabel(txt_desc.text)
            .getColor().set(txt_desc.color);
        }
        txt_desc = item.txt_bottom_right;
        if(txt_desc != null) {
            btn.SetBottomRightLabel(txt_desc.text)
             .getColor().set(txt_desc.color);
        }
        txt_desc = item.txt_bottom_left;
        if(txt_desc != null) {
            btn.SetBottomLeftLabel(txt_desc.text)
             .getColor().set(txt_desc.color);
        }

        // Set button color
        Color btn_color = btn.GetButton().getColor();
        if(item.is_cursed && item.is_cursed_known) {
            btn_color.set(Main.inst.cfg.gui_item_cursed_color);
        } else if(!item.is_identified) {
            btn_color.set(Main.inst.cfg.gui_item_non_identified_color);
        } else {
            btn_color.set(Main.inst.cfg.gui_item_normal_color);
        }

        if(item.is_equipped) {
            btn_color.a *= btn_color.a * 0.7f;
        }

        // Enable selectable button
        if(item.is_selectable) {
            btn.SetEnabled();
            btn.addListener(event_listenter);

        // Disable non-selectable buttons
        } else {
            btn.SetDisabled((slot_type != SlotType.GOLD));
        }
    }

    //--------------------------------------------------------------------------
    private int GetColNum() {
        return Main.inst.gui.IsLandscape() ? 6 : 4;
    }

    //--------------------------------------------------------------------------
    private int GetRowNum() {
        return Main.inst.gui.IsLandscape() ? 4 : 6;
    }

    //--------------------------------------------------------------------------
    private float GetSlotSize() {
        return GetSquareSize(Main.inst.cfg.gui_inventory_icon_size);
    }

    //--------------------------------------------------------------------------
    private void CreateSlotGrid() {
        // Params
        Gui g = Main.inst.gui;
        int col_num = GetColNum();
        int row_num = GetRowNum();
        float slot_size = GetSlotSize();
        Gui.TablePad img_pad = Gui.TablePad.CreateAbs(g.GetSmallest(false, 0.03f));
        Gui.TablePad txt_pad = Gui.TablePad.CreateAbs(g.GetSmallest(false, 0.01f));

        // Reset multi-slots
        m_stuff.clear();
        m_rings.clear();

        int slot_num = col_num * row_num;
        for(int i = 0; i < slot_num; i++) {
            // New row
            if(i % col_num == 0 && i != 0) {
                row();
            }

            // Create slot button
            Cell<GuiButton> slot = 
              AddCellButtonOverlay(
                null, null, null, slot_size, slot_size, img_pad, txt_pad, true, null);

            // All slots are disabled by default
            slot.getActor().SetDisabled(false);

            // Stot #0 - equipped weapon
            if(i == 0) {
                m_equip_weapon = slot;

            // Stot #1 - equipped armor
            } else if(i == 1) {
                m_equip_armor = slot;

            // Stots #2-3 - equipped rings
            } else if(i == 2 || i == 3) {
                m_rings.add(slot);

            // Last slot - gold
            } else if(i == slot_num - 1) {
                m_gold = slot;

            // Rest slots - stuff
            } else {
                m_stuff.add(slot);
            }
        }

        // Reset multi-slot iterators
        m_stuff_it = m_stuff.iterator();
        m_rings_it = m_rings.iterator();
    }

    //--------------------------------------------------------------------------
    private void CreateBagButtons() {
        Gui g = Main.inst.gui;
        float bag_btn_size = GetSlotSize() * 0.6f;//r.GetRelSmallest(0.1f);
        Gui.TablePad img_pad = Gui.TablePad.CreateAbs(g.GetSmallest(false, 0.01f));

        m_equipped_bag = null;

        // Create table for bag buttons
        row();
        m_bags_table = AddCellTable(null, null, bag_btn_size, true)
          .colspan(GetColNum()).expand().fill().align(Align.left);

        // Create button for each bag
        int idx = 0;
        for(DescBag bag : m_bags) {
            // Ignore virtual bag of equipped items
            if(bag.name.equals("equipped")) {
                m_equipped_bag = bag;
                continue;
            }

            // Get sprite of the bag
            Enum<?> sprite = 
              (bag.name.equals("backpack")) ? MapEnum.IconType.BACKPACK :
              (bag.name.equals("key ring")) ? MapEnum.IconType.KEYRING :
              (bag.name.equals("scroll holder")) ? MapEnum.IconType.SCROLL_HOLDER :
              (bag.name.equals("wand holster")) ? MapEnum.IconType.WAND_HOLSTER :
              (bag.name.equals("seed pouch")) ? MapEnum.IconType.SEED_POUCH :
              MapEnum.IconType.WARNING;

            // Create button
            final int bag_idx = idx;
            GuiButton btn = AddCellButtonOverlay(m_bags_table, 
              null, null, bag_btn_size, bag_btn_size, img_pad, null, true,
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      SelectBag(bag_idx);
                      Rebuild();
              }}).align(Align.left).getActor();

            // Set button overlay image
            btn.GetImage().setDrawable(
              new TextureRegionDrawable(
                Main.inst.renderable_man.GetTextureRegion(sprite, null)));

            if(m_cur_bag == bag) {
                btn.GetButton().setColor(Main.inst.cfg.gui_btn_color);
                btn.SetDisabled(false);
            }
            idx++;
        }

        // Add filler after last bag 
        AddCellLabel(m_bags_table, null, null, null).expand().fill();
    }

    //--------------------------------------------------------------------------
    private void FillSlotGrid() {
        // Equip flags
        boolean is_weapon_equipped = false;
        boolean is_armor_equipped = false;
        boolean is_ring0_equipped = false;
        boolean is_ring1_equipped = false;

        EngineWnd wnd_item = Main.inst.engine.wnd_item;

        // Fill items in current&equipped bags
        DescBag bags[] = new DescBag[]{m_cur_bag, m_equipped_bag};
        for(DescBag bag : bags) {
            if(bag.items == null) {
                continue;
            }
            for(final DescItem item : bag.items) {
                // Run item action
                final int quick_idx = m_quickslot_idx;
                final int item_id = item.item_id;
                ClickListener listener = null;
                switch(m_inv_listener) {
                case "generic": {
                    // Parse actions
                    final HashMap<String, ClickListener> item_actions = 
                      Main.inst.level.GetHero().IsAlive() ? new HashMap<String, ClickListener>() : null;
                    if(item_actions != null) {
                        for(final String action : item.actions) {
                            // Click action of the item
                            ClickListener action_click = new ClickListener(wnd_item) {
                                @Override public void OnReleased() {
                                    // Pop from item & inventory states
                                    Main.inst.engine.PopState(2);

                                    // Run item action
                                    Main.inst.engine.GetGameScene(true)
                                      .RunItemAction(item, item_id, -1, action);
                            }};
                            item_actions.put(action, action_click);
                        }
                    }
    
                    // Item click
                    listener = new ClickListener(this) {
                        @Override public void OnReleased() {
                            // Push item state
                            Main.inst.engine.GetGameScene(true).PushState(
                              new SceneGame.StateShowItem(item, item_actions));
                    }};
                } break;
    
                // Select item for quickslot
                case "quickslot": {
                    listener = new ClickListener(this) {
                        @Override public void OnReleased() {
                            Main.inst.proxy_client.Send(
                              MsgSelectQuickslotItem.CreateRequest(quick_idx, item_id));
    
                              // Pop from inventory state
                              Main.inst.engine.PopState(1);
                     }};
                } break;

                // Sell item
                case "com.watabou.pixeldungeon.actors.mobs.npcs.shopkeeper$1": {
                    listener = new ClickListener(this) {
                        @Override public void OnReleased() {
                            MsgQuestStart msg = MsgQuestStart.CreateRequest();
                            msg.quest = new DescQuest();
                            msg.quest.need_response = true;
                            msg.quest.name = "sell-item";
                            msg.quest.target_item_id = item.item_id;
                            Main.inst.proxy_client.Send(msg);
                     }};
                } break;

                // Apply weightstone
                case "com.watabou.pixeldungeon.items.weightstone$1": {
                    listener = new ClickListener(this) {
                        @Override public void OnReleased() {
                            MsgQuestStart msg = MsgQuestStart.CreateRequest();
                            msg.quest = new DescQuest();
                            msg.quest.need_response = true;
                            msg.quest.name = "apply-weightstone";
                            msg.quest.target_item_id = item.item_id;
                            Main.inst.proxy_client.Send(msg);
                     }};
                } break;

                // Default item selection handler
                default: {
                    listener = new ClickListener(this) {
                        @Override public void OnReleased() {
                            Main.inst.proxy_client.Send(
                              MsgSelectInventoryItem.CreateRequest(item_id));

                            // Pop from inventory state
                            Main.inst.engine.PopState(1);
                     }};
                }}

                // Select inventory slot
                SlotType slot = EngineWndInventory.SlotType.STUFF;
                if(item.is_equipped) {
                    if(item.type.equals("weapon")) {
                        slot = EngineWndInventory.SlotType.EQUIP_WEAPON;
                        is_weapon_equipped = true;
                    } else if(item.type.equals("armor")) {
                        slot = EngineWndInventory.SlotType.EQUIP_ARMOR;
                        is_armor_equipped = true;
                    } else {
                        slot = EngineWndInventory.SlotType.EQUIP_RING;
                        if(!is_ring0_equipped) is_ring0_equipped = true;
                        else                   is_ring1_equipped = true;
                    }
                }
    
                // Add item to slot
                SetSlot(slot, item, listener);
            }
        }

        // Dummy item for non-equiped items
        DescItem dummy_item = new DescItem();
        dummy_item.is_cursed = false;
        dummy_item.is_cursed_known = false;
        dummy_item.is_equipped = false;
        dummy_item.is_level_known = true;
        dummy_item.is_selectable = false;
        dummy_item.is_unique = false;
        dummy_item.is_identified = true;

        // Empty ring slots
        if(!is_ring0_equipped) {
            for(int i = 0; i < (is_ring1_equipped ? 1 : 2); i++) {
                dummy_item.sprite_id = MapEnum.ItemType.RING.ordinal();
                SetSlot(EngineWndInventory.SlotType.EQUIP_RING, 
                  dummy_item, null);
            }
        }

        // Empty weapon slot
        if(!is_weapon_equipped) {
            dummy_item.sprite_id = MapEnum.ItemType.WEAPON.ordinal();
            SetSlot(EngineWndInventory.SlotType.EQUIP_WEAPON, 
              dummy_item, null);
        }

        // Empty armor slot
        if(!is_armor_equipped) {
            dummy_item.sprite_id = MapEnum.ItemType.ARMOR.ordinal();
            SetSlot(EngineWndInventory.SlotType.EQUIP_ARMOR, 
              dummy_item, null);
        }

        // Gold slot
        dummy_item.sprite_id = MapEnum.ItemType.GOLD.ordinal();
        dummy_item.txt_top_left = new DescStringInst();
        dummy_item.txt_top_left.text = Integer.toString(m_gold_num);
        dummy_item.txt_top_left.color = 0xffffffff;
        SetSlot(EngineWndInventory.SlotType.GOLD, 
          dummy_item, null);
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        Gui g = Main.inst.gui;
        float w = g.GetWidth(true, 1.0f);
        float slot_size = GetSlotSize();
        float col_num = GetColNum();
        SetFixedSize(((slot_size + slot_size / 10) * col_num) / w, 0.0f);

        CreateSlotGrid();
        CreateBagButtons();
        FillSlotGrid();
        return this;
    }
}
