//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.matalok.pd3d.renderer.RendererAttrib;
import com.matalok.pd3d.renderer.RendererAttribStack;

//------------------------------------------------------------------------------
public class GuiAttrib 
  extends VisTable {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static class Listener {
        //----------------------------------------------------------------------
        private RendererAttrib.Type m_type;
        private RendererAttribStack.Cfg m_attrib_stack_cfg;

        //----------------------------------------------------------------------
        public Listener(RendererAttrib.Type type, RendererAttribStack.Cfg cfg) {
            m_type = type;
            m_attrib_stack_cfg = cfg;
        }

        //----------------------------------------------------------------------
        public void OnUpdate(RendererAttrib.Cfg cfg, Long old_id) {
            boolean do_reset_all = (old_id != null);
            m_attrib_stack_cfg.RegisterUpdate(m_type, cfg.id, do_reset_all);
            if(do_reset_all) {
                OnResetAll();
            }
        }

        //----------------------------------------------------------------------
        public void OnDelete(RendererAttrib.Cfg cfg) {
            m_attrib_stack_cfg.DelAttrib(m_type, cfg);
            m_attrib_stack_cfg.RegisterUpdate(m_type, cfg.id, true);
            OnResetAll();
        }

        //----------------------------------------------------------------------
        public void OnToggle(RendererAttrib.Cfg cfg) {
            m_attrib_stack_cfg.RegisterUpdate(m_type, cfg.id, true);
            OnResetAll();
        }

        //----------------------------------------------------------------------
        public void OnResetAll() {
        }
    };

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    protected RendererAttrib.Type m_type;
    protected long m_id;
    protected RendererAttrib.Cfg m_cfg;
    protected Listener m_listener;
    protected boolean m_is_created;

    protected Cell<VisCheckBox> m_on_cb;
    protected Cell<VisLabel> m_name_label;
    protected Cell<VisSelectBox<String>> m_id_select_box;
    protected Cell<VisTextButton> m_del_button;

    //--------------------------------------------------------------------------
    public GuiAttrib(RendererAttrib.Type type, RendererAttrib.Cfg cfg, 
      final Listener listener) {
        m_type = type;
        m_id = cfg.id;
        m_cfg = cfg;
        m_listener = listener;

        // Toggler
        m_on_cb = add(new VisCheckBox(null));
        m_on_cb.getActor().setChecked(m_cfg.on);
        m_on_cb.getActor().addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                m_cfg.on = m_on_cb.getActor().isChecked();
                m_listener.OnToggle(m_cfg);
            }
        });

        // Id label
        m_name_label = add(new VisLabel());

        // Id select box
        m_id_select_box = add(new VisSelectBox<String>()).expandX().fillX();
        final VisSelectBox<String> sbox = m_id_select_box.getActor();
        sbox.setItems(type.id_name_map.GetNames());
        sbox.setSelected(type.id_name_map.GetNameById(cfg.id));
        sbox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                m_cfg.id = m_type.id_name_map.GetIdByIdx(sbox.getSelectedIndex());
                OnUpdate();
            }
        });

        // Body
        OnCreateBody();

        // Delete button
        m_del_button = add(new VisTextButton("del",
          new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                m_listener.OnDelete(m_cfg);
            }}
        ));

        // Update
        OnUpdate();
        m_is_created = true;
    }

    //--------------------------------------------------------------------------
    public RendererAttrib.Cfg GetCfg() {
        return m_cfg;
    }

    //--------------------------------------------------------------------------
    public void OnCreateBody() {
    }

    //--------------------------------------------------------------------------
    public void OnUpdate() {
        boolean id_changed = (m_id != m_cfg.id);
        if(m_name_label.getActor().getText().length == 0 || id_changed) {
            m_name_label.getActor().setText(m_type.GetShortName(m_cfg.id));
        }
        if(m_is_created) {
            m_listener.OnUpdate(m_cfg, id_changed ? m_id : null);
        }
        m_id = m_cfg.id;
    }
}
