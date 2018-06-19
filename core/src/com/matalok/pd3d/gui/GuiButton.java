//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class GuiButton 
  extends Stack {
    //**************************************************************************
    // GuiButton
    //**************************************************************************
    private VisImageButton m_button;
    private GuiImage m_image;
    private VisLabel m_label_top_left, m_label_top_right, m_label_bottom_right;
    private VisLabel m_label_bottom_left, m_label_center;
    private GuiClickListener m_click_listener;

    //--------------------------------------------------------------------------
    public GuiButton(TextureRegion tx, String label, Float width, Float height, 
      Gui.TablePad img_pad, Gui.TablePad text_pad, final boolean draw_background,
      EventListener event_listener) {
        //
        // Button
        //
        m_button = new VisImageButton((tx == null) ? null : new TextureRegionDrawable(tx)) {
            @Override public void setBackground(Drawable background) {
                if(draw_background) {
                    super.setBackground(background);
                }
            }
        };
        m_button.setFocusBorderEnabled(false);
        m_button.align(Align.bottomLeft);
        m_button.getImage().setFillParent(true);
        m_button.getImage().setScaling(Scaling.stretch);
        if(event_listener != null) {
            m_button.addListener(event_listener);
            if(event_listener instanceof GuiClickListener) {
                m_click_listener = (GuiClickListener)event_listener;
            }
        }

        // Wrap button in table
        VisTable table = new VisTable();
        table.add(m_button).size(width, height);
        add(table);

        //
        // Image
        //
        m_image = new GuiImage();
        m_image.setTouchable(Touchable.disabled);
        table = new VisTable();
        table.add(m_image).expand().fill().align(Align.bottomLeft);
        if(img_pad != null) {
            img_pad.Apply(table); 
        }
        add(table);

        //
        // Top left label
        //
        m_label_top_left = new VisLabel(label);
        m_label_top_left.setTouchable(Touchable.disabled);
        table = new VisTable();
        table.add(m_label_top_left).expand().align(Align.topLeft);
        if(text_pad != null) {
            text_pad.Apply(table); 
        }
        add(table);

        //
        // Top right label
        //
        m_label_top_right = new VisLabel(label);
        m_label_top_right.setTouchable(Touchable.disabled);
        table = new VisTable();
        table.add(m_label_top_right).expand().align(Align.topRight);
        if(text_pad != null) {
            text_pad.Apply(table); 
        }
        add(table);

        //
        // Bottom right label
        //
        m_label_bottom_right = new VisLabel(label);
        m_label_bottom_right.setTouchable(Touchable.disabled);
        table = new VisTable();
        table.add(m_label_bottom_right).expand().align(Align.bottomRight);
        if(text_pad != null) {
            text_pad.Apply(table); 
        }
        add(table);

        //
        // Bottom left label
        //
        m_label_bottom_left = new VisLabel(label);
        m_label_bottom_left.setTouchable(Touchable.disabled);
        table = new VisTable();
        table.add(m_label_bottom_left).expand().align(Align.bottomLeft);
        if(text_pad != null) {
            text_pad.Apply(table); 
        }
        add(table);

        //
        // Center label
        //
        m_label_center = new VisLabel(label);
        m_label_center.setTouchable(Touchable.disabled);
        table = new VisTable();
        table.add(m_label_center).expand().align(Align.center);
        add(table);
    }

    //--------------------------------------------------------------------------
    public VisLabel SetTopLeftLabel(String label) {
        m_label_top_left.setText(label);
        return m_label_top_left; 
    }

    //--------------------------------------------------------------------------
    public VisLabel SetTopRightLabel(String label) {
        m_label_top_right.setText(label);
        return m_label_top_right;
    }

    //--------------------------------------------------------------------------
    public VisLabel SetBottomRightLabel(String label) {
        m_label_bottom_right.setText(label);
        return m_label_bottom_right;
    }

    //--------------------------------------------------------------------------
    public VisLabel SetBottomLeftLabel(String label) {
        m_label_bottom_left.setText(label);
        return m_label_bottom_left;
    }

    //--------------------------------------------------------------------------
    public VisLabel SetCenterLabel(String label) {
        m_label_center.setText(label);
        return m_label_center;
    }

    //--------------------------------------------------------------------------
    public void SetEnabled() {
        m_button.setDisabled(false);
        m_image.getColor().a = 1.0f;
    }

    //--------------------------------------------------------------------------
    public void SetDisabled(boolean is_faded) {
        m_button.setDisabled(true);
        m_image.getColor().a = (is_faded) ? 
          Main.inst.cfg.gui_disabled_object_alpha : 1.0f;
    }

    //--------------------------------------------------------------------------
    public VisImageButton GetButton() {
        return m_button;
    }

    //--------------------------------------------------------------------------
    public GuiImage GetImage() {
        return m_image;
    }

    //**************************************************************************
    // Actor
    //**************************************************************************
    @Override public void act(float delta) {
        super.act(delta);

        // Process click listener
        if(!m_button.isDisabled() && m_click_listener != null) {
            m_click_listener.Process();
        }
    }
}
