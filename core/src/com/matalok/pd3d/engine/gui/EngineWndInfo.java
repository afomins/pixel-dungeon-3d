//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import java.util.Map;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.gui.GuiImage;
import com.matalok.pd3d.gui.GuiTextBox;
import com.matalok.pd3d.map.MapEnum;

//------------------------------------------------------------------------------
public class EngineWndInfo 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndInfo
    //**************************************************************************
    protected Cell<VisTable> m_header;
    protected Cell<GuiImage> m_header_icon;
    protected Cell<VisTable> m_header_title;
    protected Cell<GuiTextBox> m_text;
    protected Cell<VisTable> m_button_bar;

    //--------------------------------------------------------------------------
    public EngineWndInfo(boolean need_title) {
        super(need_title ? "info" : null, true, true, 0.8f, 0.5f, 
          InputAction.IGNORE,       // OnTouchInArea
          InputAction.POP_STATE,    // OnTouchOutArea
          InputAction.POP_STATE,    // OnKeyPress
          InputAction.POP_STATE,    // OnBack
          0.95f, null, 0.0f);
    }

    //--------------------------------------------------------------------------
    public Cell<VisTextButton> AddButton(String title, ClickListener listener) {
        float btn_size = GetButtonSize();
        Color btn_color = Main.inst.cfg.gui_btn_color;

        boolean is_disabled = false;
        if(title.length() > 3 && title.substring(0, 3).equals("!!!")) {
            title = title.substring(3);
            is_disabled = true;
        }

        Cell<VisTextButton> btn = 
          AddCellButton(m_button_bar, title, null, btn_size, listener);
        btn.expandX().fillX().getActor().setColor(btn_color);
        btn.getActor().setDisabled(is_disabled);
        btn.uniform();
        return btn;
    }

    //--------------------------------------------------------------------------
    public void AddButtonRow() {
        m_button_bar.getActor().row();
    }

    //--------------------------------------------------------------------------
    public void SetSprite(Enum<?> sprite) {
        m_header_icon.getActor().SetSprite(sprite);
    }

    //--------------------------------------------------------------------------
    public void SetText(String str) {
        m_text.getActor().SetText(str);
        Main.inst.gui.SetScrollFocus(m_text.getActor());
    }

    //--------------------------------------------------------------------------
    public void SetButtons(Map<String, ClickListener> buttons, int button_row_size) {
        int btn_num = 0;
        Cell<VisTextButton> btn = null;
        for(Map.Entry<String, ClickListener> entry : buttons.entrySet()) {
            if(btn_num++ == button_row_size) {
                AddButtonRow();
                btn_num = 0;
            }
            btn = AddButton(entry.getKey(), entry.getValue());
        }

        // Colspan last button
        if(btn != null && btn_num < button_row_size) {
            btn.colspan(button_row_size - btn_num);
        }
    }

    //--------------------------------------------------------------------------
    public void Set(String title, Enum<?> sprite, String body_str, 
      Map<String, ClickListener> buttons, int button_row_size) {
        if(title != null) {
            SetTitle(title, true);
        }
        if(sprite != null) { 
            SetSprite(sprite);
        }
        if(body_str != null) {
            SetText(body_str);
        }
        if(buttons != null) {
            SetButtons(buttons, button_row_size);
        }
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public void SetTitle(String title) {
        if(HasTitle()) {
            super.SetTitle(title);
        } else {
            Cell<VisLabel> title_label = 
              AddCellLabel(m_header_title, title, null, null);
            if(Main.inst.gui.GetFontScale() == 1) {
                title_label.getActor().setFontScale(1.5f);
            }
            title_label.getActor().setColor(Color.YELLOW);
            title_label.expandX().fillX().row();
        }
    }

    //--------------------------------------------------------------------------
    @Override public EngineWnd OnPostReset() {
        float btn_size = GetButtonSize();

        // Header
        m_header = AddCellTable(null, null, null, true).align(Align.left);

        // Header icon
        m_header_icon = AddCellImage(m_header, MapEnum.ItemType.AMULET, btn_size, btn_size)
          .align(Align.left);

        // Header title
        m_header_title = AddCellTable(m_header, null, null, false).fill().expand();

        // Body text
        row();
        m_text = AddCellTextBox("empty").expand().fill();

        // Button bar
        row();
        m_button_bar = AddCellTable(null, null, null, true)
          .expandX().fillX();

        // Window size
        SetFixedSize(
          Main.inst.gui.IsLandscape() ? 0.6f : 0.8f, 0.0f);
        SetMaxSize(0.8f, 0.8f);
        return this;
    }
}
