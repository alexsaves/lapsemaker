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
        Set<File> _srcFiles = Files.getImagesFromFolder(dir);
        _srcFiles.forEach(fl -> {
            LapseImgSrc src = new LapseImgSrc();
            src.file = fl;
            try {
                src.metadata = ImageMetadataReader.readMetadata(fl.getAbsoluteFile());
                ExifSubIFDDirectory directory
                        = src.metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                Date date
                        = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                src.takenAt = date;
                srcImgs.add(src);
            } catch(IOException ex) {
                return;
            } catch(ImageProcessingException ex) {
                return;
            }
        });
        Collections.sort(srcImgs);
        System.out.println("hi");
    }
}
