//------------------------------------------------------------------------------
package android.media;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class MediaPlayer {
    //**************************************************************************
    // OnErrorListener
    //**************************************************************************
    public interface OnErrorListener {
        boolean onError(MediaPlayer mp, int what, int extra);
    }

    //**************************************************************************
    // OnPreparedListener
    //**************************************************************************
    public interface OnPreparedListener {
        void onPrepared(MediaPlayer player);
    }

    //**************************************************************************
    // MediaPlayer
    //**************************************************************************
    private FileHandle m_handle;
    private String m_path;
    private Music m_music;
    private OnPreparedListener m_prepared_listener;
    private OnErrorListener m_error_listener;

    //--------------------------------------------------------------------------
    private void OnError(int what, int extra) {
        if(m_error_listener != null) {
            m_error_listener.onError(this, what, extra);
        }
    }

    //--------------------------------------------------------------------------
    public void setAudioStreamType(int streamMusic) {
    }

    //--------------------------------------------------------------------------
    public void setDataSource(Object fileDescriptor, Object startOffset, 
      Object length) {
        m_handle = (fileDescriptor instanceof FileHandle) ? 
          (FileHandle)fileDescriptor : null;

        if(m_handle != null) {
            m_path = m_handle.path();
            try {
                m_music = Gdx.audio.newMusic(m_handle);
            } catch(Exception ex) {
                Utils.LogException(ex, "Failed to load music :: path=%s", m_path);
            }
        } else {
            Logger.d("Failed to load music :: path=%s", m_path);
        }

        if(m_music != null) {
            Logger.d("MUSIC - loading :: path=%s", m_path);
        } else {
            OnError(0, 0);
            release();
        }
    }

    //--------------------------------------------------------------------------
    public void setOnPreparedListener(OnPreparedListener listener) {
        m_prepared_listener = listener;
    }

    //--------------------------------------------------------------------------
    public void setOnErrorListener(OnErrorListener listener) {
        m_error_listener = listener;
    }

    //--------------------------------------------------------------------------
    public void setLooping(boolean looping) {
        if(m_music == null) {
            OnError(0, 0);
            return;
        }
        Logger.d("MUSIC - set-looping :: path=%s looping=%s", m_path, looping);
        m_music.setLooping(looping);
    }

    //--------------------------------------------------------------------------
    public void prepareAsync() {
        if(m_music != null && m_prepared_listener != null) {
            m_prepared_listener.onPrepared(this);
        }
    }

    //--------------------------------------------------------------------------
    public void release() {
        if(m_music == null) {
            return;
        }
        m_music.dispose();
        m_music = null;
    }

    //--------------------------------------------------------------------------
    public void start() {
        if(m_music == null) {
            OnError(0, 0);
            return;
        }
        Logger.d("MUSIC - playing :: path=%s", m_path);
        m_music.play();
    }

    //--------------------------------------------------------------------------
    public void pause() {
        if(m_music == null) {
            OnError(0, 0);
            return;
        }
        Logger.d("MUSIC - pausing :: path=%s", m_path);
        m_music.pause();
    }

    //--------------------------------------------------------------------------
    public void stop() {
        if(m_music == null) {
            OnError(0, 0);
            return;
        }
        Logger.d("MUSIC - stopping :: path=%s", m_path);
        m_music.stop();
    }

    //--------------------------------------------------------------------------
    public void setVolume(float value, float value2) {
        if(m_music == null) {
            OnError(0, 0);
            return;
        }
//        Logger.d("MUSIC - setting volume :: path=%s volume=%.1f:%.1f", 
//          m_path, value, value2);
        m_music.setVolume(value);
    }

    //--------------------------------------------------------------------------
    public boolean isPlaying() {
        return (m_music != null && m_music.isPlaying());
    }
}
