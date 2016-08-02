package lain;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

/**
 * Created by merlin on 16/7/26.
 */
class ReadLine {
    private static String HISTORY_FILE = null;
    private static Boolean historyLoaded = false;

    static {
        HISTORY_FILE = System.getProperty("user.home") + "/.lain-history";
    }

    static class EOFException extends Exception {
    }

    interface RLLibrary extends Library {
        // Select a library to use.
        // WARNING: GNU readLine is GPL.

        // GNU readLine (GPL)
        RLLibrary INSTANCE = (RLLibrary)
                Native.loadLibrary("readLine", RLLibrary.class);
        // Libedit (BSD)
        //  RLLibrary INSTANCE = (RLLibrary)
        //  Native.loadLibrary("edit", RLLibrary.class);

        String readline(String prompt);

        void add_history(String line);
    }

    private static void loadHistory(String filename) throws Types.LainException {
        try {
            for (String line : Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8)) {
                RLLibrary.INSTANCE.add_history(line);
            }
        } catch (IOException e) {
            throw new Types.LainException("error reading " + filename);
        }
    }

    public static void appendHistory(String filename, String line) {
        try {
            Files.write(Paths.get(filename), Collections.singletonList(line),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException ignored) {
        }
    }

    static String readLine(String prompt)
            throws IOException, EOFException {
        System.out.print(prompt);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();
        if (line == null) {
            throw new EOFException();
        }
        return line;
    }

    public static String jnaReadLine(String prompt)
            throws EOFException, IOException, Types.LainException {
        if (!historyLoaded) {
            loadHistory(HISTORY_FILE);
        }
        String line = RLLibrary.INSTANCE.readline(prompt);
        if (line == null) {
            throw new EOFException();
        }
        RLLibrary.INSTANCE.add_history(line);
        appendHistory(HISTORY_FILE, line);
        return line;
    }
}
