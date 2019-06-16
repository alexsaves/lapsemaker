import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;
import lapser.LapseProject;
import utils.ArgsParser;

import java.text.DecimalFormat;

public class main {
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
            LapseProject project = new LapseProject(parser.parseFolder);
            try {
                project.parse();
            } catch (RuntimeException ex) {
                logMessage("Parse error: " + ex.getMessage());
            }

        } else {
            terminalPrinter.println(String.format("Usage: %s ../imgs/", ProjectInfo.AppName), Ansi.Attribute.NONE, Ansi.FColor.RED, Ansi.BColor.NONE);
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
}
