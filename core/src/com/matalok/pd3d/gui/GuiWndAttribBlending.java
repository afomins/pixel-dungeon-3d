//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class GuiWndAttribBlending 
  extends GuiWndAttrib {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static final UtilsClass.IdNameMap blend_func = new UtilsClass.IdNameMap(); 
    static {
        blend_func.put((long)GL20.GL_ZERO,                     "zero");
        blend_func.put((long)GL20.GL_ONE,                      "one");
        blend_func.put((long)GL20.GL_SRC_COLOR,                "src-color");
        blend_func.put((long)GL20.GL_ONE_MINUS_SRC_COLOR,      "one-minus-src-color");
        blend_func.put((long)GL20.GL_DST_COLOR,                "dst-color");
        blend_func.put((long)GL20.GL_ONE_MINUS_DST_COLOR,      "one-minus-dst-color");
        blend_func.put((long)GL20.GL_SRC_ALPHA,                "src-alpha");
        blend_func.put((long)GL20.GL_ONE_MINUS_SRC_ALPHA,      "one-minus-src-alpha");
        blend_func.put((long)GL20.GL_DST_ALPHA,                "dst-alpha");
        blend_func.put((long)GL20.GL_ONE_MINUS_DST_ALPHA,      "one-minus-dst-alpha");
        blend_func.put((long)GL20.GL_CONSTANT_COLOR,           "constant-color");
        blend_func.put((long)GL20.GL_ONE_MINUS_CONSTANT_COLOR, "one-minus-constant-color");
        blend_func.put((long)GL20.GL_CONSTANT_ALPHA,           "constant-alpha");
        blend_func.put((long)GL20.GL_ONE_MINUS_CONSTANT_ALPHA, "one-minus-constant-alpha");
        blend_func.put((long)GL20.GL_SRC_ALPHA_SATURATE,       "src-alpha-saturate");
        blend_func.Init();
    }

    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //----------------------------------------------------------------------
        public void OnUpdate(boolean is_blended, float alpha, int src_func, int dst_func) {};
    }

    //**************************************************************************
    // GuiWndAttribBlending
    //**************************************************************************
    private boolean m_is_blended, m_is_blended_orig;
    private float m_alpha, m_alpha_orig;
    private int m_src_func, m_src_func_orig;
    private int m_dst_func, m_dst_func_orig;
    private Listener m_listener;
    private VisCheckBox m_gui_is_blended;
    private GuiSpinnerFloat m_gui_alpha;
    private GuiSelectBox m_gui_src_func, m_gui_dst_func;

    //--------------------------------------------------------------------------
    public GuiWndAttribBlending() {
        super("Blending");
    }

    //--------------------------------------------------------------------------
    public void Init(boolean is_blended, float alpha, int src_func, int dst_func, 
      Listener listener) {
        Set(m_is_blended_orig = is_blended, 
            m_alpha_orig = alpha,
            m_src_func_orig = src_func,
            m_dst_func_orig = dst_func);
        m_listener = listener;
    }

    //--------------------------------------------------------------------------
    private void Set(boolean is_blended, float alpha, int src_func, int dst_func) {
        m_gui_is_blended.setProgrammaticChangeEvents(false);
        m_gui_is_blended.setChecked(m_is_blended = is_blended);
        m_gui_is_blended.setProgrammaticChangeEvents(true);

        m_gui_alpha.SetValue(m_alpha = alpha, false);
        m_gui_src_func.SetId(m_src_func = src_func, false);
        m_gui_dst_func.SetId(m_dst_func = dst_func, false);
        if(m_listener != null) {
            m_listener.OnUpdate(is_blended, alpha, src_func, dst_func);
        }
    }

    //**************************************************************************
    // GuiWndAttrib
    //**************************************************************************
    @Override public int OnCreateBody() {
        // Is blended
        row();
        add(new VisLabel("is-blended:")).align(Align.left);
        add(m_gui_is_blended = new VisCheckBox(null))
          .expandX().fillX();
        m_gui_is_blended.align(Align.right);
        m_gui_is_blended.setChecked(true);
        m_gui_is_blended.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Set(m_gui_is_blended.isChecked(), m_alpha, m_src_func, m_dst_func);
            }
        });

        // Alpha
        row(); 
        add(new VisLabel("alpha:")).align(Align.left);
        add(m_gui_alpha = new GuiSpinnerFloat(null, 0.0f, 0.0f, 1.0f, 0.1f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  Set(m_is_blended, m_gui_alpha.GetValue(), m_src_func, m_dst_func);
              };
        })).expandX().fillX();

        // Source function
        row(); 
        add(new VisLabel("src-func:")).align(Align.left);
        add(m_gui_src_func = new GuiSelectBox(blend_func)).expandX().fillX();
        m_gui_src_func.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Set(m_is_blended, m_alpha, (int)m_gui_src_func.GetId(), m_dst_func);
            }
        });

        // Destination function
        row(); 
        add(new VisLabel("dst-func:")).align(Align.left);
        add(m_gui_dst_func = new GuiSelectBox(blend_func)).expandX().fillX();
        m_gui_dst_func.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Set(m_is_blended, m_alpha, m_src_func, (int)m_gui_dst_func.GetId());
            }
        });
        return 2;
    }

    //--------------------------------------------------------------------------
    @Override public void OnRestoreSettings() {
        Set(m_is_blended_orig, m_alpha_orig, m_src_func_orig, m_dst_func_orig);
    }
}
