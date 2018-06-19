// -----------------------------------------------------------------------------
package com.matalok.pd3d.level;

//------------------------------------------------------------------------------
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.GeomBuilder.CubeSide;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class LevelFog 
 extends GameNode {
    //**************************************************************************
    // State
    //**************************************************************************
    public enum State {
        VISIBLE,        // alpha =  max
        INVISIBLE,      // alpha =  0.0f
        FADING_IN,      // alpha -> max
        FADING_OUT      // alpha -> 0.0f
    };

    //**************************************************************************
    // Node
    //**************************************************************************
    public class Node {
        //----------------------------------------------------------------------
        public UtilsClass.SimpleTween tweener;
        public State state;
        public int x, y;

        //----------------------------------------------------------------------
        public Node(int x, int y) {
            tweener = new UtilsClass.SimpleTween();
            tweener.SetValue(Main.inst.cfg.fog_alpha);
            state = State.VISIBLE;
            this.x = x; this.y = y;
        }
    };

    //**************************************************************************
    // LevelFog
    //**************************************************************************
    private Node[] m_nodes;
    private LinkedList<Node> m_fading_nodes, m_invisible_nodes;

    private int m_map_width, m_map_height;
    private Vector3 m_node_size, m_node_offset;
    private Color m_color;
    private Pixmap m_pixmap;
    private Texture m_texture;
    private Material m_material;
    private Model m_model;
    private RendererModel m_renderer_model;
    private Renderer.Layer m_renderer_type;
    private boolean m_was_fading;
    private UtilsClass.PeriodicTask m_update_task;

    //--------------------------------------------------------------------------
    public LevelFog(Renderer.Layer renderer_type, int map_width, int map_height, 
      Vector3 node_size, Vector3 node_offset, float alpha, Color color) {
        super("fog", alpha);

        // Renderer type
        m_renderer_type = renderer_type;

        // Map
        m_map_width = map_width;
        m_map_height = map_height;
        m_node_offset = node_offset;
        m_node_size = node_size;

        // Color of the fog
        m_color = new Color(color);

        // Pixmap covers all map
        m_pixmap = new Pixmap(map_width, map_height, Format.RGBA4444);
        m_pixmap.setBlending(Blending.None);

        // Create node array
        m_nodes = new Node[map_width * map_height];
        for(int i = 0; i < m_nodes.length; i++) {
            int x = i % m_map_width, y = i / m_map_width;
            m_nodes[i] = new Node(x, y);
        }

        // List of fading nodes
        m_fading_nodes = new LinkedList<Node>();

        // List of invisible nodes
        m_invisible_nodes = new LinkedList<Node>();
    }

    //--------------------------------------------------------------------------
    public void UpdateVisibility(int idx, float distance_to_origin, boolean is_visible) {
        // Do nothing by default
        boolean add_to_fading_list = false;
        float fade_dest = -1.0f;
        float fade_duration = 0.5f;

        // Do fade-in
        Node node = m_nodes[idx];
        if(is_visible && 
          (node.state == State.INVISIBLE || node.state == State.FADING_OUT)) {
            fade_dest = Main.inst.cfg.fog_alpha;
            fade_duration = (1.0f - distance_to_origin) * 
              Main.inst.cfg.fog_fade_in_duration_factor;

            if(node.state == State.INVISIBLE) {
                add_to_fading_list = true;

                // Stop being invisible
                boolean was_invisible = m_invisible_nodes.remove(node);
                Utils.Assert(was_invisible, 
                  "Failed to update fog visibity, node was not visible");
            }
            node.state = State.FADING_IN;

        // Do fade-out
        } else if(!is_visible && 
          (node.state == State.VISIBLE || node.state == State.FADING_IN)) {
            fade_dest = 0.0f;
            fade_duration = distance_to_origin * 
              Main.inst.cfg.fog_fade_out_duration_factor;
            add_to_fading_list = (node.state == State.VISIBLE);
            node.state = State.FADING_OUT;
        }

        // Check if fading is needed
        if(fade_dest < 0.0f) {
            return;
        }

        // Start fading
        node.tweener.Start(
          Main.inst.timer.GetCurSec(), 
          fade_duration, fade_dest);

        // Add to fading list once
        if(add_to_fading_list) {
            m_fading_nodes.add(node);
        }
    }

    //--------------------------------------------------------------------------
    private boolean UpdateFading() {
        float cur_time = Main.inst.timer.GetCurSec();

        // Nothing is fading 
        if(m_fading_nodes.isEmpty()) {
            return false;
        }

        // Update node fading
        Iterator<Node> it = m_fading_nodes.iterator();
        while(it.hasNext()) {
            Node node = it.next();

            // Update node fading
            if(Main.inst.cfg.fog_fading_enabled && 
              !node.tweener.Update(cur_time)) {
                continue; // Fading is not over yet
            }

            // Stop fading and remove node from fading list
            it.remove();

            // Become visible
            if(node.state == State.FADING_IN) {
                node.state = State.VISIBLE;

            // Become invisible
            } else if(node.state == State.FADING_OUT) {
                node.state = State.INVISIBLE;
                m_invisible_nodes.add(node);

            // O-o-o-ops - wrong state
            } else {
                Utils.Assert(false, "Failed to process fog, wrong node state");
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    private void UpdateModel() {
        // Update pixmap #0 - fill visible nodes
        m_color.a = Main.inst.cfg.fog_alpha;
        m_pixmap.setColor(m_color);
        m_pixmap.fill();

        // Update pixmap #1 - fill invisible nodes
        m_color.a = 0.0f;
        m_pixmap.setColor(m_color);
        for(Node node : m_invisible_nodes) {
            m_pixmap.drawPixel(node.x, node.y);
        }

        // Update pixmap #2 - fill fading nodes
        for(Node node : m_fading_nodes) {
            m_color.a = node.tweener.GetValue();
            m_pixmap.setColor(m_color);
            m_pixmap.drawPixel(node.x, node.y);
        }

        // Update existing texture
        if(m_renderer_model != null) {
            m_texture.draw(m_pixmap, 0, 0);

        // Create new texture, material & model
        } else {
            // New texture
            m_texture = new Texture(m_pixmap);

            // New material
            m_material = RendererModel.CreateMaterial(
              null, m_texture, false, true);

            // New model
            GeomBuilder builder = Main.inst.geom_builder;
            float layer_size = (Main.inst.cfg.fog_layer_num == 2) ? 0.9f * m_node_offset.y: 
              m_node_offset.y / Main.inst.cfg.fog_layer_num; 
            for(int i = 0; i < Main.inst.cfg.fog_layer_num; i++) {
                float width = m_map_width * m_node_size.x;
                float height = m_map_height * m_node_size.z;
                builder.InitVertex4(width, height,
                  width / 2 - m_node_size.x / 2, m_node_offset.y - i * layer_size, 
                  height / 2 - m_node_size.z / 2, null);

                // Create cube
                builder.CreateCube(CubeSide.TOP.bit,
                  Usage.Position | Usage.TextureCoordinates, 
                  m_material, 1.0f, 1.0f, 0.0f, false, null);
            }
            if(m_model != null) {
                m_model.dispose();
            }
            m_model = builder.FinalizeModel();
            m_renderer_model = new RendererModel(m_model);
        }
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Update fog periodically
        long cur_time = Main.inst.timer.GetCur();
        if(m_update_task == null) {
            m_update_task = new UtilsClass.PeriodicTask(cur_time, 
              Utils.SecToMsec(Main.inst.cfg.fog_update_interval), 
              new UtilsClass.Callback() {
                //--------------------------------------------------------------
                private int m_update_num;
                private long m_begin_time;

                //--------------------------------------------------------------
                @Override public Object Run(Object... args) {
                    // Update fog model when fading
                    if(UpdateFading() || m_renderer_model == null) {
                        UpdateModel();
                        m_update_num++;
                    }
    
                    // Begin fading
                    long cur_time = Main.inst.timer.GetCur();
                    boolean is_fading = (m_fading_nodes.size() > 0);
                    if(!m_was_fading && is_fading) {
                        Logger.d("Fog fading - begin");
                        m_update_num = 0;
                        m_begin_time = cur_time;

                    // End fading
                    } else if(m_was_fading && !is_fading) {
                        Logger.d("Fog fading - end :: update-num=%d time-diff=%.1fsec", 
                          m_update_num, Utils.MsecToSec(cur_time - m_begin_time));
                    }
    
                    // Save fading flag for next iteration
                    m_was_fading = is_fading;
                    return null;
                }
            });
        }
        m_update_task.Run(cur_time);

        // Add to rendering layer
        ctx.AddLayerObject(m_renderer_model, m_renderer_type, 
          true, 2 * Main.inst.cfg.fog_layer_num);
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodJson(JsonCtx ctx) {
        super.OnMethodJson(ctx);

        if(ctx.pre_children && 
           ctx.targets.contains(JsonTarget.COMMON)) {
            ctx.Write("\"map-width\" : %d, \"map-height\" : %d,", 
              m_map_width, m_map_height);
            ctx.Write("\"node-num\" : %d, \"fading-num\" : %d, \"invisible-num\" : %d,", 
              m_nodes.length, m_fading_nodes.size(), m_invisible_nodes.size());
            for(int y = 0 ; y < m_map_height; y++) {
                String line = "";
                for(int x = 0 ; x < m_map_width; x++) {
                    Node node = m_nodes[y * m_map_height + x];
                    char c = '.';
                    if(node.state == State.VISIBLE) {
                        c = '#';
                    } else if(node.state == State.FADING_IN) {
                        c = '^';
                    } else if(node.state == State.FADING_OUT) {
                        c = 'v';
                    }
                    line += c;
                }

                if(y == 0) {
                    ctx.Write("\"map\" : [ \"%s\",", line);
                } else if (y == m_map_height - 1) {
                    ctx.Write("          \"%s\",]", line);
                } else {
                    ctx.Write("          \"%s\",", line);
                }
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_nodes = null;

        m_fading_nodes.clear();
        m_fading_nodes = null;

        m_invisible_nodes.clear();
        m_invisible_nodes = null;

        m_pixmap.dispose();
        m_pixmap = null;

        if(m_renderer_model != null) {
            m_model.dispose();
            m_model = null;

            m_renderer_model.OnCleanup();
            m_renderer_model = null;

            m_texture.dispose();
            m_texture = null;

            m_material = null;
        }
        return true;
    }
}
