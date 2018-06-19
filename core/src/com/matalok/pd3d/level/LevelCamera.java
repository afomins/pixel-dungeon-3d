//------------------------------------------------------------------------------
package com.matalok.pd3d.level;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.Tweener.Vector3Accessor;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.pd3d.shared.UtilsClass.FloatFollower;

//------------------------------------------------------------------------------
public class LevelCamera
  extends Camera {
    //**************************************************************************
    // Vector3Follower
    //**************************************************************************
    public static class Vector3Follower {
        //----------------------------------------------------------------------
        private FloatFollower[] m_ff_xyz;
        private UtilsClass.FFloat[] m_target_xyz;
        protected Vector3 m_target;
        protected Vector3 m_value;

        //----------------------------------------------------------------------
        public Vector3Follower() {
            m_ff_xyz = new FloatFollower[] {
                new FloatFollower(), new FloatFollower(), new FloatFollower()
            };
            m_target_xyz = new UtilsClass.FFloat[] {
                new UtilsClass.FFloat(), new UtilsClass.FFloat(), new UtilsClass.FFloat()
            };
            m_value = new Vector3();
        }

        //----------------------------------------------------------------------
        public void SetTarget(Vector3 target, float speed) {
            m_target = target;
            m_ff_xyz[0].SetTarget(m_target_xyz[0], speed);
            m_ff_xyz[1].SetTarget(m_target_xyz[1], speed);
            m_ff_xyz[2].SetTarget(m_target_xyz[2], speed);
        }

        //----------------------------------------------------------------------
        public Vector3 Update(float delta) {
            m_target_xyz[0].v = m_target.x;
            m_target_xyz[1].v = m_target.y;
            m_target_xyz[2].v = m_target.z;

            return m_value.set(
              m_ff_xyz[0].Update(delta), 
              m_ff_xyz[1].Update(delta), 
              m_ff_xyz[2].Update(delta));
        }
    }

    //**************************************************************************
    // Param
    //**************************************************************************
    public static class Param {
        //----------------------------------------------------------------------
        public float distance;
        public float angle_v;
        public float fov;
        public Vector3 pos;
        public Vector3 look_at;
        public Vector3 up[];
        public boolean do_target_rot;
        public Float target_follower_speed;

        //----------------------------------------------------------------------
        public Param(float _angle_v, float _distance, float _fov, Vector3 _pos_offset, 
          Vector3 _look_at_pos, float _shake_angle, boolean _do_target_rot, 
          Float _target_follower_speed) {
            distance = _distance;
            angle_v = _angle_v;
            fov = _fov;
            pos = new Vector3(_pos_offset);
            look_at = new Vector3(_look_at_pos);
            do_target_rot = _do_target_rot;
            target_follower_speed = _target_follower_speed;

            // Up angles when shaking
            up = (_shake_angle == 0.0f) ? 
              new Vector3[] { Vector3.Y } :
              new Vector3[] { new Vector3(Vector3.Y).rotate(Vector3.Z, - _shake_angle),
                              new Vector3(Vector3.Y).rotate(Vector3.Z, + _shake_angle)};
        }
    }
    private static final Param PARAM[] = new Param[] {
        // First-person camera
        new Param(0.0f,     0.0f,   090.0f, new Vector3(0.0f, 0.6f, 0.0f), 
                                            new Vector3(0.0f, 0.0f, 0.0f), 1.0f, true, null),

        // Shoulder camera portrait
        new Param(40.0f,    0.34f,  066.0f, new Vector3(-0.5f, 0.6f, 1.1f), 
                                            new Vector3(0.0f, -2.5f, -0.1f), 0.0f, true, 2.5f),

        // Shoulder camera landscape
        new Param(40.0f,    0.16f,  065.0f,  new Vector3(-0.5f, 0.6f, 1.1f), 
                                             new Vector3(0.0f, -0.6f, -1.0f), 0.0f, true, 3.5f),

        // Top-down camera #1 
        new Param(90.0f,    0.4f,   100.0f, new Vector3(0.0f, 0.0f, 0.0f), 
                                            new Vector3(0.0f, 0.0f, 0.0f), 0.0f, false, null),

        // Top-down camera #2
        new Param(90.0f,    0.6f,   100.0f, new Vector3(0.0f, 0.0f, 0.0f), 
                                            new Vector3(0.0f, 0.0f, 0.0f), 0.0f, false, null),
    };

    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static Vector3 INIT_UP          = new Vector3( 0.0f, 1.0f,  0.0f);
    private static Vector3 INIT_DIR         = new Vector3( 0.0f, 0.0f, -1.0f);
    private static Vector3 ROTATE_V_AXIS    = new Vector3(-1.0f, 0.0f,  0.0f);
    private static Vector3 ROTATE_H_AXIS    = new Vector3( 0.0f, 1.0f,  0.0f);
    private static float MAX_DISTANCE       = 20.0f;

    //**************************************************************************
    // LevelCamera
    //**************************************************************************
    private GameNode m_target;

    private Vector3 m_pos, m_look_at, m_up;
    private UtilsClass.FFloat m_distance, m_angle_v, m_fov;
    private int m_camera_idx, m_up_idx;
    private Param m_camera_param[];

    private float m_shake_magnitude;
    private long m_shake_start, m_shake_duration;

    private UtilsClass.SimpleTween m_circular_rot_tween;
    private UtilsClass.FFloat m_target_rot_h;
    private UtilsClass.AngleFollower m_rot_follower_h;
    private Vector3Follower m_pos_follower;
    private Float m_target_follower_speed;

    private Vector3 m_sway_offset;
    private Tweener.Callback m_sway_cb;

    //--------------------------------------------------------------------------
    public LevelCamera() {
        super();

        m_camera_idx = 0;
        m_distance = new UtilsClass.FFloat();
        m_angle_v = new UtilsClass.FFloat();
        m_fov = new UtilsClass.FFloat();
        m_pos = new Vector3();
        m_look_at = new Vector3();
        m_up = new Vector3(Vector3.Y);
        m_camera_param = PARAM;
        m_target_rot_h = new UtilsClass.FFloat();
        m_rot_follower_h = new UtilsClass.AngleFollower();
        m_pos_follower = new Vector3Follower();

        // Start swaying
        m_sway_offset = new Vector3();
        m_sway_cb = new Tweener.Callback(this) {
            @Override public void OnComplete() {
                StartSwaying();
            }
        };
        m_sway_cb.OnComplete();

        SwitchCamera(+1);
    }

    //--------------------------------------------------------------------------
    public void SetTarget(GameNode target) {
        m_target = target;

        if(m_target_follower_speed != null) {
            m_pos_follower.SetTarget(target.GetGlobalPos(), m_target_follower_speed);
        }
    }

    //--------------------------------------------------------------------------
    public void StartSwaying() {
        float max = Main.inst.cfg.camera_sway_max;
        Vector3 dest = new Vector3(MathUtils.random(-max, +max), 
          MathUtils.random(-max, +max), MathUtils.random(-max, +max));
        float distance = Vector3.dst(
          m_sway_offset.x, m_sway_offset.y, m_sway_offset.z, dest.x, dest.y, dest.z);
        float duration = distance * Main.inst.cfg.camera_sway_speed;
        m_tweener.Start(
          m_sway_offset, null, Vector3Accessor.AXIS_XY, duration, null, m_sway_cb, dest);
    }

    //--------------------------------------------------------------------------
    public void StartCircleRotation(boolean is_forced) {
        if(m_circular_rot_tween != null) {
            if(!is_forced) {
                return;
            }
        } else {
            m_circular_rot_tween = new UtilsClass.SimpleTween();
        }

        float start_val = m_circular_rot_tween.GetValue();
        m_circular_rot_tween.Start(
          Main.inst.timer.GetCurSec(), Main.inst.cfg.camera_circular_360_rot_duration, 
          start_val, start_val + 360.0f);
    }

    //--------------------------------------------------------------------------
    public void StopCircleRotation() {
        m_circular_rot_tween = null;
    }

    //--------------------------------------------------------------------------
    public void SwitchCamera(int dir) {
        // Select next camera index
        m_camera_idx = MathUtils.clamp(m_camera_idx + dir, 0, m_camera_param.length - 1);
        Param param = m_camera_param[m_camera_idx];

        // Tween camera parameters
        float tween_duration = 0.5f;

        // Distance from camera to target
        m_tweener.Start(
          m_distance, null, 0, tween_duration, null, null, param.distance * MAX_DISTANCE);

        // Vertical rotation
        m_tweener.Start(
          m_angle_v, null, 0, tween_duration, null, null, param.angle_v);

        // Field of view
        m_tweener.Start(
          m_fov, null, 0, tween_duration, null, null, param.fov);

        // Position
        m_tweener.Start(
          m_pos, null, Vector3Accessor.AXIS_XYZ, tween_duration, null, null, param.pos);

        // Look-at position
        m_tweener.Start(
          m_look_at, null, Vector3Accessor.AXIS_XYZ, tween_duration, null, null, param.look_at);

        // Horizontal rotation
        UtilsClass.FFloat follow_rot_h = param.do_target_rot ? 
          m_target_rot_h :                  // Follow horizontal rotation of the target
          new UtilsClass.FFloat(0.0f);      // Do not rotate horizontally
        m_rot_follower_h.SetTarget(follow_rot_h, Main.inst.cfg.camera_rotate_follower_speed);

        // Following speed 
        m_target_follower_speed = param.target_follower_speed;
        SwingCamera();
    }

    //--------------------------------------------------------------------------
    public void SwingCamera() {
        if(m_camera_param[m_camera_idx].up.length <= 1) {
            return;
        }
        m_up_idx = (m_up_idx + 1) % m_camera_param[m_camera_idx].up.length;

        Vector3 swing_vectors[] = new Vector3[] {
          m_camera_param[m_camera_idx].up[m_up_idx], Vector3.Y};
        m_tweener.StartTimeline(m_up, null, Tweener.Vector3Accessor.AXIS_XYZ, 
          Main.inst.cfg.camera_swing_duration, null, swing_vectors);
    }

    //--------------------------------------------------------------------------
    public void ShakeCamera(float magnitude, long duration) {
        m_shake_magnitude = magnitude;
        m_shake_start = Main.inst.timer.GetCur();
        m_shake_duration = duration;
    }

    //--------------------------------------------------------------------------
    public boolean IsFirstPerson() {
        return (m_camera_idx == 0);
    }

    //--------------------------------------------------------------------------
    public Param GetParam() {
        return m_camera_param[m_camera_idx];
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Register self as level camera
        ctx.SetCamera(Renderer.CameraType.LEVEL, this);

        // Default static camera without target
        if(m_target == null) {
            m_gdx_camera.up.set(INIT_UP);
            m_gdx_camera.position.set(-20.0f, 5.0f, -20.0f);
            m_gdx_camera.lookAt(0.0f, 0.0f, 0.0f);

        // Camera rotating with target
        } else {
            // UP vector
            m_gdx_camera.up.set(m_up);

            // Direction vector
            m_gdx_camera.direction.set(INIT_DIR);

            // Fov
            m_gdx_camera.fieldOfView = m_fov.v;

            // Camera position
            m_gdx_camera.position.set(m_pos);
            m_gdx_camera.position.z += m_distance.v;

            // Shake
            long shake_time = Main.inst.timer.GetCur() - m_shake_start;
            if(shake_time > 0 && shake_time <= m_shake_duration) {
                float shake_prc = 1.0f - (float)m_shake_duration / shake_time;
                float magnitude = m_shake_magnitude * shake_prc;
                m_gdx_camera.position.add(
                  (MathUtils.random() - 0.5f) * magnitude,
                  (MathUtils.random() - 0.5f) * magnitude,
                  (MathUtils.random() - 0.5f) * magnitude);
            }

            // Sway
            m_gdx_camera.position.add(m_sway_offset);

            // Rotate vertically 
            m_gdx_camera.rotateAround(m_look_at, ROTATE_V_AXIS, m_angle_v.v);

            // Update horizontal rotation of the target
            m_target_rot_h.v = m_target.GetLocalRot().getAngleAround(ROTATE_H_AXIS) + 
              Main.inst.cfg.camera_horizontal_tilt - 90.0f;

            // Rotate horizontally
            float h_rot_angle = 
              m_rot_follower_h.Update(Main.inst.timer.GetDeltaSec());

            // Apply circle rotation
            float h_rot_circle = 0.0f; 
            if(m_circular_rot_tween != null) {
                if(m_circular_rot_tween.Update(Main.inst.timer.GetCurSec())) {
                    StartCircleRotation(true); // Restart rotation when finished
                }
                h_rot_circle = m_circular_rot_tween.GetValue();
            }
            m_gdx_camera.rotateAround(Vector3.Zero, ROTATE_H_AXIS, h_rot_angle + h_rot_circle);

            // Move to target
            Vector3 target_pos = (m_target_follower_speed != null) ? 
              m_pos_follower.Update(Main.inst.timer.GetDeltaSec()) : m_target.GetGlobalPos();

            float offset_y = IsFirstPerson() ? target_pos.y : 0.0f;
            m_gdx_camera.translate(target_pos.x, offset_y, target_pos.z);

            // Calculate drawing distance relative to target 
            Vector3 cam_pos = m_gdx_camera.position;
            m_gdx_camera.far = Main.inst.cfg.camera_draw_distance + 
              Vector3.dst(cam_pos.x, cam_pos.y, cam_pos.z, 
                target_pos.x, target_pos.y, target_pos.z);
        }

        // Update camera matrix
        m_gdx_camera.update();
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodResize(int width, int height) {
        super.OnMethodResize(width, height);

        // Select optimized cameras for portrait & landscape views
        boolean is_portrait = (height > width);
        if(m_camera_idx == 1 && !is_portrait) {
            SwitchCamera(+1);

        } else if(m_camera_idx == 2 && is_portrait) {
            SwitchCamera(-1);
        }
        return true;
    }

}
