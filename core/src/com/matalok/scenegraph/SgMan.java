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
package com.matalok.scenegraph;

//------------------------------------------------------------------------------
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.shared.Logger;
import com.matalok.scenegraph.SgNode.JsonTarget;
import com.matalok.scenegraph.SgNode.SgState;

//------------------------------------------------------------------------------
public class SgMan 
  implements SgUtils.IPlatform {
    //**************************************************************************
    // SgMan
    //**************************************************************************
    private SgNode m_root;
    private HashSet<SgNode> m_scheduled_for_deletion;
    private HashMap<SgNode, LinkedList<SgMethod>> m_perf_cnt_methods;
    private LinkedList<SgMethod.PerfCnt> m_perf_cnt_ordered;

    //--------------------------------------------------------------------------
    public SgMan(SgNode root) {
        m_root = root.SgAttachToParent(null);
        m_scheduled_for_deletion = new HashSet<SgNode>();
        m_perf_cnt_methods = new HashMap<SgNode, LinkedList<SgMethod>>();
        m_perf_cnt_ordered = new LinkedList<SgMethod.PerfCnt>();
    }

    //--------------------------------------------------------------------------
    public SgNode GetRoot() {
        return m_root;
    }

    //--------------------------------------------------------------------------
    public void ScheduleForDeletion(SgNode node) {
        Dbg("Scheduling node for deletion :: node=%s", node.SgGetNameId());

        Assert(node.SgGetState() == SgState.IN_TREE || node.SgGetState() == SgState.IDLE, 
          "Failed to schedule node for deletion, wrong state");

        m_scheduled_for_deletion.add(node);
    }

    //--------------------------------------------------------------------------
    public void EnablePerformanceCounter(SgNode node, SgMethod method) {
        // Line break
        if(node == null) {
            m_perf_cnt_ordered.add(null);
            return;
        }

        // Save performance counter mapping
        LinkedList<SgMethod> perf_cnt_methods = m_perf_cnt_methods.get(node);
        if(perf_cnt_methods == null) {
            perf_cnt_methods = new LinkedList<SgMethod>();
            m_perf_cnt_methods.put(node, perf_cnt_methods);
        }
        perf_cnt_methods.add(method);

        // Enable node's performance counter for specific method
        m_perf_cnt_ordered.add(
          node.SgEnablePerformanceCounter(method));
    }

    //--------------------------------------------------------------------------
    public void DisablePerformanceCounter(SgNode node) {
        LinkedList<SgMethod> perf_cnt_methods = m_perf_cnt_methods.get(node);
        if(perf_cnt_methods == null) {
            return;
        }

        // Delete performance counter from node
        for(SgMethod method : perf_cnt_methods) {
            m_perf_cnt_ordered.remove(
              node.SgDisablePerformanceCounter(method));
        }

        // Clear performance counter mapping
        perf_cnt_methods.clear();
        m_perf_cnt_methods.remove(node);
    }

    //--------------------------------------------------------------------------
    public void UpdatePerformanceCounters() {
        for(SgMethod.PerfCnt perf_cnt : m_perf_cnt_ordered) {
            if(perf_cnt == null) {
                continue;
            }
            perf_cnt.Prepare();
        }
    }

    //--------------------------------------------------------------------------
    public void LogPerformanceCounters() {
        for(SgMethod.PerfCnt perf_cnt : m_perf_cnt_ordered) {
            // Line break
            if(perf_cnt == null) {
                Logger.d("");
                continue;
            }

            String call_num = String.format("%d + %d = %d", 
              perf_cnt.stat_run_pre_child_num, perf_cnt.stat_run_post_child_num,
              perf_cnt.stat_run_pre_child_num + perf_cnt.stat_run_post_child_num);

            Logger.d(" >> name=%s time-sec=%.8f load=%03d%% call-num=%s", 
              perf_cnt.name, perf_cnt.time.value, 
              (int)(perf_cnt.load.value * 100), call_num);
        }
    }

    //--------------------------------------------------------------------------
    public SgUtils.IWriter GetWriter(FileHandle h_file) {
        // Write to log
        SgUtils.IWriter writer = null;
        if(h_file == null) {
            writer = new SgWriterLog(this) {
                @Override public void Write(String str, Object... args) {
                    super.Write(">> " + str, args);
                }
            };

        // Write to file
        } else {
            writer = new SgWriterFile(h_file);
        }
        return writer;
    }

    //--------------------------------------------------------------------------
    public void RunJson(SgNode node, SgUtils.IWriter writer, String root_name, 
      int align_size, JsonTarget... targets) {
        EnumSet<JsonTarget> json_targets = EnumSet.noneOf(JsonTarget.class);
        for(JsonTarget target : targets) {
            json_targets.add(target);
        }

        SgNode root = (node != null) ? node : m_root;
        Dbg("Running json :: root=%s dest=%s targets=%s", root.SgGetNameId(),
          writer.toString(), json_targets.toString().toLowerCase());

        SgNode.Walk(root, true, SgNode.JSON.ResetStats(), 0, 
          new SgNode.JsonCtx(writer, root_name, align_size, json_targets));
    }

    //--------------------------------------------------------------------------
    public void RunSnapshot(SgNode node, SgUtils.IWriter writer, String root_name, 
      int align_size, JsonTarget... targets) {
    }

    //--------------------------------------------------------------------------
    public void RunCleanup() {
        if(m_scheduled_for_deletion.size() == 0) {
            return;
        }

        Dbg("Running cleanup :: num=%s", m_scheduled_for_deletion.size());
        for(SgNode node : m_scheduled_for_deletion) {
            Dbg("Running node cleanup :: node=%s", node.SgGetNameId());

            // Ignore delete objects
            if(node.SgGetState() == SgNode.SgState.DELETED) {
                continue;
            }

            // Delete object and it's children
            SgNode.Walk(node, true, SgNode.CLEANUP.ResetStats(), 0, null);

            // Disable performance counters 
            DisablePerformanceCounter(node);
        }
        m_scheduled_for_deletion.clear();
    }

    //**************************************************************************
    // SgUtils.IPlatform
    //**************************************************************************
    @Override public void Dbg(String fmt, Object... args) {
        SgObject.platform.Dbg("SG: " + fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Inf(String fmt, Object... args) {
        SgObject.platform.Inf("SG: " + fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Err(String fmt, Object... args) {
        SgObject.platform.Err("SG: " + fmt, args);
    }

    //--------------------------------------------------------------------------
    @Override public void Assert(boolean statement, String fmt, Object... args) {
        SgObject.platform.Assert(statement, "SG: " + fmt, args);
    }
}
