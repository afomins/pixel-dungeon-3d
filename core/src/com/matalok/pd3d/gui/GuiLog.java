//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import java.util.Iterator;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

//------------------------------------------------------------------------------
public class GuiLog 
  extends VisTable {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private Object[] m_color_map = new Object[] {
        new Object[] {"++ ", Color.GREEN},   // positive
        new Object[] {"-- ", Color.RED},     // negative
        new Object[] {"** ", Color.ORANGE},  // warning
        new Object[] {"@@ ", Color.YELLOW},  // neutral
        new Object[] {null, Color.WHITE},   // default
    };

    //**************************************************************************
    // GuiLog
    //**************************************************************************
    private VisLabel m_labels[];
    private String m_last_txt;
    private LinkedList<Object[]> m_stack;

    //--------------------------------------------------------------------------
    public GuiLog(int line_num) {
        m_stack = new LinkedList<Object[]>();
        m_labels = new VisLabel[line_num];
        for(int i = m_labels.length - 1; i >= 0; i--) {
            VisLabel l = m_labels[i] = add(new VisLabel())
              .align(Align.left).expandX().fillX().getActor();
            row();

            LabelStyle style = l.getStyle();
            style.font = VisUI.getSkin().getFont("default-shadow-font");
            l.setStyle(style);
        }
    }

    //--------------------------------------------------------------------------
    public void Write(String txt) {
        // Find color of the text
        Color color = null;
        for(Object color_desc : m_color_map) {
            String prefix = (String)((Object[])color_desc)[0];
            if(prefix == null || txt.startsWith(prefix)) {
                color = (Color)((Object[])color_desc)[1];
                if(prefix != null) {
                    txt = txt.substring(prefix.length());
                }
                break;
            }
        }

        // Ignore duplicate logs
        if(m_last_txt != null && txt.equals(m_last_txt)) {
            return;
        }
        m_last_txt = txt;

        // Trim lines that do not fit in stack
        m_stack.addFirst(new Object[] {txt, color});
        if(m_stack.size() > m_labels.length) {
            m_stack.removeLast();
        }

        // Update text&color of the stack
        Iterator<Object[]> txt_line_it = m_stack.iterator();
        for(VisLabel l : m_labels) {
            String txt_line = "";
            Color txt_color = Color.WHITE;
            if(txt_line_it.hasNext()) {
                Object[] txt_line_desc = txt_line_it.next();
                txt_line = (String)txt_line_desc[0];
                txt_color = (Color)txt_line_desc[1];
            }
            l.setText(txt_line);
            l.setColor(txt_color);
        }
    }
}
