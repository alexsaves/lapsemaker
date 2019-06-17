package lapser.img;

import lapser.LapseImgSrc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Blends images
 */
public class ImageBlender {
    /**
     * Create an image which is a blend of two other images
     *
     * @param img1
     * @param img2
     * @param timeIndex
     * @return
     */
    public static BufferedImage BlendImages(LapseImgSrc img1, LapseImgSrc img2, Long timeIndex) throws RuntimeException {
        ImageIO.setUseCache(false);

        // Compute the fraction progress
        Long msProg = (Long) (timeIndex - img1.takenAt.getTime());
        Long msDuration = img2.takenAt.getTime() - img1.takenAt.getTime();
        Double timeProg = msProg.doubleValue() / msDuration.doubleValue();

        // Load the images
        BufferedImage bimg1 = img1.getImage();
        BufferedImage bimg2 = img2.getImage();

        int imageWidth = bimg1.getWidth();
        int imageHeight = bimg1.getHeight();

        BufferedImage intermediateImg = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                ImagePixel bx1 = ImagePixel.getPixel(bimg1, x, y);
                ImagePixel bx2 = ImagePixel.getPixel(bimg2, x, y);
                ImagePixel blended = ImagePixel.Blend(bx1, bx2, timeProg);
                intermediateImg.setRGB(x, y, blended.getValue());
            }
        }

        return intermediateImg;
    }
}
