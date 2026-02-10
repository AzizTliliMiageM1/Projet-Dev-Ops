package backend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    public void main_shouldPrintRouterResult() {
        // Capture stdout
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            String[] args = new String[] { "createUser", "email=test@example.com", "password=Password1", "pseudo=testuser" };
            Main.main(args);
            String printed = out.toString();
            assertTrue(printed.contains("User created"));
            assertTrue(printed.contains("test@example.com"));
        } finally {
            System.setOut(originalOut);
        }
    }
}
