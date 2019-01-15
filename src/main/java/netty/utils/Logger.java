package netty.utils;

public class Logger {

    public static void log(final String msg) {
        System.out.println(translatecolor("[§eLOG" + Color.RESET.code + "]§r " + msg));
    }

    public static void info(final String msg) {
        System.out.println(translatecolor("[§eINFO" + Color.RESET.code + "]§r " + msg));
    }

    public static void debug(final String msg) {
        System.out.println(translatecolor("[§eDEBUG" + Color.RESET.code + "]§r " + msg));
    }

    public static void warn(final String msg) {
        System.out.println(translatecolor("[§eWARN" + Color.RESET.code + "]§r " + msg));
    }

    public static void error(final String msg) {
        System.out.println(translatecolor("[§eERROR" + Color.RESET.code + "]§r " + msg));
    }


    private static String translatecolor(String msg) {

        msg = msg.replace("§0", Color.BLACK.code);

        msg = msg.replace("§4", Color.RED.code);
        msg = msg.replace("§c", Color.RED.code);

        msg = msg.replace("§2", Color.GREEN.code);
        msg = msg.replace("§a", Color.GREEN.code);

        msg = msg.replace("§e", Color.YELLOW.code);
        msg = msg.replace("§6", Color.YELLOW.code);

        msg = msg.replace("§1", Color.BLUE.code);
        msg = msg.replace("§b", Color.BLUE.code);

        msg = msg.replace("§5", Color.MAGENTA.code);
        msg = msg.replace("§d", Color.MAGENTA.code);

        msg = msg.replace("§3", Color.CYAN.code);
        msg = msg.replace("§b",Color.CYAN.code);

        msg = msg.replace("§f", Color.CYAN.code);

        msg = msg.replace("§r", Color.RESET.code);

        msg = msg + Color.RESET.code;

        return msg;
    }

    public enum Color {
        RESET("\033[0m"),

        // Regular Colors
        BLACK("\033[0;30m"),    // BLACK
        RED("\033[0;31m"),      // RED
        GREEN("\033[0;32m"),    // GREEN
        YELLOW("\033[0;33m"),   // YELLOW
        BLUE("\033[0;34m"),     // BLUE
        MAGENTA("\033[0;35m"),  // MAGENTA
        CYAN("\033[0;36m"),     // CYAN
        WHITE("\033[0;37m"),    // WHITE

        // Bold
        BLACK_BOLD("\033[1;30m"),   // BLACK
        RED_BOLD("\033[1;31m"),     // RED
        GREEN_BOLD("\033[1;32m"),   // GREEN
        YELLOW_BOLD("\033[1;33m"),  // YELLOW
        BLUE_BOLD("\033[1;34m"),    // BLUE
        MAGENTA_BOLD("\033[1;35m"), // MAGENTA
        CYAN_BOLD("\033[1;36m"),    // CYAN
        WHITE_BOLD("\033[1;37m"),   // WHITE

        // Underline
        BLACK_UNDERLINED("\033[4;30m"),     // BLACK
        RED_UNDERLINED("\033[4;31m"),       // RED
        GREEN_UNDERLINED("\033[4;32m"),     // GREEN
        YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
        BLUE_UNDERLINED("\033[4;34m"),      // BLUE
        MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
        CYAN_UNDERLINED("\033[4;36m"),      // CYAN
        WHITE_UNDERLINED("\033[4;37m"),     // WHITE

        // Background
        BLACK_BACKGROUND("\033[40m"),   // BLACK
        RED_BACKGROUND("\033[41m"),     // RED
        GREEN_BACKGROUND("\033[42m"),   // GREEN
        YELLOW_BACKGROUND("\033[43m"),  // YELLOW
        BLUE_BACKGROUND("\033[44m"),    // BLUE
        MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
        CYAN_BACKGROUND("\033[46m"),    // CYAN
        WHITE_BACKGROUND("\033[47m"),   // WHITE

        // High Intensity
        BLACK_BRIGHT("\033[0;90m"),     // BLACK
        RED_BRIGHT("\033[0;91m"),       // RED
        GREEN_BRIGHT("\033[0;92m"),     // GREEN
        YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
        BLUE_BRIGHT("\033[0;94m"),      // BLUE
        MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
        CYAN_BRIGHT("\033[0;96m"),      // CYAN
        WHITE_BRIGHT("\033[0;97m"),     // WHITE

        // Bold High Intensity
        BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
        RED_BOLD_BRIGHT("\033[1;91m"),      // RED
        GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
        YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
        BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
        MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
        CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
        WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE

        // High Intensity backgrounds
        BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
        RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
        GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
        YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
        BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
        MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
        CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
        WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE

        private final String code;

        Color(final String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }

    }
}
