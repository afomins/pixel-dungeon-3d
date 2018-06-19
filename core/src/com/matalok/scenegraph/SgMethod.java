//------------------------------------------------------------------------------
package com.matalok.scenegraph;

//------------------------------------------------------------------------------
import com.badlogic.gdx.utils.PerformanceCounter;

//------------------------------------------------------------------------------
public class SgMethod 
  extends SgObject {
    //**************************************************************************
    // PerfCnt
    //**************************************************************************
    public static class PerfCnt 
      extends PerformanceCounter {
        //----------------------------------------------------------------------
        public int stat_run_pre_child_num;
        public int stat_run_post_child_num;

        //----------------------------------------------------------------------
        public PerfCnt(String name) {
            super(name);
        }

        //----------------------------------------------------------------------
        public void Prepare() {
            tick();
            stat_run_pre_child_num = stat_run_post_child_num = 0;
        }

        //----------------------------------------------------------------------
        public void Start(SgMethod method) {
            start();
            stat_run_pre_child_num = method.stat_run_pre_child_num;
            stat_run_post_child_num = method.stat_run_post_child_num;
        }

        //----------------------------------------------------------------------
        public void Stop(SgMethod method) {
            stop();
            stat_run_pre_child_num = 
              method.stat_run_pre_child_num - stat_run_pre_child_num;
            stat_run_post_child_num = 
              method.stat_run_post_child_num - stat_run_post_child_num;
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static int sg_id;

    //**************************************************************************
    // SgMethod
    //**************************************************************************
    private boolean m_run_pre_child;
    private boolean m_run_child;
    private boolean m_run_post_child;

    public int stat_run_pre_child_num;
    public int stat_run_post_child_num;

    //--------------------------------------------------------------------------  
    public SgMethod(String name, boolean run_pre_children, boolean run_children, 
      boolean run_post_child_children) {
        super(SgMethod.sg_id++, name);
        m_run_pre_child = run_pre_children;
        m_run_child = run_children;
        m_run_post_child = run_post_child_children;
    }

    //--------------------------------------------------------------------------
    public SgMethod ResetStats() {
        stat_run_pre_child_num = stat_run_post_child_num = 0;
        return this;
    }

    //--------------------------------------------------------------------------
    public boolean RunPreChildren() {
        return m_run_pre_child;
    }

    //--------------------------------------------------------------------------
    public boolean RunChildren() {
        return m_run_child;
    }

    //--------------------------------------------------------------------------
    public boolean RunPostChildren() {
        return m_run_post_child;
    }

    //--------------------------------------------------------------------------
    public boolean OnRun(SgNode node, boolean pre_children, int lvl, 
      boolean is_first, boolean is_last, Object arg) {
        return true;
    }
}
