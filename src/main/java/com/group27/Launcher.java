package com.group27;

/**
 * Launcher class for the GreenGrocer application.
 * This is a simple wrapper class that delegates to the Main class.
 * Useful for packaging and deployment scenarios.
 * 
 * @author Group27
 * @version 1.0
 */
public class Launcher {
    /**
     * Entry point that delegates to Main.main().
     * 
     * @param args Command line arguments passed to Main
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
