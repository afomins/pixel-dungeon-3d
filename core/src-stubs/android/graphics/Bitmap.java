package android.graphics;

public class Bitmap {
    public static class Config {
        public static final String ARGB_8888 = null;
    }

    public static Bitmap stub = new Bitmap();

    public static Bitmap createBitmap(int length, int i, String argb8888) {
        return stub;
    }

    public void setPixel(int i, int j, int k) {
        // TODO Auto-generated method stub
        
    }

    public int getWidth() {
        return 256;
    }

    public int getHeight() {
        return 256;
    }

    public void recycle() {
        // TODO Auto-generated method stub
        
    }

    public void eraseColor(int color) {
        // TODO Auto-generated method stub
        
    }

    public void getPixels(int[] pixels, int i, int w, int j, int k, int w2, int h) {
        // TODO Auto-generated method stub
        
    }

    public int getPixel(int pos, int j) {
        return 0xffffffff;
    }

}
