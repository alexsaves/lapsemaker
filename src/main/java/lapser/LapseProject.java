package lapser;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lapser.interval.LapseIntervalCalculator;
import utils.EventEmitter;
import utils.Files;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static humanize.Humanize.binaryPrefix;
import static humanize.Humanize.duration;

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
     * Handles log messages
     */
    public EventEmitter<String> OnLog = new EventEmitter<>();

    /**
     * The threshold to use when grouping images that have intervals which are slightly different
     */
    private Long intervalClosenessThresholdSeconds = 1l;

    /**
     * Make a new project
     * @param dir
     */
    public LapseProject(String dir, Long intervalClosenessThresholdSeconds) {
        this.srcDir = dir;
        this.intervalClosenessThresholdSeconds = intervalClosenessThresholdSeconds;
    }

    /**
     * Log a message without elipses
     * @param str
     */
    private void logMessage(String str) {
        this.logMessage(str, false);
    }

    /**
     * Log a message
     * @param str
     */
    private void logMessage(String str, boolean ellipses) {
        this.OnLog.fire(str + (ellipses ? "…" : ""));
    }

    /**
     * Parse the source folder
     * @throws Exception
     */
    public void parse() throws RuntimeException {
        logMessage("Parsing folder " + this.srcDir, true);
        Set<File> _srcFiles = Files.getImagesFromFolder(this.srcDir);

        // Output some basic info about the image set
        DecimalFormat basicIntFormatter = new DecimalFormat("###,###,###,##0");
        Long imgKBytes = _srcFiles.stream().map(fl -> fl.length()).reduce(0l, (x, y) -> x + y) / 1024l;
        logMessage("Found " + basicIntFormatter.format(_srcFiles.size()) + " images (" + binaryPrefix(imgKBytes) + ").");

        // Loop over the set and convert to our special format
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

        // Sort
        Collections.sort(srcImgs);

        // Enforce at least two images to make a video
        if (srcImgs.size() < 2) {
            logMessage("Only " + basicIntFormatter.format(srcImgs.size()) + " images found, which is not enough to make a timelapse.");
            return;
        }

        // How long is this?
        logMessage("Timelapse spans " + duration((srcImgs.get(srcImgs.size() - 1).takenAt.getTime() - srcImgs.get(0).takenAt.getTime()) / 1000) + ".");

        // Try to figure out the likely interval
        LapseIntervalCalculator intervalCalc = new LapseIntervalCalculator(srcImgs);
        Long intervalSeconds = intervalCalc.computeApproximateInterval(intervalClosenessThresholdSeconds);
        logMessage("Interval between frames is probably " + duration(intervalSeconds) + ".");

        // Remove any frames that look like accidental exposures (occurred in less than expected time interval)
        Date currentTime = (Date)srcImgs.get(0).takenAt.clone();
        int accidentalExposures = 0;
        for (int i = 1; i < srcImgs.size(); i++) {
            Long tdiff = (srcImgs.get(i).takenAt.getTime() - currentTime.getTime()) / 1000l;
            if (tdiff - intervalSeconds < -(intervalClosenessThresholdSeconds * 3l)) {
                // Probably accidental exposure - remove it
                srcImgs.remove(i--);
                accidentalExposures++;
            }
            currentTime = (Date)srcImgs.get(i).takenAt.clone();
        }
        if (accidentalExposures > 0) {
            logMessage("Removed " + basicIntFormatter.format(accidentalExposures) + " images due to probable accidental exposure.");
        }

        /*
        We will attempt two techniques for filling missing images.
        1. Use images from similar time in the previous day or subsequent day
        2. Use the closest image in time
         */

        // Now attempt to fill any missing images with images from similar time
        List<String> frameSequence = new ArrayList<>();
        currentTime = (Date)srcImgs.get(0).takenAt.clone();
        for (int i = 1; i < srcImgs.size(); i++) {
            Long tdiff = (srcImgs.get(i).takenAt.getTime() - currentTime.getTime()) / 1000l;
            if ((tdiff - intervalSeconds) > (intervalClosenessThresholdSeconds * 2)) {
                // Missing frame
                frameSequence.add("x");

                // Generate the expected time
                Date newTime = new Date(currentTime.getTime() + (intervalSeconds * 1000l));

                // Set the time
                currentTime = (Date)newTime.clone();

                // Create a new frame
                LapseImgSrc replacementFrame = new LapseImgSrc();
                replacementFrame.takenAt = (Date)currentTime.clone();

                // Stick it in the sequence
                srcImgs.add(i, replacementFrame);
            } else {
                frameSequence.add(".");
                currentTime = (Date)srcImgs.get(i).takenAt.clone();
            }
        }

        // Output the result
        logMessage("Frame sequence: " + frameSequence.stream().reduce("", (x, y) -> x + y));
    }
}
