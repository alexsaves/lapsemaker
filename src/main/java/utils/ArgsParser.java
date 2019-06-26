package utils;

import lapser.datetimeoverlay.DateTimeOverlay;
import lapser.img.ImgType;

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
     * Where the overlay should be positioned
     */
    public DateTimeOverlay.OverlayPosition overlayPosition = DateTimeOverlay.OverlayPosition.NONE;

    /**
     * Frames per second for the final video
     */
    public int fps = 10;

    /**
     * The encoding type
     */
    public int imgType = ImgType.JPEG;

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
        if (_args.length == 7) {
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
                            String imgTpeStr = _args[5].toUpperCase();
                            switch (imgTpeStr) {
                                case "JPG":
                                    imgType = ImgType.JPEG;
                                    break;
                                case "JPEG":
                                    imgType = ImgType.JPEG;
                                    break;
                                case "PNG":
                                    imgType = ImgType.PNG;
                                    break;
                            }
                            String overlaypos = _args[6].toUpperCase().trim();
                            switch (overlaypos) {
                                case "NONE":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.NONE;
                                    break;
                                case "BOTTOMCENTER":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.BOTTOMCENTER;
                                    break;
                                case "BOTTOMLEFT":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.BOTTOMLEFT;
                                    break;
                                case "BOTTOMRIGHT":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.BOTTOMRIGHT;
                                    break;
                                case "TOPCENTER":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.TOPCENTER;
                                    break;
                                case "TOPLEFT":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.TOPLEFT;
                                    break;
                                case "TOPRIGHT":
                                    overlayPosition = DateTimeOverlay.OverlayPosition.TOPRIGHT;
                                    break;
                            }
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
