//------------------------------------------------------------------------------
package com.matalok.pd3d.level;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class LevelTrashBin 
  extends GameNode {
    //**************************************************************************
    // LevelTrashBin 
    //**************************************************************************
    private UtilsClass.TimeQueue<GameNode> m_del_queue;
    private UtilsClass.Callback m_del_queue_cb;

    //--------------------------------------------------------------------------
    public LevelTrashBin(String name) {
        super(name, 1.0f);
        m_del_queue = new UtilsClass.TimeQueue<GameNode>();
    }

    //--------------------------------------------------------------------------
    public void Put(final GameNode node, boolean delete_instantly) {
        // Move node to trash bin
        SgRelocateChild(node);

        // Notify object that it's about to be deleted
        long delay = node.OnPreDelete(delete_instantly);

        // Delete now
        if(delete_instantly) {
            Main.inst.sg_man.ScheduleForDeletion(node);

        // Delete later
        } else {
            m_del_queue.Put(node, Main.inst.timer.GetCur() + delay);
        }
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        if(m_del_queue_cb == null) {
            m_del_queue_cb = new UtilsClass.Callback() {
                @Override public Object Run(Object... args) {
                    Main.inst.sg_man.ScheduleForDeletion((GameNode)args[0]);
                    return null;
                }
            };
        }
        m_del_queue.Run(Main.inst.timer.GetCur(), m_del_queue_cb);
        return true;
    } 
}
