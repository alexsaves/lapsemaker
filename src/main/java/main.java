import lapser.LapseProject;
import utils.ArgsParser;

import java.text.DecimalFormat;

public class main {
    public static void main(String[] args) {
        DecimalFormat myFormatter = new DecimalFormat("0.00");
        System.out.println(ProjectInfo.AppName + " " + myFormatter.format(ProjectInfo.AppVer));
        ArgsParser parser = new ArgsParser(args);
        parser.parse();
        if (parser.IsValid) {
            // Everything is good
            LapseProject project = new LapseProject(parser.parseFolder);

        } else {
            System.out.println(String.format("Usage: %s ../imgs/", ProjectInfo.AppName));
            return;
        }
    }
}
