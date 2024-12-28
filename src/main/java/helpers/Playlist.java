package helpers;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

public class Playlist {

    /**
     * Search a file a get its index
     * @param map playlist
     * @param searchString search query
     * @return file's index
     */
    public static int getSearchedFileIndex(Map<File, String> map, String searchString) {
        ArrayList<File> keys = new ArrayList<>(map.keySet());
        if (searchString == null || searchString.isEmpty()) {
            return -1;
        }
        System.out.println("Keys list size: " + keys.size());
        for (File key : keys) {
            System.out.println("Search string: " + searchString);
            if (key.toString().toLowerCase().contains(searchString.toLowerCase())) {
                System.out.println("Found match: " + key.toString());
                return keys.indexOf(key);
            }
        }
        // Nothing
        return -1;
    }

    /**
     * Search all files in a directory/folder
     * @param directory where to search
     * @param playlist Playlist map[path, name]
     */
    public static void getAllFiles(File directory, Map<File, String> playlist) {
        File[] files = directory.listFiles();

        for (File file : files) {

            // Recursive call to find files inside directories
            if (file.isDirectory()) {
                getAllFiles(file, playlist);
            }
            // if is a media file, add to the full path as the key, and name to display the ListView
            else if (isVideoFile(file.toString()) || isAudioFile(file.toString())) {
                playlist.put(file, file.getName());
            }
        }
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isAudioFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }
}
