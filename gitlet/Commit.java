package gitlet;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    private final String timestamp;

    private final String parents;

    private final String parent2;

    private final HashMap<String, String> blobs;

    private final String id;

    public Commit() {
        ZonedDateTime current = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss YYYY Z");
        this.message = "initial commit";
        this.timestamp = current.format(formatter);
        this.parents = "";
        this.parent2 = "";
        this.blobs = new HashMap<>();
        this.id = sha1Maker();
    }

    public Commit(String message, String parents, HashMap<String, String> blobs) {
        ZonedDateTime current = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss YYYY Z");
        this.message = message;
        this.timestamp = current.format(formatter);
        this.parents = parents;
        this.parent2 = "";
        this.blobs = blobs;
        this.id = sha1Maker();
    }

    public Commit(String message, String parent1, String parent2, HashMap<String, String> blobs) {
        ZonedDateTime current = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss YYYY Z");
        this.message = message;
        this.timestamp = current.format(formatter);
        this.parents = parent1;
        this.parent2 = parent2;
        this.blobs = blobs;
        this.id = sha1Maker();
    }

    public String getMessage() {
        return this.message;
    }

    public String getDate() {
        return this.timestamp;
    }

    public String returnParents() {
        return this.parents;
    }

    public String getParent2() {
        return this.parent2;
    }

    public HashMap<String, String> returnBlobs() {
        return this.blobs;
    }

    private String sha1Maker() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getId() {
        return this.id;
    }

}
