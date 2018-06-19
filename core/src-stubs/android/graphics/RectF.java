package android.graphics;

public class RectF {

    public float left;
    public float top;
    public float right;
    public float bottom;

    public RectF(float l, float t, float r, float b) {
        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    public RectF(RectF frame) {
        this(frame.left, frame.top, frame.right, frame.bottom);
    }

    public float width() {
        return right - left;
    }

    public float height() {
        return bottom - top;
    }

    public void offset(float dx, float dy) {
        left += dx;
        right += dx;
        top += dy;
        bottom += dy;
    }

}
