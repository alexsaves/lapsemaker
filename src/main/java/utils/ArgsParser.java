package utils;

import java.io.File;

public class ArgsParser {
    /**
     * Raw copy of the args
     */
    private String[] _args;

    /**
     * Is the current state valid?
     */
    public boolean IsValid = false;

    /**
     * Parse the folder
     */
    public String parseFolder;


    /**
     * Target folder
     */
    public String targetFolder;

    /**
     * Where to find FFMPEG
     */
    public String ffmpegLocation;

    /**
     * How many frames to generate
     */
    public int intermediateFrames = 0;

    /**
     * Frames per second for the final video
     */
    public int fps = 10;

    /**
     * Set up a new args
     *
     * @param args
     */
    public ArgsParser(String[] args) {
        this._args = args;
    }

    /**
     * Parse the args
     */
    public void parse() {
        if (_args.length == 5) {
            File f = new File(_args[0]);
            if (f.exists() && f.isDirectory()) {
                parseFolder = _args[0];
                File t = new File(_args[1]);
                if (t.exists() && t.isDirectory()) {
                    targetFolder = _args[1];
                    try {
                        intermediateFrames = Integer.parseInt(_args[2]);
                        ffmpegLocation = _args[3];
                        t = new File(ffmpegLocation);
                        if (t.exists() && t.isDirectory()) {
                            fps = Integer.parseInt(_args[4]);
                            IsValid = true;
                        }
                    } catch (NumberFormatException ex) {
                        IsValid = false;
                    }
                }
            }
        }
    }
}
