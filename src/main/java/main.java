import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import lapser.LapseProject;
import utils.ArgsParser;
import utils.EventEmitter;

import java.text.DecimalFormat;

public class main {

    /**
     * The threshold to use when grouping images that have intervals which are slightly different
     */
    private static final Long intervalClosenessThresholdSeconds = 2l;

    /**
     * Program entry point
     * @param args
     */
    public static void main(String[] args) {
        DecimalFormat myFormatter = new DecimalFormat("0.00");
        terminalPrinter.println(ProjectInfo.AppName + " " + myFormatter.format(ProjectInfo.AppVer), Ansi.Attribute.NONE, Ansi.FColor.MAGENTA, Ansi.BColor.NONE);
        ArgsParser parser = new ArgsParser(args);
        parser.parse();
        if (parser.IsValid) {
            // Everything is good
            LapseProject project = new LapseProject(parser.parseFolder, parser.targetFolder, parser.ffmpegLocation, parser.fps, parser.intermediateFrames, intervalClosenessThresholdSeconds);
            project.OnLog.register(new EventEmitter.Listener<String>() {
                @Override
                public void onEventFired(EventEmitter emitter, String o) {
                    logMessage(o);
                }
            });

            // Confirm presence of FFMpeg
            try {
                project.validateFFMpegPresense();
            } catch (RuntimeException ex) {
                logError("Setup error: " + ex.getMessage());
                return;
            }

            // Clean up the target folder
            try {
                project.cleanupTargetDir();
            } catch (RuntimeException ex) {
                logError("Cleanup error: " + ex.getMessage());
                return;
            }

            // Parse the folder and fix basic problems
            try {
                project.parse();
            } catch (RuntimeException ex) {
                logError("Parse error: " + ex.getMessage());
                return;
            }

            // Generate the final set of images
            try {
                project.generateFinalImages();
            } catch (RuntimeException ex) {
                logError("Generation error: " + ex.getMessage());
                return;
            }

            // Generate final video
            try {
                project.generateFinalVideo();
            } catch (RuntimeException ex) {
                logError("Video generation error: " + ex.getMessage());
                return;
            }

        } else {
            terminalPrinter.println(String.format("Usage: %s [IMGDIR] [OUTDIR] [INTERMEDIATEFRAMESCOUNT] [FFMPEGLOCATION] [FPS]", ProjectInfo.AppName), Ansi.Attribute.NONE, Ansi.FColor.RED, Ansi.BColor.NONE);
            terminalPrinter.println(String.format("Eg: %s ../imgs/ ../out/ 2 /usr/bin/ffmpeg 10", ProjectInfo.AppName), Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE);
            return;
        }
    }

    /**
     * Handles ANSI terminal printing
     */
    private static ColoredPrinter terminalPrinter = new ColoredPrinter.Builder(1, false).build();

    /**
     * Log a message
     * @param msg
     */
    private static void logMessage(String msg) {
        terminalPrinter.print("[" + terminalPrinter.getDateFormatted() + "] ", Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE);
        terminalPrinter.println(msg, Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.NONE);
        terminalPrinter.clear();
    }

    /**
     * Log an error
     * @param msg
     */
    private static void logError(String msg) {
        terminalPrinter.print("[" + terminalPrinter.getDateFormatted() + "] ", Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE);
        terminalPrinter.println(msg, Ansi.Attribute.NONE, Ansi.FColor.RED, Ansi.BColor.NONE);
        terminalPrinter.clear();
    }
}
