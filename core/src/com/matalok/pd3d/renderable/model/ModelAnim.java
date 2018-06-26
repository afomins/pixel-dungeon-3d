/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model;

//------------------------------------------------------------------------------
import java.util.LinkedList;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class ModelAnim 
  implements IManaged {
    //**************************************************************************
    // Template
    //**************************************************************************
    public static final int BOX_TR_NUM  = 2 * 6;
    public static final int QUAD_TR_NUM = 2 * 1;
    public static final int TRIG_TR_NUM = 1;

    //**************************************************************************
    // ModelDesc
    //**************************************************************************
    public static class ModelDesc {
        //----------------------------------------------------------------------
        public Model model;
        public int triangle_num;

        //----------------------------------------------------------------------
        public ModelDesc(Model model, int triangle_num) {
            this.model = model;
            this.triangle_num = triangle_num;
        }
    }

    //**************************************************************************
    // FrameDesc
    //**************************************************************************
    public static class FrameDesc {
        //----------------------------------------------------------------------
        public ModelDesc model_desc;
        public RendererModel renderer_model;

        //----------------------------------------------------------------------
        public FrameDesc(ModelDesc model_desc, RendererModel model_inst) {
            this.model_desc = model_desc;
            this.renderer_model = model_inst;
        }
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    public static class Template 
      implements IManaged {
        //----------------------------------------------------------------------
        public int fps;
        public boolean is_looped;
        public LinkedList<ModelDesc> frames;

        //----------------------------------------------------------------------
        public Template(int _fps, boolean _is_looped) {
            frames = new LinkedList<ModelDesc>();
            fps = _fps;
            is_looped = _is_looped;
        }

        //----------------------------------------------------------------------
        public void AddFrame(Model model, int triangle_num) {
            frames.add(new ModelDesc(model, triangle_num));
        }

        //----------------------------------------------------------------------
        @Override public void OnCleanup() {
            for(ModelDesc m : frames) {
                m.model.dispose();
            }
            frames.clear();
            frames = null;
        }
    }

    //**************************************************************************
    // ModelAnim
    //**************************************************************************
    private FrameDesc m_frames[];
    private FrameDesc m_cur_frame;
    private boolean m_is_looped;
    private long m_frame_duration;
    private long m_start_time;
    private boolean m_is_running;

    //--------------------------------------------------------------------------
    public ModelAnim(Template t) {
        this(t.frames.size(), t.fps, t.is_looped);

        int idx = 0;
        for(ModelDesc frame : t.frames) {
            SetFrame(idx++, frame);
        }
    }

    //--------------------------------------------------------------------------
    public ModelAnim(int frame_num, int fps, boolean is_looped) {
        m_frames = new FrameDesc[frame_num];
        m_frame_duration = Utils.SecToMsec(1.0f) / fps;
        m_is_looped = is_looped;
    }

    //--------------------------------------------------------------------------
    public void SwitchTemplate(Template new_template, LinkedList<Disposable> kill_list) {
        Utils.Assert(m_frames.length == new_template.frames.size(), 
          "Failed to switch anim template, wrong number of frames");

        int frame_idx = 0;
        for(ModelDesc new_model_desc : new_template.frames) {
            FrameDesc old_frame_desc = m_frames[frame_idx++];

            if(kill_list != null) {
                kill_list.add(old_frame_desc.model_desc.model);
            }
            old_frame_desc.model_desc = new_model_desc;
            old_frame_desc.renderer_model.SwitchModel(new_model_desc.model);
        }
    }

    //--------------------------------------------------------------------------
    public boolean IsRunning() {
        return m_is_running;
    }

    //--------------------------------------------------------------------------
    public boolean IsLooped() {
        return m_is_looped;
    }

    //--------------------------------------------------------------------------
    public void SetFrame(int idx, ModelDesc model_desc) {
        FrameDesc frame_desc = m_frames[idx] = 
          new FrameDesc(model_desc, new RendererModel(model_desc.model));

        if(idx == 0) {
            m_cur_frame = frame_desc;
        }
    }

    //--------------------------------------------------------------------------
    public RendererModel GetRendererModel() {
        return m_cur_frame.renderer_model;
    }

    //--------------------------------------------------------------------------
    public int GetTriangleNum() {
        return m_cur_frame.model_desc.triangle_num;
    }

    //--------------------------------------------------------------------------
    public RendererModel Start() {
        m_start_time = Main.inst.timer.GetCur();
        m_is_running = (m_frames.length > 1);
        m_cur_frame = m_frames[0];
        return m_cur_frame.renderer_model;
    }

    //--------------------------------------------------------------------------
    public void Stop() {
        m_is_running = false;
    }

    //--------------------------------------------------------------------------
    public RendererModel Update() {
        // Freeze at current frame if not running
        if(!m_is_running) {
            return m_cur_frame.renderer_model;
        }

        // Find number of frames passed since start
        long time_delta = Main.inst.timer.GetCur() - m_start_time;
        int frames_delta = (int)(time_delta / m_frame_duration);

        // Select last frame when animation is over and not looping
        if(!m_is_looped && frames_delta >= m_frames.length) {
            m_cur_frame = m_frames[m_frames.length - 1];
            Stop();

        // Select new frame and loop
        } else {
            m_cur_frame = m_frames[frames_delta % m_frames.length];
        }
        return m_cur_frame.renderer_model;
    }

    //**************************************************************************
    // ModelAnim
    //**************************************************************************
    @Override public void OnCleanup() {
        m_frames = null;
        m_cur_frame = null;
    }
}
