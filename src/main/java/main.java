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
            LapseProject project = new LapseProject(parser.parseFolder, intervalClosenessThresholdSeconds);
            project.OnLog.register(new EventEmitter.Listener<String>() {
                @Override
                public void onEventFired(EventEmitter emitter, String o) {
                    logMessage(o);
                }
            });
            try {
                project.parse();
            } catch (RuntimeException ex) {
                logError("Parse error: " + ex.getMessage());
            }

        } else {
            terminalPrinter.println(String.format("Usage: %s IMGDIR", ProjectInfo.AppName), Ansi.Attribute.NONE, Ansi.FColor.RED, Ansi.BColor.NONE);
            terminalPrinter.println(String.format("Eg: %s ../imgs/", ProjectInfo.AppName), Ansi.Attribute.NONE, Ansi.FColor.WHITE, Ansi.BColor.NONE);
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
