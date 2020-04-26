package ir.ac.um.melogger.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Samad Paydar
 */
public class Utils {
    private static ConsoleView consoleView;

    public static String capitalize(String text) {
        if (text != null && !text.isEmpty()) {
            text = "" + text.toUpperCase().charAt(0) + text.toLowerCase().substring(1);
        }
        return text;
    }

    public static String getTimestamp() {
        String pattern = "yyyy_MM_dd_HH_mm_ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    public static ConsoleView getConsoleView() {
        return Utils.consoleView;
    }

    public static void setConsoleView(ConsoleView consoleView) {
        Utils.consoleView = consoleView;
    }

    public static void showMessage(String message) {
        if (consoleView != null) {
            consoleView.print(String.format("%s%n", message),
                    ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }

    public static void showException(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        showMessage(writer.toString());
    }
}
