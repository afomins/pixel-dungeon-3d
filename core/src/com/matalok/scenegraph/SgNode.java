//------------------------------------------------------------------------------
package com.matalok.scenegraph;

//-------------------------------------------------------------------------------
import java.util.EnumSet;
import java.util.Iterator;
import java.util.TreeSet;

import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescSnapshot;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public abstract class SgNode 
  extends SgObject.SgObjectNoLog {
    //**************************************************************************
    // METHOD - CLEANUP
    //**************************************************************************
    public static SgMethod CLEANUP = new SgMethod("cleanup", false, true, true) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, int lvl, 
          boolean is_first, boolean is_last, Object arg) {
            return node.OnMethodCleanup();
        }
    };

    //**************************************************************************
    // METHOD - JSON
    //**************************************************************************
    public static enum JsonTarget {
        COMMON,
        SNAPSHOT
    };

    //--------------------------------------------------------------------------
    private static String[] spaces = new String[64];
    private static String GetEmptyString(int size) {
        if(spaces[size] == null) {
            spaces[size] = (size == 0) ? "" :
              new String(new char[size]).replace("\0", " ");
        }
        return spaces[size];
    }

    //--------------------------------------------------------------------------
    public static class JsonCtx {
        //----------------------------------------------------------------------
        public boolean pre_children;
        public boolean is_first_child, is_last_child;
        public int lvl;
        public String lvl_prefix, align_prefix;
        public String root_name;
        public SgUtils.IWriter writer;
        public EnumSet<JsonTarget> targets;

        //----------------------------------------------------------------------
        public JsonCtx(SgUtils.IWriter writer, String root_name, int align_size, 
          EnumSet<JsonTarget> targets) {
            this.lvl = -1;
            this.writer = writer;
            this.root_name = root_name;
            this.targets = targets;
            this.align_prefix = GetEmptyString(align_size);
            Update(true, 0, true, true);
        }

        //----------------------------------------------------------------------
        public JsonCtx UpdateLevel(int inc) {
            return Update(pre_children, lvl + inc, is_first_child, is_last_child);
        }

        //----------------------------------------------------------------------
        public JsonCtx UpdateAlign(int cnt) {
            align_prefix = GetEmptyString(align_prefix.length() + cnt);
            return this;
        }

        //----------------------------------------------------------------------
        public JsonCtx Update(boolean pre_children, int lvl, boolean is_first, 
          boolean is_last) {
            this.lvl = lvl;
            this.lvl_prefix = GetEmptyString(1 + lvl * 2);
            this.pre_children = pre_children;
            this.is_first_child = is_first;
            this.is_last_child = is_last;
            return this;
        }

        //----------------------------------------------------------------------
        public JsonCtx Write(String str, Object... args) {
//            writer.Write(String.format("pfx=%02d |", lvl) + prefix + str, args);
            writer.Write(lvl_prefix + align_prefix + str, args);
            return this;
        }
    }

    //----------------------------------------------------------------------
    public static SgMethod JSON = new SgMethod("json", true, true, true) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            JsonCtx ctx = (JsonCtx)arg;
            return node.OnMethodJson(ctx.Update(pre_children, lvl, is_first, is_last));
        }
    };

    //**************************************************************************
    // METHOD - SNAPSHOT
    //**************************************************************************
    public static SgMethod SNAPSHOT = new SgMethod("snapshot", true, true, false) { 
        @Override public boolean OnRun(SgNode node, boolean pre_children, 
          int lvl, boolean is_first, boolean is_last, Object arg) {
            DescSnapshot snapshot = (DescSnapshot)arg;
            return node.OnMethodSnapshot(snapshot);
        }
    };

    //**************************************************************************
    // State
    //**************************************************************************
    public enum SgState {
        IDLE,
        IN_TREE,
        DELETED
    };

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static void Walk(SgNode node, boolean forward, SgMethod method, 
      int lvl, Object arg) {
        Walk(node, forward, method, lvl, true, true, arg);
    }

    //--------------------------------------------------------------------------
    public static void Walk(SgNode node, boolean forward, SgMethod method, 
      int lvl, boolean is_first, boolean is_last, Object arg) {
        // Start performance counter
        SgMethod.PerfCnt perf_cnt = node.SgGetPerformanceCounter(method);
        if(perf_cnt != null) {
            perf_cnt.Start(method);
        }

        boolean run_children = method.RunChildren();
        boolean run_pre_children = method.RunPreChildren();
        boolean run_post_children = method.RunPostChildren();

        // Run own method before children
        if(run_pre_children) {
            method.stat_run_pre_child_num++;
            run_children = method.OnRun(node, true, lvl, is_first, is_last, arg);
        }

        // Walk children
        if(run_children) {
            Iterator<SgNode> it = (forward) ? node.m_sg_children.iterator() : 
              node.m_sg_children.descendingIterator();
            int child_lvl = lvl + 1;
            boolean is_first_child = true;
            while(it.hasNext()) {
                SgNode child = it.next();
                SgNode.Walk(child, forward, method, child_lvl, 
                  is_first_child, !it.hasNext(), arg);

                // Detach child from parent if node was deleted
                if(child.SgGetState() == SgState.DELETED) {
                    child.SgDetachFromParent(false);
                    it.remove();
                }
                is_first_child = false;
            }
        }

        // Run own method after children
        if(run_post_children) {
            method.stat_run_post_child_num++;
            method.OnRun(node, false, lvl, is_first, is_last, arg);
        }

        // Detach top node from parent if it was deleted from tree
        if(node.SgGetState() == SgState.DELETED && 
          lvl == 0 && node.SgGetParent() != null) {
            node.SgDetachFromParent(true);
        }

        // Stop performance counter
        if(perf_cnt != null) {
            perf_cnt.Stop(method);
        }
    }

    //--------------------------------------------------------------------------
    private static int sg_id;

    //**************************************************************************
    // SgNode
    //**************************************************************************
    private SgNode m_sg_parent;
    private TreeSet<SgNode> m_sg_children;
    private SgState m_sg_state;
    private SgMethod.PerfCnt[] m_perf_cnt;

    //--------------------------------------------------------------------------
    public SgNode(String name) {
        super(SgNode.sg_id++, name);

        m_sg_children = new TreeSet<SgNode>(SgObject.priority_comparator);
        m_sg_state = SgState.IDLE;

        // Each method can have own performance counter
        m_perf_cnt = new SgMethod.PerfCnt[SgMethod.sg_id];
        Dbg("Creating node :: node=%s", SgGetNameId());
    }

    //--------------------------------------------------------------------------
    public SgMethod.PerfCnt SgEnablePerformanceCounter(SgMethod method) {
        int method_id = method.SgGetId();
        Utils.Assert(m_perf_cnt[method_id] == null, 
          "Failed to enable performance counter, counter already present :: method=%s node=%s", 
          method.SgGetNameId(), SgGetNameId());

        String name = String.format("%s-%s", SgGetNameId(), method.SgGetName());
        SgMethod.PerfCnt perf_cnt = m_perf_cnt[method_id] = new SgMethod.PerfCnt(
          Utils.GetPaddedString(name, Main.inst.cfg.prof_perf_cnt_log_pad));
        return perf_cnt;
    }

    //--------------------------------------------------------------------------
    public SgMethod.PerfCnt SgDisablePerformanceCounter(SgMethod method) {
        int method_id = method.SgGetId();
        Utils.Assert(m_perf_cnt[method_id] != null, 
          "Failed to disable performance counter, counter not present :: method=%s node=%s", 
          method.SgGetNameId(), SgGetNameId());

        SgMethod.PerfCnt perf_cnt = m_perf_cnt[method_id];
        m_perf_cnt[method_id] = null;
        return perf_cnt;
    }

    //--------------------------------------------------------------------------
    public SgMethod.PerfCnt SgGetPerformanceCounter(SgMethod method) {
        return m_perf_cnt[method.SgGetId()];
    }

    //--------------------------------------------------------------------------
    public void SgSetState(SgState state) {
        Dbg("Switching node state :: node=%s new-state=%s", 
          SgGetNameId(), state);
        m_sg_state = state;
    }

    //--------------------------------------------------------------------------
    public SgNode.SgState SgGetState() {
        return m_sg_state;
    }

    //--------------------------------------------------------------------------
    public SgNode SgGetParent() {
        return m_sg_parent;
    }

    //--------------------------------------------------------------------------
    public SgNode SgGetParent(Class<? extends SgNode> parent_class) {
        SgNode node = SgGetParent();
        while(node != null) {
            if(node.getClass() == parent_class) {
                break;
            }
            node = node.SgGetParent();
        }
        return node;
    }

    //--------------------------------------------------------------------------
    public SgNode SgAttachToParent(SgNode parent) {
        String old_parent = (m_sg_parent == null) ? "none" : m_sg_parent.SgGetNameId();
        String new_parent = (parent == null) ? "none" : parent.SgGetNameId();

        Dbg("Attaching node to parent :: node=%s parent=%s", SgGetNameId(), new_parent);

        Assert(m_sg_state == SgState.IDLE, 
          "Failed to attach node to parent, wrong state");
        Assert(m_sg_parent == null, 
          "Failed to attach node to parent, child already has parent :: old-parent=%s",
          old_parent);

        m_sg_parent = parent;
        SgSetState(SgState.IN_TREE);
        return this;
    }

    //--------------------------------------------------------------------------
    private SgNode SgDetachFromParent(boolean remove_from_parent) {
        Dbg("Detaching node from parent :: node=%s parent=%s remove-from-parent=%s", 
          SgGetNameId(), m_sg_parent == null ? "none" : m_sg_parent.SgGetNameId(), 
          Boolean.toString(remove_from_parent));

        Assert(m_sg_state == SgState.IN_TREE || m_sg_state == SgState.DELETED, 
          "Failed to detach from parent, wrong state");

        // Remove self from parent's list
        if(remove_from_parent) {
            Assert(m_sg_parent != null, "Failed to detach from parent, no parent");
            Assert(m_sg_parent.m_sg_children.contains(this), 
              "Failed to detach from parent, parent-child mismatch");

            m_sg_parent.m_sg_children.remove(this);
        }
        m_sg_parent = null;

        // Reset state only if node is not being deleted
        if(m_sg_state == SgState.IN_TREE) {
            SgSetState(SgState.IDLE);
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public SgNode SgAddChild(SgNode child) {
        Dbg("Adding child node :: node=%s child=%s", 
          SgGetNameId(), child.SgGetNameId());

        m_sg_children.add(child);
        return child.SgAttachToParent(this);
    }

    //--------------------------------------------------------------------------
    public SgNode SgRelocateChild(SgNode child) {
        Dbg("Relocating child node :: node=%s child=%s old-parent=%s", 
          SgGetNameId(), child.SgGetNameId(), 
          (child.m_sg_parent == null) ? "none" : child.m_sg_parent.SgGetNameId());

        // Child better be active
        Assert(m_sg_state == SgState.IN_TREE, 
          "Failed to relocate child node, wrong state");

        // Detach child from old parent
        if(m_sg_parent != null) {
            child.SgDetachFromParent(true);
        }

        // Attach to new parent
        return SgAddChild(child);
    }

    //--------------------------------------------------------------------------
    public Iterator<SgNode> SgGetChildren() {
        return m_sg_children.iterator();
    }

    //--------------------------------------------------------------------------
    public SgNode SgGetFirstChild() {
        return (m_sg_children.size() == 0) ? null : m_sg_children.first();
    }

    //--------------------------------------------------------------------------
    public int SgGetChildNum() {
        return m_sg_children.size();
    }

    //--------------------------------------------------------------------------
    public SgNode SgGetChild(String name) {
        for(SgNode n : m_sg_children) {
            if(n.m_sg_name.equals(name)) {
                return n;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public SgNode SgGetChild(Class<? extends SgNode> child_class) {
        for(SgNode n : m_sg_children) {
            if(n.getClass() == child_class) {
                return n;
            }
        }
        return null;
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    public boolean OnMethodCleanup() {
        Dbg("Cleanup node :: node=%s parent=%s", 
          SgGetNameId(), (m_sg_parent != null) ? m_sg_parent.SgGetNameId() : "none");

        Assert(m_sg_state == SgState.IDLE || m_sg_state == SgState.IN_TREE, 
          "Failed to clean node, wrong state");

        // Become deleted
        SgSetState(SgState.DELETED);

        // Cleanup children
        m_sg_children.clear();
        m_sg_children = null;

        // Cleanup parent
        OnCleanup();
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodJson(JsonCtx ctx) {
        boolean is_root = (ctx.lvl == 0);
        if(ctx.pre_children) {
            if(!is_root && ctx.is_first_child) {
                ctx.UpdateLevel(-1)
                   .Write("\"sg-children\" : [")
                   .UpdateLevel(+1);
            }

            String str = "";
            if(is_root) {
                if(ctx.root_name != null) {
                    str = String.format("%s{ ", ctx.root_name);
                }
            } else {
                str = "{ ";
            }

            if(ctx.targets.contains(JsonTarget.COMMON)) {
                str += String.format(
                  "\"sg-name\" : \"%s\", \"sg-id\" : %d, \"sg-state\" : \"%s\",",
                  SgGetName(), SgGetId(), SgGetState());
            }

            if(str.length() > 0) {
                ctx.Write(str)
                   .UpdateAlign(+2);
            }

        } else {
            String str = "},";
            if(is_root && ctx.root_name == null) {
                str = "";
            }
            if(str.length() > 0) {
                ctx.UpdateAlign(-2)
                   .Write(str);
            }

            if(!is_root && ctx.is_last_child) {
                ctx.UpdateLevel(-1)
                   .Write("]")
                   .UpdateLevel(+1);
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodSnapshot(DescSnapshot desc) {
        return false;
    }

    //**************************************************************************
    // SgObject
    //**************************************************************************
    @Override public String SgGetNameId() {
        return m_sg_name_id;// + ":" + m_sg_state.toString();
    }
}
