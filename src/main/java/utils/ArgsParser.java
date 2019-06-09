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
     * Set up a new args
     * @param args
     */
    public ArgsParser(String[] args) {
        this._args = args;
    }

    /**
     * Parse the args
     */
    public void parse() {
        if (_args.length == 1) {
            File f = new File(_args[0]);
            if (f.exists() && f.isDirectory()) {
                IsValid = true;
                parseFolder = _args[0];
            }
        }
    }
}
