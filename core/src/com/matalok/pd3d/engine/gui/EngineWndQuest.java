//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import java.util.Map;

//------------------------------------------------------------------------------
public class EngineWndQuest 
  extends EngineWndInfo {
    //**************************************************************************
    // EngineWndQuest
    //**************************************************************************
    protected String m_title;
    protected String m_text;
    protected Enum<?> m_sprite; 
    protected Map<String, ClickListener> m_buttons;
    protected int m_btn_row_size;

    //--------------------------------------------------------------------------
    public EngineWndQuest() {
        super(false);
    }

    //--------------------------------------------------------------------------
    public EngineWndQuest Init(String title, String text, Enum<?> sprite, 
      Map<String, ClickListener> buttons, int btn_row_size) {
        m_title = title;
        m_text = text;
        m_sprite = sprite;
        m_buttons = buttons;
        m_btn_row_size = btn_row_size;
        return this;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        super.OnPostReset();

        Set(m_title, m_sprite, m_text, m_buttons, m_btn_row_size);
        return this;
    }
}
 