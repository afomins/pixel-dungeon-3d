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
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class GuiSelectBox
  extends VisSelectBox<String> {
    //**************************************************************************
    // GuiSelectBox
    //**************************************************************************
    private UtilsClass.IdNameMap m_id_name_map;
    private String[] m_names;

    //--------------------------------------------------------------------------
    public GuiSelectBox(UtilsClass.IdNameMap id_name_map) {
        m_id_name_map = id_name_map;
        setItems(id_name_map.names);
    }

    //--------------------------------------------------------------------------
    public GuiSelectBox(String names[]) {
        m_names = names;
        setItems(names);
    }

    //--------------------------------------------------------------------------
    public GuiSelectBox SetName(String name, boolean fire_event) {
        getSelection().setProgrammaticChangeEvents(fire_event);
        setSelected(name);
        getSelection().setProgrammaticChangeEvents(true);
        return this;
    }

    //--------------------------------------------------------------------------
    public String GetName() {
        return getSelected();
    }

    //--------------------------------------------------------------------------
    public GuiSelectBox SetId(long id, boolean fire_event) {
        return SetName((m_id_name_map != null) ? 
          m_id_name_map.get(id) : m_names[(int)id], fire_event);
    }

    //--------------------------------------------------------------------------
    public long GetId() {
        int idx = getSelectedIndex();
        return (m_id_name_map != null) ? m_id_name_map.ids[idx] : idx;
    }
}
