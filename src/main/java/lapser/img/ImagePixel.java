package lapser.img;

import java.awt.image.BufferedImage;

public class ImagePixel {
    /**
     * Red
     */
    public int r;

    /**
     * Green
     */
    public int g;

    /**
     * Blue
     */
    public int b;

    /**
     * Get a pixel
     *
     * @param img
     * @param x
     * @param y
     * @return
     */
    public static ImagePixel getPixel(BufferedImage img, int x, int y) {
        int val = img.getRGB(x, y);
        ImagePixel p = new ImagePixel();
        p.r = (val >> 16) & 0xff;
        p.g = (val >> 8) & 0xff;
        p.b = val & 0xff;
        return p;
    }

    /**
     * Get a blended pixel
     *
     * @param p1
     * @param p2
     * @param prog
     * @return
     */
    public static ImagePixel Blend(ImagePixel p1, ImagePixel p2, Double prog) {
        double r = (((double) p2.r - (double) p1.r) * prog) + (double) p1.r;
        double g = (((double) p2.g - (double) p1.g) * prog) + (double) p1.g;
        double b = (((double) p2.b - (double) p1.b) * prog) + (double) p1.b;
        ImagePixel p0 = new ImagePixel();
        p0.r = (int) r;
        p0.g = (int) g;
        p0.b = (int) b;
        return p0;
    }

    /**
     * Get a single integer value
     *
     * @return
     */
    public int getValue() {
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Stringify the pixel
     *
     * @return
     */
    @Override
    public String toString() {
        return r + ", " + g + ", " + b;
    }
}
