package gitlet;
import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private final byte[] workingFile;

    private final String workingFileString;

    private final String id;

    public Blob(File file) {
        this.workingFile = Utils.readContents(file);
        this.workingFileString = Utils.readContentsAsString(file);
        this.id = sha1Maker();
        File blobFile = new File(".gitlet/blob_area/" + this.id);
        Utils.writeObject(blobFile, this);
    }

    private String sha1Maker() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getId() {
        return this.id;
    }

    public byte[] getFiles() {
        return this.workingFile;
    }

    public String getWorkingFileString() {
        return this.workingFileString;
    }

}
