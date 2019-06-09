package utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Files {

    /**
     * The list of valid file extensions
     */
    private static final List<String> VALID_IMAGE_EXTENSIONS = Arrays.asList("CR2", "JPG");

    /**
     * Get all the images in a folder
     * @param dir
     * @return
     */
    public static Set<File> getImagesFromFolder(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> {
                    String ext = getFileExtension(file);
                    return VALID_IMAGE_EXTENSIONS.contains(ext);
                })
                .collect(Collectors.toSet());
    }

    /**
     * Get a file extension
     * @param file
     * @return
     */
    private static String getFileExtension(File file) {
        String extension = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf(".") + 1);
            }
        } catch (Exception e) {
            extension = "";
        }

        return extension.toUpperCase();
    }
}
