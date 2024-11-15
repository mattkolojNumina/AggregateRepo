package host;

public class StringUtils {
    public static boolean isNumeric(String str) {
        return str != null && str.matches("[0-9.]+");
    }
}
