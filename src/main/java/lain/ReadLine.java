package lain;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by merlin on 16/7/26.
 */
class ReadLine {
    static String readLine(String prompt)
            throws IOException {
        System.out.print(prompt);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();
        if (line == null) {
            throw new EOFException();
        }
        return line;
    }
}
