package main.java.utils;

import java.io.File;
import java.util.Comparator;

/**
 * Simple comparator class used to sort files by creation date.
 */
public class FileDateComparator implements Comparator<File>{
    /**
     * compare method between two file
     * @param o1 file 1
     * @param o2 file 2
     * @return 1 if o1 is older, -1 if younger, 0 otherwise
     */
    public int compare(File o1, File o2) {
        if (o1.lastModified() > o2.lastModified()) {
            return +1;
        } else if (o1.lastModified() < o2.lastModified()) {
            return -1;
        } else {
            return 0;
        }
    }
}
