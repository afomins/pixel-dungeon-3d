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
