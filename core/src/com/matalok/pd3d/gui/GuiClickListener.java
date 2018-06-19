//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class GuiClickListener 
  extends ClickListener {
    //--------------------------------------------------------------------------
    private long m_long_press_time;
    private boolean m_is_long_press_processed;

    //**************************************************************************
    // GuiClickListener
    //**************************************************************************
    public void OnPressed(boolean is_long) {
    }

    //--------------------------------------------------------------------------
    public void OnReleased(boolean is_long) {
    }

    //--------------------------------------------------------------------------
    public boolean IsLongPress() {
        return (m_long_press_time > 0 && 
          Main.inst.timer.GetCur() >= m_long_press_time);
    }

    //--------------------------------------------------------------------------
    public void Process() {
        // Detect long press 
        if(!m_is_long_press_processed && IsLongPress()) {
            OnPressed(true);
            m_is_long_press_processed = true;
        }
    }

    //**************************************************************************
    // ClickListener
    //**************************************************************************
    @Override public boolean touchDown(InputEvent event, float x, float y, 
      int pointer, int button) {
        boolean rc = super.touchDown(event, x, y, pointer, button);
        if(rc) {
            OnPressed(false);
            m_is_long_press_processed = false;
            m_long_press_time = Main.inst.timer.GetCur() + 
              Utils.SecToMsec(Main.inst.cfg.gui_long_click_duration);
        }
        return rc;
    }

    //--------------------------------------------------------------------------
    @Override public void clicked(InputEvent event, float x, float y) {
        OnReleased(IsLongPress());
        m_long_press_time = -1;
    }
}
