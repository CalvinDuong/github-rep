package gitlet;
import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable {
    private final String branchId;
    private final Commit commit;
    private final String branchName;

    public Branch(Commit pointedCommit) {
        this.branchName = "main";
        this.commit = pointedCommit;
        this.branchId = pointedCommit.getId();
        File branchFile = new File(".gitlet/branches/" + this.branchName);
        Utils.writeObject(branchFile, this);
    }

    public Branch(Commit pointedCommit, String branch) {
        this.branchName = branch;
        this.commit = pointedCommit;
        this.branchId = pointedCommit.getId();
        File branchFile = new File(".gitlet/branches/" + this.branchName);
        Utils.writeObject(branchFile, this);
    }

    public String id() {
        return branchId;
    }

    public String returnName() {
        return branchName;
    }

    public Commit getCommit() {
        return commit;
    }
}
