//------------------------------------------------------------------------------
package android.util;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.TreeSet;

//------------------------------------------------------------------------------
public class SparseArray<T> {
    //--------------------------------------------------------------------------
    // XXX: replace with fast and efficient implementation
    private HashMap<Integer, T> m_hash;

    //--------------------------------------------------------------------------
    public SparseArray() {
        m_hash = new HashMap<Integer, T>();
    }

    //--------------------------------------------------------------------------
    public int size() {
        return m_hash.size();
    }

    //--------------------------------------------------------------------------
    public int keyAt(int idx) {
        return new TreeSet<Integer>(m_hash.keySet())
          .toArray(new Integer[0])[idx];
    }

    //--------------------------------------------------------------------------
    public T valueAt(int idx) {
        int i = 0;
        for(T value : m_hash.values()) {
            if(i++ == idx) {
                return value;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public void put(int key, T val) {
        m_hash.put(key, val);
    }

    //--------------------------------------------------------------------------
    public T get(int key) {
        return m_hash.get(key);
    }

    //--------------------------------------------------------------------------
    public void clear() {
        m_hash.clear();
    }

    //--------------------------------------------------------------------------
    public void remove(int key) {
        delete(key);
    }

    //--------------------------------------------------------------------------
    public void delete(int key) {
        m_hash.remove(key);
    }
}
