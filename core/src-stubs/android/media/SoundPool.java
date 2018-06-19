//------------------------------------------------------------------------------
package android.media;

//------------------------------------------------------------------------------
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import android.content.res.AssetFileDescriptor;

//------------------------------------------------------------------------------
public class SoundPool {
    //**************************************************************************
    // OnLoadCompleteListener
    //**************************************************************************
    public interface OnLoadCompleteListener {
        void onLoadComplete(SoundPool soundPool, int sampleId, int status);
    }

    //**************************************************************************
    // SoundPool
    //**************************************************************************
    private HashMap<Integer, Sound> m_sounds;
    private OnLoadCompleteListener m_load_complete_listener;

    //--------------------------------------------------------------------------
    public SoundPool(int maxStreams, int streamMusic, int i) {
        m_sounds = new HashMap<Integer, Sound>();
    }

    //--------------------------------------------------------------------------
    public void release() {
        for(Sound sound : m_sounds.values()) {
            sound.dispose();
        }
        m_sounds.clear();
        m_sounds = null;
    }

    //--------------------------------------------------------------------------
    public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
        m_load_complete_listener = listener;
    }

    //--------------------------------------------------------------------------
    public void autoPause() {
        Logger.d("SOUND - pause all");
        for(Sound sound : m_sounds.values()) {
            sound.pause();
        }
    }

    //--------------------------------------------------------------------------
    public void autoResume() {
        Logger.d("SOUND - resume all");
        for(Sound sound : m_sounds.values()) {
            sound.resume();
        }
    }

    //--------------------------------------------------------------------------
    public int load(AssetFileDescriptor fd, int priority) {
        int id = m_sounds.size();
        FileHandle handle = (FileHandle)fd.getFileDescriptor();
        Logger.d("SOUND - loading :: id=%d path=%s", id, handle.path());

        try {
            m_sounds.put(id, Gdx.audio.newSound(handle));
            m_load_complete_listener.onLoadComplete(this, id, 0);
        } catch(Exception ex) {
            Utils.LogException(ex, "Failed to load sound");
            throw ex;
        }
        return id;
    }

    //--------------------------------------------------------------------------
    public void unload(Integer id) {
        if(m_sounds.containsKey(id)) {
            Logger.d("SOUND - unloading :: id=%d", id);
            m_sounds.remove(id).dispose();
        } else {
            Logger.e("SOUND - failed to unload wrong id :: id=%d", id);
        }
    }

    //--------------------------------------------------------------------------
    public int play(Integer id, float leftVolume, float rightVolume, 
      int priority, int loop, float rate) {
        if(m_sounds.containsKey(id)) {
            Logger.d("SOUND - play :: id=%d volume=%f rate=%f", id, leftVolume, rate);
            Sound sound = m_sounds.get(id);
            sound.play(leftVolume);
            return id;
        } else {
            Logger.e("SOUND - failed to play wrong id :: id=%d", id);
            return 0;
        }
    }
}
