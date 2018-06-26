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
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class RendererProfiler {
    //**************************************************************************
    // Stats
    //**************************************************************************
    public static class Stats {
        //----------------------------------------------------------------------
        private String m_log_fmt;

        public int call_num;
        public int tx_bind_num;
        public int draw_call_num;
        public int shader_switch_num;
        public int vertex_num;

        //----------------------------------------------------------------------
        public Stats(String name) {
            m_log_fmt = String.format(
              " >> name=%s " + 
              "call=%%0%dd tx-bind=%%0%dd draw-call=%%0%dd " + 
              "sh-switch=%%0%dd vertex=%%0%dd", 
              Utils.GetPaddedString(name, Main.inst.cfg.prof_gl_log_pad), 
              5, 5, 5, 5, 5);
        }

        //----------------------------------------------------------------------
        public Stats Reset() {
            call_num = tx_bind_num = draw_call_num = 
              shader_switch_num = vertex_num = 0;
            return this;
        }

        //----------------------------------------------------------------------
        public Stats Update(GLProfiler gl_profiler) {
            call_num = gl_profiler.getCalls();
            tx_bind_num = gl_profiler.getTextureBindings();
            draw_call_num = gl_profiler.getDrawCalls();
            shader_switch_num = gl_profiler.getShaderSwitches();
            vertex_num = gl_profiler.getVertexCount().count;
            return this;
        }

        //----------------------------------------------------------------------
        public Stats Set(Stats target) {
            call_num = target.call_num;
            tx_bind_num = target.tx_bind_num;
            draw_call_num = target.draw_call_num;
            shader_switch_num = target.shader_switch_num;
            vertex_num = target.vertex_num;
            return this;
        }

        //----------------------------------------------------------------------
        public Stats Dec(Stats target) {
            call_num -= target.call_num;
            tx_bind_num -= target.tx_bind_num;
            draw_call_num -= target.draw_call_num;
            shader_switch_num -= target.shader_switch_num;
            vertex_num -= target.vertex_num;
            return this;
        }

        //----------------------------------------------------------------------
        public void Log() {
            Logger.d(m_log_fmt, call_num, tx_bind_num, draw_call_num, 
              shader_switch_num, vertex_num);
        }
    }

    //**************************************************************************
    // RendererProfiler
    //**************************************************************************
    public Stats stats_cur;
    public Stats stats_prev;
    private GLProfiler m_gl_profiler;

    //--------------------------------------------------------------------------
    public RendererProfiler() {
        if(Main.inst.cfg.prof_enable) {
            m_gl_profiler = new GLProfiler(Gdx.graphics);
            m_gl_profiler.enable();
        }
        stats_cur = new Stats("total");
        stats_prev = new Stats("total-prev");
    }

    //--------------------------------------------------------------------------
    public RendererProfiler Reset() {
        if(!Main.inst.cfg.prof_enable) {
            return this;
        }

        stats_cur.Reset();
        stats_prev.Reset();
        m_gl_profiler.reset();
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererProfiler Update(Stats dest) {
        if(!Main.inst.cfg.prof_enable) {
            return this;
        }

        // Read current stats
        stats_cur.Update(m_gl_profiler);

        // dest = (cur - prev)
        if(dest != null) {
            dest.Set(stats_cur).Dec(stats_prev);
        }

        // Save current to prev
        stats_prev.Set(stats_cur);
        return this;
    }
}
