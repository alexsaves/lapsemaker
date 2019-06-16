package lapser;

import com.drew.metadata.Metadata;

import java.io.File;
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
