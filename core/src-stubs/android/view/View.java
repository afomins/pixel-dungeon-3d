package android.view;

public class View {

    public interface OnTouchListener {

        boolean onTouch(View view, MotionEvent event);
    }

    public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 0;
    public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 0;
    public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 0;
    public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 0;
    public static final int SYSTEM_UI_FLAG_FULLSCREEN = 0;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 0;
    
    public static View stub = new View();

    public void setSystemUiVisibility(int i) {
        // TODO Auto-generated method stub
        
    }

}
