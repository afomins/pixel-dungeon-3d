//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import com.matalok.pd3d.map.MapEnum;

//------------------------------------------------------------------------------
public class EngineWndInterLevel 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndInterLevel
    //**************************************************************************
    private String m_description;

    //--------------------------------------------------------------------------
    public EngineWndInterLevel() {
        super(null, false, false, 1.0f, 1.0f, 
          InputAction.CONSUME,  // OnTouchInArea
          InputAction.CONSUME,  // OnTouchOutArea
          InputAction.CONSUME,  // OnKeyPress
          InputAction.CONSUME,  // OnBack
          1.0f, MapEnum.TerrainType.EMPTY, 1.0f);
        setColor(0.5f, 0.5f, 0.5f, 1.0f);
        m_description = "...";
    }

    //--------------------------------------------------------------------------
    public void SetDescription(String text) {
        m_description = text;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        AddCellLabel(null, m_description, null, null);
        return this;
    }
}
