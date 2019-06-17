package lapser;

import com.drew.metadata.Metadata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class LapseImgSrc implements Comparable<LapseImgSrc> {
    /**
     * The raw file src
     */
    public File file;

    /**
     * When the image was taken
     */
    public Date takenAt;

    /**
     * The EXIF data
     */
    public Metadata metadata;

    /**
     * Holds the image
     */
    private BufferedImage _image;

    /**
     * Replace with a substitute image
     * @param img
     */
    public void replaceWithImage(BufferedImage img) {
        _image = img;
    }

    /**
     * Get the image
     * @return
     * @throws RuntimeException
     */
    public BufferedImage getImage() throws RuntimeException {
        try {
            return (_image != null) ? _image : ImageIO.read(file);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Free up memory
     */
    public void Dispose() {
        _image = null;
        metadata = null;
    }

    /**
     * Compare two images
     * @param othr
     * @return
     */
    @Override
    public int compareTo(LapseImgSrc othr) {
        return takenAt.compareTo(othr.takenAt);
    }

    /**
     * Stringify
     * @return
     */
    @Override
    public String toString() {
        return takenAt.toString();
    }
}
