// -----------------------------------------------------------------------------
package com.matalok.pd3d;

// -----------------------------------------------------------------------------
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Utils;

// -----------------------------------------------------------------------------
public class Timer
  extends GameNode {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static long GetMsec() { 
        return Calendar.getInstance().getTimeInMillis(); 
    }

    // *************************************************************************
    // Timer
    // *************************************************************************
    private long m_startup, m_cur, m_delta;
    private float m_cur_sec, m_delta_sec;

    // -------------------------------------------------------------------------
    public Timer() {
        super("timer", 1.0f);
        m_startup = GetMsec();
        UpdateTime();
    }

    // -------------------------------------------------------------------------
    public long GetCur() { return m_cur; }
    public long GetDelta() { return m_delta; }
    public float GetCurSec() { return m_cur_sec; }
    public float GetDeltaSec() { return m_delta_sec; }

    //--------------------------------------------------------------------------
    public String GetTimestamp() {
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        return ft.format(Calendar.getInstance().getTime());
    }

    // -------------------------------------------------------------------------
    private void UpdateTime() {
        long prev = m_cur; 
        m_cur = GetMsec() - m_startup;
        m_cur_sec = Utils.MsecToSec(m_cur);
        m_delta = m_cur - prev;
        m_delta_sec = Utils.MsecToSec(m_delta);
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        UpdateTime();
        return true;
    }
}
