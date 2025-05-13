package org.finite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        // Create an instance of the Parser class
        parser parser = new parser();

        boolean debug = false;
        String outputFilePath = "assembly.qbe"; // Default output file name

        // Check if the input file is provided
        if (args.length < 1) {
            System.out.println("Error: No input file provided.");
            return;
        }
        // check for -o
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                if (i + 1 < args.length) {
                    outputFilePath = args[i + 1];
                    i++;
                } else {
                    System.out.println("Error: No output file specified after -o.");
                    return;
                }
            } else if (args[i].equals("-d")) {
                debug = true;
            }
        }
        //read the input file
        String inputFilePath = args[0];
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.out.println("Error: Input file does not exist.");
            return;
        }
        // read the contents of the file
        String[] contents;
        StringBuilder fileContents = new StringBuilder();
        try (Scanner scanner = new Scanner(inputFile)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine()).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: Unable to read the input file.");
            return;
        }
        // Split the contents into lines
        contents = fileContents.toString().split("\n");
        parser.parse(contents);

        compiler.CompileQBE(parser.instructions, outputFilePath);

    }
}
