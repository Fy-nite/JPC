package org.finite.Language.Backend;

/*
    * The BackendSearcher class is responsible for searching for and loading
    * backend compilers for the Micro-Assembly language.
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;


public class BackendSearcher {
    // class array to hold the backend compilers
    public Class<?>[] backends;


    // constructor to initialize the backends array
    public BackendSearcher() {
        backends = new Class<?>[100]; // let's say we have 100 backends, how in hell does that happen but ok.
    }

    // method to search for and load the backend compilers
    public void searchForBackends() {
        long index = 0; // index to keep track of the number of backends found
        // get the classpath
        String classpath = System.getProperty("java.class.path");
        // split the classpath into directories
        String[] directories = classpath.split(File.pathSeparator);
        // loop through the directories
        for (String directory : directories) {
            // create a file object for the directory
            File dir = new File(directory);
            // check if the directory exists and is a directory
            if (dir.exists() && dir.isDirectory()) {
                // loop through the files in the directory
                for (File file : dir.listFiles()) {

                    // check if the file is a class file
                    if (file.getName().endsWith(".class")) {
                        try {
                            // load the class using reflection
                            URLClassLoader loader = new URLClassLoader(new URL[]{dir.toURI().toURL()});
                            Class<?> clazz = loader.loadClass(file.getName().replace(".class", ""));
                            // check if the class has the MASMBackend annotation
                            if (clazz.isAnnotationPresent(MASMBackend.class)) {
                                backends[(int) index] = clazz; // add the class to the backends array
                                index++; // increment the index
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


}
