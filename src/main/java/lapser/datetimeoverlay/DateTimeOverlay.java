package lapser.datetimeoverlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * Adds a text overlay showing when in the timelapse we are
 */
public class DateTimeOverlay {

    /**
     * Where the overlay should be positioned
     */
    public static enum OverlayPosition {
        NONE,
        TOPLEFT,
        TOPRIGHT,
        TOPCENTER,
        BOTTOMLEFT,
        BOTTOMRIGHT,
        BOTTOMCENTER
    }

    /**
     * Where should we be putting the overlay?
     */
    private OverlayPosition overlayPosition = OverlayPosition.NONE;

    /**
     * The start date
     */
    private Date startDate;

    /**
     * The end date
     */
    private Date endDate;

    /**
     * The final frames per second
     */
    private int fps;

    /**
     * Set up an overlay for date time
     * @param pos
     */
    public DateTimeOverlay(OverlayPosition pos, Date startDate, Date endDate, int fps) {
        this.overlayPosition = pos;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fps = fps;
    }

    /**
     * Draw the overlay
     * @param src
     * @param timeIndex
     * @return
     */
    public BufferedImage generateOverlay(BufferedImage src, Date timeIndex) {
        if (overlayPosition != OverlayPosition.NONE) {
            Graphics graphics = src.getGraphics();
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.fillRect(0, 0, 200, 50);
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Arial Black", Font.BOLD, 20));
            graphics.drawString("Here is some sample text", 10, 25);
            graphics.dispose();
        }
        return src;
    }
}
