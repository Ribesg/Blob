package fr.ribesg.blob.util;

import java.text.DecimalFormat;

/**
 * TODO Convert to Kotlin
 *
 * This Class provides convenient access to all IRC special Strings,
 * like Color codes, formatting codes, and others.
 * <p>
 * Color codes and formatting codes are not defined by any real convention,
 * and may not work on all clients.
 * This API is based on the mIRC character codes for colors and formatting.
 *
 * @author Ribesg
 */
public class Codes {

    // ########## //
    // Formatting //
    // ########## //

    public static final String BOLD          = Codes.get(0x02);
    public static final String ITALIC        = Codes.get(0x1D);
    public static final String STRIKETHROUGH = Codes.get(0x13);
    public static final String UNDERLINE     = Codes.get(0x1F);
    public static final String REVERSE       = Codes.get(0x16);

    // ###### //
    // Colors //
    // ###### //

    public static final String WHITE       = Color.CODE + Color.WHITE;
    public static final String BLACK       = Color.CODE + Color.BLACK;
    public static final String BLUE        = Color.CODE + Color.BLUE;
    public static final String GREEN       = Color.CODE + Color.GREEN;
    public static final String RED         = Color.CODE + Color.RED;
    public static final String BROWN       = Color.CODE + Color.BROWN;
    public static final String PURPLE      = Color.CODE + Color.PURPLE;
    public static final String ORANGE      = Color.CODE + Color.ORANGE;
    public static final String YELLOW      = Color.CODE + Color.YELLOW;
    public static final String LIGHT_GREEN = Color.CODE + Color.LIGHT_GREEN;
    public static final String TEAL        = Color.CODE + Color.TEAL;
    public static final String LIGHT_CYAN  = Color.CODE + Color.LIGHT_CYAN;
    public static final String LIGHT_BLUE  = Color.CODE + Color.LIGHT_BLUE;
    public static final String PINK        = Color.CODE + Color.PINK;
    public static final String GRAY        = Color.CODE + Color.GRAY;
    public static final String LIGHT_GRAY  = Color.CODE + Color.LIGHT_GRAY;

    // ##################### //
    // Reset colors & format //
    // ##################### //

    public static final String RESET = Codes.get(0x0f);

    // ########### //
    // Other codes //
    // ########### //

    /**
     * Space character, used to separate prefix, command and parameters in
     * IRC messages
     */
    public static final String SP = Codes.get(0x20);

    /**
     * Carriage return, used to separate different IRC messages
     */
    public static final String CRLF = Codes.get(0x0D) + Codes.get(0x0A);

    /**
     * ASCII Colon, used as first character of any IRC message and as first
     * character of the trailing parameter
     */
    public static final String COLON = Codes.get(0x3a);

    /**
     * 'Blank' character. Will not appear in clients that supports UTF-8.
     * Used to prevent pinging someone by inserting this character into its
     * name
     */
    public static final String EMPTY = Codes.get(0x200B);

    // ###################### //
    // ## END OF ENUM LIST ## //
    // ###################### //

    /**
     * Transform a char into a String
     *
     * @param charCode the integer code of the char
     *
     * @return a String containing the char
     */
    private static String get(int charCode) {
        return Character.toString((char) charCode);
    }

    /**
     * This enum provides a list of color number, their number being their
     * ordinal
     */
    private static enum Color {
        WHITE,
        BLACK,
        BLUE,
        GREEN,
        RED,
        BROWN,
        PURPLE,
        ORANGE,
        YELLOW,
        LIGHT_GREEN,
        TEAL,
        LIGHT_CYAN,
        LIGHT_BLUE,
        PINK,
        GRAY,
        LIGHT_GRAY;

        /**
         * Code used in color codes. A valid color code is composed of this code
         * + the color number
         */
        private static final String CODE = Codes.get(0x03);

        /**
         * Allows to have 2-chars number, even if it's in the 0-9 range
         * (00-09)
         */
        private static final DecimalFormat FORMAT = new DecimalFormat("00");

        /**
         * @return a String of 2 chars containing the color number
         */
        public String toString() {
            return Color.FORMAT.format(this.ordinal());
        }
    }
}