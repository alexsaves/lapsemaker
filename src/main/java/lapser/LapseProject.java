package lapser;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lapser.img.ImageBlender;
import lapser.interval.LapseIntervalCalculator;
import utils.EventEmitter;
import utils.Files;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * The target directory
     */
    private String targDir;

    /**
     * Location of FFMPEG
     */
    private String ffmpegDir;

    /**
     * How many zeroes to use in image filenames
     */
    private static int zerosInFileNames = 10;

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
     * The final video frame rate
     */
    private int fps = 10;

    /**
     * Make a new project
     *
     * @param srcDir
     * @param targDir
     * @param intervalClosenessThresholdSeconds
     */
    public LapseProject(String srcDir, String targDir, String ffmpegDir, int frameRate, Long intervalClosenessThresholdSeconds) {
        this.srcDir = srcDir;
        this.ffmpegDir = ffmpegDir;
        this.targDir = targDir;
        this.fps = frameRate;
        this.intervalClosenessThresholdSeconds = intervalClosenessThresholdSeconds;
    }

    /**
     * Log a message without elipses
     *
     * @param str
     */
    private void logMessage(String str) {
        this.logMessage(str, false);
    }

    /**
     * Log a message
     *
     * @param str
     */
    private void logMessage(String str, boolean ellipses) {
        this.OnLog.fire(str + (ellipses ? "…" : ""));
    }

    /**
     * Confirm that FFMPEG is there
     * @throws RuntimeException
     */
    public boolean validateFFMpegPresense() throws RuntimeException {
        File f = new File(ffmpegDir + "ffmpeg");
        if (f.exists() && f.isFile()) {
            logMessage("FFMpeg found at " + ffmpegDir + ".");
            return true;
        } else {
            throw new RuntimeException("FFMpeg not found at " + ffmpegDir + ".");
        }
    }

    /**
     * Cleanup the target folder
     *
     * @throws RuntimeException
     */
    public void cleanupTargetDir() throws RuntimeException {
        int filesCleanedUp = 0;
        DecimalFormat basicIntFormatter = new DecimalFormat("###,###,###,##0");
        File t = new File(targDir);
        if (t.exists() && t.isDirectory()) {
            for (File file : t.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                    filesCleanedUp++;
                }
            }
            logMessage("Cleaned up " + basicIntFormatter.format(filesCleanedUp) + " files in target folder.");
        } else {
            throw new RuntimeException("Target folder " + targDir + " is missing or not a folder.");
        }
    }

    /**
     * Parse the source folder
     *
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
        Date currentTime = (Date) srcImgs.get(0).takenAt.clone();
        int accidentalExposures = 0;
        for (int i = 1; i < srcImgs.size(); i++) {
            Long tdiff = (srcImgs.get(i).takenAt.getTime() - currentTime.getTime()) / 1000l;
            if (tdiff - intervalSeconds < -(intervalClosenessThresholdSeconds * 3l)) {
                // Probably accidental exposure - remove it
                srcImgs.remove(i--);
                accidentalExposures++;
            }
            currentTime = (Date) srcImgs.get(i).takenAt.clone();
        }
        if (accidentalExposures > 0) {
            logMessage("Removed " + basicIntFormatter.format(accidentalExposures) + " images due to probable accidental exposure.");
        }

        // Look for missing frames
        List<String> frameSequence = new ArrayList<>();
        currentTime = (Date) srcImgs.get(0).takenAt.clone();
        int missingFrames = 0;
        for (int i = 1; i < srcImgs.size(); i++) {
            Long tdiff = (srcImgs.get(i).takenAt.getTime() - currentTime.getTime()) / 1000l;
            if ((tdiff - intervalSeconds) > (intervalClosenessThresholdSeconds * 2)) {
                missingFrames++;

                // Missing frame
                frameSequence.add("x");

                // Generate the expected time
                Date newTime = new Date(currentTime.getTime() + (intervalSeconds * 1000l));

                // Set the time
                currentTime = (Date) newTime.clone();

                // Back up the incrementor
                i--;
            } else {
                frameSequence.add(".");
                currentTime = (Date) srcImgs.get(i).takenAt.clone();
            }
        }

        // Give the user a sense of the missing frames
        logMessage("Frame sequence: " + frameSequence.stream().reduce("", (x, y) -> x + y));

        if (missingFrames > 0) {
            logMessage("Generating " + basicIntFormatter.format(missingFrames) + " missing frames", true);
            // Now attempt to fill any missing images with blends of proximate images
            currentTime = (Date) srcImgs.get(0).takenAt.clone();
            int imagesGenerated = 0;
            LapseImgSrc lastKnownGoodImg = srcImgs.get(0);
            for (int i = 1; i < srcImgs.size(); i++) {
                Long tdiff = (srcImgs.get(i).takenAt.getTime() - currentTime.getTime()) / 1000l;
                if ((tdiff - intervalSeconds) > (intervalClosenessThresholdSeconds * 2)) {
                    // Generate the expected time
                    Date newTime = new Date(currentTime.getTime() + (intervalSeconds * 1000l));

                    // Set the time
                    currentTime = (Date) newTime.clone();

                    // Create a new frame
                    LapseImgSrc replacementFrame = new LapseImgSrc();
                    replacementFrame.takenAt = (Date) currentTime.clone();
                    BufferedImage _img = ImageBlender.BlendImages(lastKnownGoodImg, srcImgs.get(i), currentTime.getTime());
                    replacementFrame.replaceWithImage(_img);
                    logMessage("(" + basicIntFormatter.format(imagesGenerated + 1) + ") Built a missing image.");

                    // Bump the count
                    imagesGenerated++;

                    // Stick it in the sequence
                    srcImgs.add(i, replacementFrame);
                } else {
                    lastKnownGoodImg = srcImgs.get(i);
                    currentTime = (Date) srcImgs.get(i).takenAt.clone();
                }
            }

            // Output the result
            logMessage("Generated " + basicIntFormatter.format(imagesGenerated) + " missing images.");
        }

        // Hint to do garbage collection
        System.gc();
    }

    /**
     * Make the final image set
     *
     * @throws RuntimeException
     */
    public void generateFinalImages() throws RuntimeException {
        int frameCount = 0;
        DecimalFormat basicIntFormatter = new DecimalFormat("###,###,###,##0");

        logMessage("Generating final images in " + this.targDir, true);

        for (int i = 0; i < srcImgs.size(); i++) {
            LapseImgSrc img1 = srcImgs.get(i);
            BufferedImage bimg1 = img1.getImage();
            String finalFilename = getFileNameForFrame(frameCount);
            File outputfile = new File(this.targDir + finalFilename);
            try {
                ImageIO.write(bimg1, "png", outputfile);
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            img1.Dispose();

            logMessage("(" + basicIntFormatter.format(frameCount + 1) + ") Wrote " + finalFilename + ".");
            frameCount++;
        }
    }

    /**
     * Get a generated file name
     *
     * @param frame
     * @return
     */
    private String getFileNameForFrame(int frame) {
        String finalStr = "";
        for (int i = 0; i < zerosInFileNames; i++) {
            finalStr += "0";
        }
        DecimalFormat filenameFormatter = new DecimalFormat(finalStr);
        return filenameFormatter.format(frame) + ".png";
    }

    /**
     * Make the final video
     * @throws RuntimeException
     */
    public void generateFinalVideo() throws RuntimeException {
        logMessage("Generating final video with FFMpeg", true);

        // Get the dimensions
        BufferedImage imgTmp = srcImgs.get(0).getImage();

        // Hint to do garbage collection
        System.gc();

        String[] finalCommand = {"-r", this.fps + "", "-f", "image2", "-s", imgTmp.getWidth() + "x" + imgTmp.getHeight(), "-i", this.targDir + "%" + zerosInFileNames + "d.png", "-vcodec", "libx264", "-crf", "25", "-pix_fmt", "yuv420p", this.targDir + "out.mp4"};
        logMessage("Running FFMpeg with \"" + String.join(" ", finalCommand) + "\"", true);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.inheritIO();
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        processBuilder.directory(new File(s));
        processBuilder.command(ffmpegDir + "ffmpeg",
                finalCommand[0],
                finalCommand[1],
                finalCommand[2],
                finalCommand[3],
                finalCommand[4],
                finalCommand[5],
                finalCommand[6],
                finalCommand[7],
                finalCommand[8],
                finalCommand[9],
                finalCommand[10],
                finalCommand[11],
                finalCommand[12],
                finalCommand[13],
                finalCommand[14]);
        try {
            processBuilder.start();
        } catch(IOException ex) {
            throw new RuntimeException(ex.toString());
        }
    }
}
