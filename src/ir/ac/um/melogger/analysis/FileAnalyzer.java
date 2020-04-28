package ir.ac.um.melogger.analysis;

import java.io.File;

public class FileAnalyzer {
    public File findManifestFile(File sourceDirectory) {
        File manifestFile = new File(sourceDirectory, "main" + File.separatorChar + "AndroidManifest.xml");
        if (manifestFile.exists() && manifestFile.isFile()) {
            return manifestFile;
        } else {
            return null;
        }
    }


    public File findSourceDirectory(File directory) {
        File[] innerFiles = directory.listFiles();
        File sourceDirectory = null;
        //use a breadth-first search
        if (innerFiles != null) {
            for (File innerFile : innerFiles) {
                if (innerFile.isDirectory()) {
                    if (innerFile.getName().equals("src")) {
                        sourceDirectory = innerFile;
                        break;
                    }
                }
            }

            if (sourceDirectory == null) {
                //now depth-first search
                for (File innerFile : innerFiles) {
                    if (innerFile.isDirectory()) {
                        File result = findSourceDirectory(innerFile);
                        if (result != null) {
                            sourceDirectory = result;
                            break;
                        }
                    }
                }
            }
        }
        return sourceDirectory;
    }

}
