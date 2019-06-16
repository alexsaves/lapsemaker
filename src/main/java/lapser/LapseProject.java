package lapser;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import utils.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Describes a lapse project
 */
public class LapseProject {
    /**
     * The source directory
     */
    private String srcDir;

    /**
     * The list of source images
     */
    private ArrayList<LapseImgSrc> srcImgs = new ArrayList<>();

    /**
     * Make a new project
     * @param dir
     */
    public LapseProject(String dir) {
        this.srcDir = dir;
    }

    /**
     * Parse the source folder
     * @throws Exception
     */
    public void parse() throws RuntimeException {
        Set<File> _srcFiles = Files.getImagesFromFolder(this.srcDir);
        _srcFiles.forEach(fl -> {
            LapseImgSrc src = new LapseImgSrc();
            src.file = fl;
            try {
                src.metadata = ImageMetadataReader.readMetadata(src.file.getAbsoluteFile());
                ExifSubIFDDirectory directory
                        = src.metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                Date date
                        = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                src.takenAt = date;
                srcImgs.add(src);
            } catch (IOException ex) {
                throw new RuntimeException("Folder missing or files inaccessible");
            } catch (ImageProcessingException ex) {
                throw new RuntimeException("Unable to parse image: " + ex.getMessage());
            }
        });
        Collections.sort(srcImgs);
    }
}
