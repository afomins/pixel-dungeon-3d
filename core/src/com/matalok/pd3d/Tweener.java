//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgUtils.IManaged;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;

//------------------------------------------------------------------------------
public class Tweener 
  implements IManaged {
    //**************************************************************************
    // Callback
    //**************************************************************************
    public static abstract class Callback 
      implements TweenCallback {
        //----------------------------------------------------------------------
        protected Object m_target;

        //----------------------------------------------------------------------
        public Callback(Object target) {
            m_target = target;
        }

        //----------------------------------------------------------------------
        public void onEvent(int type, BaseTween<?> source) {
            if(type == TweenCallback.COMPLETE) {
                OnComplete();
            }
        }

        //----------------------------------------------------------------------
        public abstract void OnComplete();
    }

    //**************************************************************************
    // FloatAccessor
    //**************************************************************************
    public static class FloatAccessor 
      implements TweenAccessor<UtilsClass.FFloat> {
        //---------------------------------------------------------------------
        @Override public int getValues(UtilsClass.FFloat target, int tweenType, 
          float[] returnValues) {
            returnValues[0] = target.v;
            return 1;
        }

        //---------------------------------------------------------------------
        @Override public void setValues(UtilsClass.FFloat target, int tweenType, 
          float[] newValues) {
            target.v = newValues[0];
        }
    }

    //**************************************************************************
    // Vector3Accessor
    //**************************************************************************
    public static class Vector3Accessor 
      implements TweenAccessor<Vector3> {
        //----------------------------------------------------------------------
        public static final int AXIS_X     = 1 << 0;
        public static final int AXIS_Y     = 1 << 1;
        public static final int AXIS_Z     = 1 << 2;
        public static final int AXIS_XY    = AXIS_X | AXIS_Y;
        public static final int AXIS_XZ    = AXIS_X | AXIS_Z;
        public static final int AXIS_YZ    = AXIS_Y | AXIS_Z;
        public static final int AXIS_XYZ   = AXIS_X | AXIS_XY | AXIS_Z;

        //----------------------------------------------------------------------
        @Override public int getValues(Vector3 target, int tweenType, 
          float[] returnValues) {
            int idx = 0;
            if((tweenType & AXIS_X) != 0) returnValues[idx++] = target.x;
            if((tweenType & AXIS_Y) != 0) returnValues[idx++] = target.y;
            if((tweenType & AXIS_Z) != 0) returnValues[idx++] = target.z;
            return idx;
        }

        //----------------------------------------------------------------------
        @Override public void setValues(Vector3 target, int tweenType, 
          float[] newValues) {
            int idx = 0;
            if((tweenType & AXIS_X) != 0) target.x = newValues[idx++];
            if((tweenType & AXIS_Y) != 0) target.y = newValues[idx++];
            if((tweenType & AXIS_Z) != 0) target.z = newValues[idx++];
        }
    }

    //**************************************************************************
    // User data
    //**************************************************************************
    public static class UserData {
        //----------------------------------------------------------------------
        public long start;
        public long stop;
        public long duration;

        //----------------------------------------------------------------------
        public UserData(long duration) {
            this.start = Timer.GetMsec();
            this.stop = start + duration;
            this.duration = duration;
        }

        //----------------------------------------------------------------------
        public long GetTtl() {
            long ttl = stop - Timer.GetMsec();
            return (ttl < 0) ? 0 : ttl; 
        }

        //----------------------------------------------------------------------
        public boolean IsFinished() {
            return (Timer.GetMsec() >= stop);
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static void RegisterClass(Class<?> classs, TweenAccessor<?> accessor) {
        Tween.setCombinedAttributesLimit(4);
        Tween.registerAccessor(classs, accessor);
    }

    //**************************************************************************
    // Tweener
    //**************************************************************************
    private TweenManager m_tween;

    //--------------------------------------------------------------------------
    public Tweener() {
        m_tween = new TweenManager();
    }

    //--------------------------------------------------------------------------
    public void Cancel() {
        m_tween.killAll();
    }

    //--------------------------------------------------------------------------
    public void Finish() {
        // Simulate big time delta that will finish all tweens and call all callbacks
        while(m_tween.getRunningTweensCount() != 0) {
            m_tween.update(100000.0f);
        }
    }

    //--------------------------------------------------------------------------
    private Tween Prepare(Object target, Class<?> target_class, int type, 
      float duration, Integer repeat_yoyo, TweenCallback cb, boolean add_ud) {
        // Begin
        Tween tw = Tween.to(target, type, duration);

        // Custom accessor class 
        if(target_class != null) {
            tw.cast(target_class);
        }

        // Callback
        if(cb != null) {
            tw.setCallback(cb);
        }

        // Repeat yo-yo
        if(repeat_yoyo != null) {
            tw.repeatYoyo(repeat_yoyo, 0.0f);
        }

        // Interpolation function
        tw.ease(aurelienribon.tweenengine.equations.Linear.INOUT);

        // UserData
        if(add_ud) {
            tw.setUserData(new UserData(Utils.SecToMsec(duration)));
        }
        return tw; 
    }

    //--------------------------------------------------------------------------
    public Tweener.UserData Start(Object target, Class<?> target_class, int tween_type, 
      float duration, Integer repeat_yoyo, TweenCallback cb, float ...values) {
        return (UserData)Prepare(target, target_class, tween_type, duration, repeat_yoyo, cb, true)
          .target(values).start(m_tween).getUserData();
    }

    //--------------------------------------------------------------------------
    public Tweener.UserData Start(Object target, Class<?> target_class, int tween_type, 
      float duration, Integer repeat_yoyo, TweenCallback cb, Vector3 value) {
        return (UserData)Start(target, target_class, tween_type, duration, repeat_yoyo, cb, 
          value.x, value.y, value.z);
    }

    //--------------------------------------------------------------------------
    public Tweener.UserData Start(Object target, Class<?> target_class, int tween_type, 
      float duration, Integer repeat_yoyo, TweenCallback cb, Quaternion value) {
        return Start(target, target_class, tween_type, duration, repeat_yoyo, cb, 
          value.x, value.y, value.z, value.w);
    }

    //--------------------------------------------------------------------------
    public Tweener.UserData StartTimeline(Object target, Class<?> target_class, int tween_type, 
      float duration, TweenCallback cb, Float[] values) {
        Timeline timeline = Timeline.createSequence();
        for(int i = 0; i < values.length; i++) {
            TweenCallback timeline_cb = (i == values.length - 1) ? cb : null;
            timeline.push(
              Prepare(target, target_class, tween_type, duration, null, timeline_cb, false)
                .target(values[i]));
        }

        // User data
        timeline.setUserData(new UserData(Utils.SecToMsec(duration * values.length)));
        return (UserData)timeline.start(m_tween).getUserData();
    }

    // -------------------------------------------------------------------------
    public Tweener.UserData StartTimeline(Vector3 target, Class<?> target_class, int tween_type, 
      float duration, TweenCallback cb, Vector3[] values) {
        Timeline timeline = Timeline.createSequence();
        for(int i = 0; i < values.length; i++) {
            TweenCallback timeline_cb = (i == values.length - 1) ? cb : null;
            timeline.push(
              Prepare(target, target_class, tween_type, duration, null, timeline_cb, false)
                .target(values[i].x, values[i].y, values[i].z));
        }

        // User data
        timeline.setUserData(new UserData(Utils.SecToMsec(duration * values.length)));
        return (UserData)timeline.start(m_tween).getUserData();
    }

    //--------------------------------------------------------------------------
    public void Update() {
        m_tween.update(Main.inst.timer.GetDeltaSec());
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        Cancel();
        m_tween = null;
    }
}
