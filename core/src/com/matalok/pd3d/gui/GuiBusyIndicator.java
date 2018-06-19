//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class GuiBusyIndicator 
  extends VisTable {
    //--------------------------------------------------------------------------
    private Cell<VisImage> m_image_cell;
    private long m_stop_rotate_time;

    //**************************************************************************
    // GuiBusyIndicator
    //**************************************************************************
    public GuiBusyIndicator(TextureRegion tx_region) {
        VisImage image = new VisImage(new TextureRegionDrawable(tx_region));
        m_image_cell = add(image).expand().fill();
    }

    //--------------------------------------------------------------------------
    public void SetRotation(boolean do_rotate) {
        m_stop_rotate_time = (do_rotate) ? Long.MAX_VALUE : 
          Main.inst.timer.GetCur() + 
          Utils.SecToMsec(Main.inst.cfg.gui_busy_rot_after_stop);
    }

    //--------------------------------------------------------------------------
    public VisImage GetImage() {
        return m_image_cell.getActor();
    }

    //**************************************************************************
    // Actor
    //**************************************************************************
    @Override public void act(float delta) {
        super.act(delta);

        if(Main.inst.timer.GetCur() < m_stop_rotate_time) {
            VisImage img = m_image_cell.getActor();
            img.setOrigin(getWidth() / 2, getHeight() / 2);

            float angle = delta * Main.inst.cfg.gui_busy_rot_speed;
            m_image_cell.getActor().rotateBy(angle);
        }
    }
}
