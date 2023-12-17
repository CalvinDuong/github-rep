package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  @author Calvin Duong and Vivi Thai
 */
public class Repository implements Serializable {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The Commit directory. */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");

    /** The Staging Area directory. */
    public static final File STAGING_AREA = join(GITLET_DIR, "staging_area");

    /** The Blob directory. */
    public static final File BLOB_AREA = join(GITLET_DIR, "blob_area");

    /** The Head's Name directory. */
    public static final File HEAD_NAME = join(GITLET_DIR, "head_name");

    /** The Branches directory. */
    public static final File BRANCHES = join(GITLET_DIR, "branches");

    /** The Branches directory. */
    public static final File MERGE = join(GITLET_DIR, "merge");

    /** FOR OUR STAING AREA. */
    private HashMap<String, String> alreadyAdded;
    private HashMap<String, Commit> commitMap;
    private HashSet<String> removeStage;
    private String _head;
    private final File addMap = join(STAGING_AREA, "alreadyAdded");
    private final File removeSet = join(STAGING_AREA, "removeStage");
    private final File head = join(HEAD_NAME, "head");

    public Repository() {
        alreadyAdded = new HashMap<>();
        commitMap = new HashMap<>();
        removeStage = new HashSet<>();
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory.");
        } else {
            GITLET_DIR.mkdir();
            COMMIT_DIR.mkdir();
            STAGING_AREA.mkdir();
            BLOB_AREA.mkdir();
            BRANCHES.mkdir();
            HEAD_NAME.mkdir();
            MERGE.mkdir();
        }
        Commit initialCommit = new Commit();
        commitMap.put(initialCommit.getId(), initialCommit);
        File commitLocation = new File(".gitlet/staging_area/commitMap");
        Utils.writeObject(commitLocation, commitMap);
        saveFileAdd(alreadyAdded);
        saveFileRemove(removeStage);
        Branch headPointer = new Branch(initialCommit);
        _head = headPointer.returnName();
        saveFileHeadName();
    }

    public void add(String file) {
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles.contains(file)) {
            File newFile = Utils.join(CWD, file);
            removeStage = fromFileRemove();
            Blob blob = new Blob(newFile);
            String blobId = blob.getId();
            alreadyAdded = fromFileAdd();
            _head = fromFileHeadName();
            Branch headPointer = fromFileBranch(_head);
            Commit headCommit = headPointer.getCommit();
            HashMap<String, String> blobs = headCommit.returnBlobs();
            if (!newFile.exists()) {
                System.out.println("File does not exist.");
                return;
            }
            if (alreadyAdded.containsKey(file)) {
                if (!alreadyAdded.containsValue(blobId)) {
                    alreadyAdded.remove(file);
                    alreadyAdded.put(file, blobId);
                }
            }
            if (!blobs.containsKey(file) || !Objects.equals(blobs.get(file), blobId)) {
                alreadyAdded.put(file, blobId);
            }
            if (removeStage != null && removeStage.contains(file)) {
                alreadyAdded.remove(file);
                removeStage.remove(file);
                saveFileRemove(removeStage);
            }
            Utils.writeObject(addMap, alreadyAdded);
        } else {
            System.out.println("File does not exist.");
        }
    }

    public void commit(String message) {
        alreadyAdded = fromFileAdd();
        removeStage = fromFileRemove();
        commitMap = fromFileCommit();
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit curr = headPointer.getCommit();
        HashMap<String, String> combined = curr.returnBlobs();
        if (alreadyAdded.isEmpty() && removeStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        if (!removeStage.isEmpty()) {
            for (String removeFile: removeStage) {
                if (combined.containsKey(removeFile)) {
                    combined.remove(removeFile);
                    removeStage.remove(removeFile);
                }
            }
        }
        combined.putAll(alreadyAdded);
        Commit updated = new Commit(message, headPointer.id(), combined);
        commitMap.put(updated.getId(), updated);
        File commitLocation = new File(".gitlet/staging_area/commitMap");
        Utils.writeObject(commitLocation, commitMap);
        headPointer = new Branch(updated, _head);
        _head = headPointer.returnName();
        saveFileHeadName();
        stageClear();
        saveFileRemove(removeStage);
    }

    public void log() {
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit curr = headPointer.getCommit();
        HashMap<String, Commit> commits = fromFileCommit();
        while (curr != null) {
            System.out.println("===");
            System.out.println("commit " + curr.getId());
            System.out.println("Date: " + curr.getDate());
            System.out.println(curr.getMessage());
            System.out.println();
            curr = commits.get(curr.returnParents());
        }
    }

    public void globalLog() {
        commitMap = fromFileCommit();
        for (Commit commit: commitMap.values()) {
            System.out.println("===");
            System.out.println("commit " + commit.getId());
            System.out.println("Date: " + commit.getDate());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public void find(String message) {
        commitMap = fromFileCommit();
        boolean somethingPrinted = false;
        for (Commit commit: commitMap.values()) {
            if (Objects.equals(commit.getMessage(), message)) {
                somethingPrinted = true;
                System.out.println(commit.getId());
            }
        }
        if (!somethingPrinted) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        _head = fromFileHeadName();
        alreadyAdded = fromFileAdd();
        String[] addStringVersion = new String[alreadyAdded.size()];
        Set<String> keySet = alreadyAdded.keySet();
        String[] addSorted = keySet.toArray(addStringVersion);
        Arrays.sort(addSorted);

        removeStage = fromFileRemove();
        String[] removeStringVersion = new String[removeStage.size()];
        String[] removeSorted =  removeStage.toArray(removeStringVersion);
        Arrays.sort(removeSorted);

        System.out.println("=== Branches ===");
        for (String branches: returnBranches()) {
            if (Objects.equals(_head, branches)) {
                System.out.println("*" + branches);
            } else {
                System.out.println(branches);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String staged : addSorted) {
            System.out.println(staged);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String removed : removeSorted) {
            System.out.println(removed);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    public void checkout(String filename) {
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit currHead = headPointer.getCommit();
        HashMap<String, String> blobs = currHead.returnBlobs();
        checkoutHelper(filename, blobs);
    }

    public void checkout(String commitId, String filename) {
        commitMap = fromFileCommit();
        Set<String> commitIds = commitMap.keySet();
        Boolean worked = false;
        for (String key: commitIds) {
            if (Objects.equals(commitId, key) || key.contains(commitId)) {
                Commit rollback = commitMap.get(key);
                HashMap<String, String> blobs = rollback.returnBlobs();
                checkoutHelper(filename, blobs);
                worked = true;
            }
        }
        if (!worked) {
            System.out.println("No commit with that id exists.");
        }
    }

    public void checkoutBranch(String branchName) {
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit current = headPointer.getCommit();
        HashMap<String, String> blobs = current.returnBlobs();
        commitMap = fromFileCommit();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        List<String> branches = returnBranches();

        if (Objects.equals(_head, branchName)) {
            System.out.println("No need to checkout the current branch.");

        } else if (branches.contains(branchName)) {
            for (String fileName : cwdFiles) {
                if (!blobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it, or add and commit it first.");
                } else {
                    Branch newBranch = fromFileBranch(branchName);
                    Commit newCommit = newBranch.getCommit();
                    HashMap<String, String> newBlobs = newCommit.returnBlobs();
                    for (String file: cwdFiles) {
                        if (!newBlobs.containsKey(file)) {
                            Utils.restrictedDelete(Utils.join(CWD, file));
                        }
                    }
                    for (String item: newBlobs.keySet()) {
                        File oName = Utils.join(BLOB_AREA, newBlobs.get(item));
                        Blob oldBlob = Utils.readObject(oName, Blob.class);
                        byte[] blobContents = oldBlob.getFiles();
                        File currLocation = Utils.join(CWD, item);
                        Utils.writeContents(currLocation, blobContents);
                    }
                }
            }
            _head = branchName;
            saveFileHeadName();
        } else {
            System.out.println("No such branch exists.");
        }
    }

    private void checkoutHelper(String filename, HashMap<String, String> blob) {
        if (blob.get(filename) != null) {
            String blobSHA = blob.get(filename);
            List<String> blobList = Utils.plainFilenamesIn(BLOB_AREA);
            for (String item: blobList) {
                if (Objects.equals(item, blobSHA)) {
                    File oldName = new File(".gitlet/blob_area/" + item);
                    Blob oldBlob = Utils.readObject(oldName, Blob.class);
                    byte[] blobContents = oldBlob.getFiles();
                    File currLocation = Utils.join(CWD, filename);
                    Utils.writeContents(currLocation, blobContents);
                }
            }
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void rm(String filename) {
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit headCommit = headPointer.getCommit();
        HashMap<String, String> blobs = headCommit.returnBlobs();
        alreadyAdded = fromFileAdd();

        if (alreadyAdded.containsKey(filename)) {
            alreadyAdded.remove(filename);
            Utils.writeObject(addMap, alreadyAdded);
        } else if (blobs.containsKey(filename)) {
            removeStage.add(filename);
            Utils.writeObject(removeSet, removeStage);
            restrictedDelete(Utils.join(CWD, filename));
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void branch(String branchName) {
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit currHead = headPointer.getCommit();
        File branchNames = new File(".gitlet/branches/" + branchName);
        if (branchNames.exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            Branch newBranch = new Branch(currHead, branchName);
        }
    }

    public void removeBranch(String branchName) {
        _head = fromFileHeadName();
        List<String> branches = returnBranches();
        if (!branches.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (_head.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            File branchFile = new File(".gitlet/branches/" +  branchName);
            branchFile.delete();
        }
    }

    public void reset(String commitId) {
        commitMap = fromFileCommit();
        Set<String> commitIds = commitMap.keySet();
        HashMap<String, String> oldBlobs;
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit headCommit = headPointer.getCommit();
        HashMap<String, String> headBlobs = headCommit.returnBlobs();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (!commitMap.containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
        } else {
            for (String key : commitIds) {
                if (Objects.equals(commitId, key) || key.contains(commitId)) {
                    Commit rollback = commitMap.get(key);
                    oldBlobs = rollback.returnBlobs();
                    if (!oldBlobs.isEmpty()) {
                        for (String fileName : cwdFiles) {
                            if (!headBlobs.containsKey(fileName)
                                    && oldBlobs.containsKey(fileName)) {
                                System.out.println("There is an untracked file in the way;"
                                        + " delete it, or add and commit it first.");
                            }
                            for (String file : oldBlobs.keySet()) {
                                checkout(commitId, file);
                                for (String files: cwdFiles) {
                                    if (!oldBlobs.containsKey(files)) {
                                        File location = Utils.join(CWD, files);
                                        Utils.restrictedDelete(location);
                                    }
                                }
                            }
                        }
                        headPointer = new Branch(rollback, _head);
                        _head = headPointer.returnName();
                        saveFileHeadName();
                    }
                }
            }
            stageClear();
        }
    }

    public void merge(String branchName) {
        _head = fromFileHeadName();
        Branch headPointer = fromFileBranch(_head);
        Commit currHead = headPointer.getCommit();
        HashMap<String, String> headBlobs = currHead.returnBlobs();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        boolean found = false;

        HashMap<String, String> newBlobs = new HashMap<>();

        List<String> branches = returnBranches();
        Commit givenCommit = null;
        Commit commonAncestor = null;

        removeStage = fromFileRemove();
        alreadyAdded = fromFileAdd();

        if (!alreadyAdded.isEmpty() || !removeStage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (Objects.equals(branchName, _head)) {
            System.out.println("Cannot merge a branch with itself.");
        }
        for (String fileName : cwdFiles) {
            if (!headBlobs.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
        for (String branch: branches) {
            if (Objects.equals(branch, branchName)) {
                Branch givenBranch = fromFileBranch(branch);
                givenCommit = givenBranch.getCommit();
                found = true;
            }
        }
        if (!found) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        HashMap<String, String> givenBlobs = givenCommit.returnBlobs();
        if (givenCommit.returnParents().equals(currHead.getId())) {
            commonAncestor = currHead;
        } else {
            commonAncestor = splitFinder(currHead, givenCommit);
        }
        HashMap<String, String> ancestorBlobs = commonAncestor.returnBlobs();
        if (givenCommit == commonAncestor) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (commonAncestor.getId().equals(currHead.getId())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        HashSet<String> allFileNames = new HashSet<>();
        allFileNames.addAll(headBlobs.keySet());
        allFileNames.addAll(ancestorBlobs.keySet());
        allFileNames.addAll(givenBlobs.keySet());

        newBlobs = mergeCondHelper(allFileNames, newBlobs, headBlobs, givenBlobs, ancestorBlobs);
        mergeEndHelper(branchName, headPointer, givenCommit, newBlobs);
    }

    private HashMap<String, String> mergeCondHelper(HashSet<String> allFileNames, HashMap<String,
            String> newBlobs, HashMap<String, String> headBlobs, HashMap<String, String> givenBlobs,
                                                         HashMap<String, String> ancestorBlobs) {
        for (String fileNames: allFileNames) {
            if (!Objects.equals(givenBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && Objects.equals(headBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && givenBlobs.containsKey(fileNames)) {
                newBlobs.put(fileNames, givenBlobs.get(fileNames));
            } else if (Objects.equals(givenBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && headBlobs.containsKey(fileNames)
                    && !Objects.equals(headBlobs.get(fileNames), ancestorBlobs.get(fileNames))) {
                newBlobs.put(fileNames, headBlobs.get(fileNames));
            } else if (!Objects.equals(headBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && !Objects.equals(givenBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && Objects.equals(headBlobs.get(fileNames), givenBlobs.get(fileNames))) {
                if (headBlobs.get(fileNames) != null) {
                    newBlobs.put(fileNames, headBlobs.get(fileNames));
                }
            } else if (!ancestorBlobs.containsKey(fileNames)
                    && !givenBlobs.containsKey(fileNames)
                    && headBlobs.containsKey(fileNames)) {
                newBlobs.put(fileNames, headBlobs.get(fileNames));
            } else if (!ancestorBlobs.containsKey(fileNames)
                    && givenBlobs.containsKey(fileNames)
                    && !headBlobs.containsKey(fileNames)) {
                newBlobs.put(fileNames, givenBlobs.get(fileNames));
            } else if (Objects.equals(ancestorBlobs.get(fileNames), headBlobs.get(fileNames))
                    && !givenBlobs.containsKey(fileNames)) {
                continue;
            } else if (Objects.equals(ancestorBlobs.get(fileNames), givenBlobs.get(fileNames))
                    && !headBlobs.containsKey(fileNames)) {
                continue;
            } else if (!Objects.equals(headBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && !Objects.equals(givenBlobs.get(fileNames), ancestorBlobs.get(fileNames))
                    && !Objects.equals(headBlobs.get(fileNames), givenBlobs.get(fileNames))) {
                List<String> blobDir = Utils.plainFilenamesIn(BLOB_AREA);
                String headDs = "";
                String givenDs = "";
                for (String item : blobDir) {
                    if (Objects.equals(item, headBlobs.get(fileNames))) {
                        headDs = item;
                    }
                    if (Objects.equals(item, givenBlobs.get(fileNames))) {
                        givenDs = item;
                    }
                }
                Blob conflictBlob;
                if (givenBlobs.containsKey(fileNames)) {
                    conflictBlob = conflictBlob(fileNames, headDs, givenDs);
                } else {
                    conflictBlob = conflictBlob(fileNames, headDs);
                }
                String conflictId = conflictBlob.getId();
                newBlobs.put(fileNames, conflictId);
                System.out.println("Encountered a merge conflict.");
            }
        }
        return newBlobs;
    }
    private void mergeEndHelper(String branchName, Branch headPointer,
                                Commit givenCommit, HashMap<String, String> newBlobs) {
        String message = "Merged " + branchName + " into " + _head + ".";
        Commit updated = new Commit(message, headPointer.id(), givenCommit.getId(), newBlobs);
        commitMap.put(updated.getId(), updated);
        File commitLocation = new File(".gitlet/staging_area/commitMap");
        Utils.writeObject(commitLocation, commitMap);
        headPointer = new Branch(updated, _head);
        _head = headPointer.returnName();
        saveFileHeadName();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String file: cwdFiles) {
            if (!newBlobs.containsKey(file)) {
                Utils.restrictedDelete(Utils.join(CWD, file));
            }
        }
        for (String item: newBlobs.keySet()) {
            File oName = Utils.join(BLOB_AREA, newBlobs.get(item));
            Blob oldBlob = Utils.readObject(oName, Blob.class);
            byte[] blobContents = oldBlob.getFiles();
            File currLocation = Utils.join(CWD, item);
            Utils.writeContents(currLocation, blobContents);
        }
    }

    /** Finds the split commit common ancestor */
    public Commit splitFinder(Commit current, Commit mergeBranch) {
        commitMap = fromFileCommit();
        Set<String> parentList = new HashSet<>();
        splitHelper(current, parentList);
        if (parentList.contains(mergeBranch.getId())) {
            return mergeBranch;
        } else {
            return splitFinder(current, commitMap.get(mergeBranch.returnParents()));
        }
    }

    /** Gets the list of all the parents of the HEAD branch */
    public Set<String> splitHelper(Commit current, Set<String> parentList) {
        commitMap = fromFileCommit();
        if (current == null) {
            return parentList;
        }
        if (!Objects.equals(current.getParent2(), "")) {
            parentList.add((current.getParent2()));
        }
        parentList.add(current.returnParents());
        return splitHelper(commitMap.get(current.returnParents()), parentList);
    }

    public Blob conflictBlob(String name, String headDs, String givenDs) {
        File newConflict = Utils.join(MERGE, name);
        File headName = new File(".gitlet/blob_area/" + headDs);
        Blob headBlob = Utils.readObject(headName, Blob.class);
        String headContents = headBlob.getWorkingFileString();
        File givenName = new File(".gitlet/blob_area/" + givenDs);
        Blob givenBlob = Utils.readObject(givenName, Blob.class);
        String givenContents = givenBlob.getWorkingFileString();
        String text = "<<<<<<< HEAD" + "\n" + headContents
                + "=======" + "\n" + givenContents + ">>>>>>>" + "\n";
        Utils.writeContents(newConflict, text);
        return new Blob(newConflict);
    }

    public Blob conflictBlob(String name, String headDs) {
        File newConflict = Utils.join(MERGE, name);
        File headName = new File(".gitlet/blob_area/" + headDs);
        Blob headBlob = Utils.readObject(headName, Blob.class);
        String headContents = headBlob.getWorkingFileString();
        String text = "<<<<<<< HEAD" + "\n" + headContents
                + "=======" + "\n" + ">>>>>>>" + "\n";
        Utils.writeContents(newConflict, text);
        return new Blob(newConflict);
    }

    public void stageClear() {
        alreadyAdded.clear();
        saveFileAdd(alreadyAdded);
    }
    public void saveFileAdd(HashMap<String, String> map) {
        Utils.writeObject(addMap, map);
    }

    public void saveFileRemove(HashSet<String> map) {
        Utils.writeObject(removeSet, map);
    }

    public void saveFileHeadName() {
        Utils.writeContents(head, _head);
    }

    public HashMap<String, String> fromFileAdd() {
        return Utils.readObject(addMap, HashMap.class);
    }

    public HashSet<String> fromFileRemove() {
        return Utils.readObject(removeSet, HashSet.class);
    }

    public static HashMap<String, Commit> fromFileCommit() {
        File hashMapFile = new File(".gitlet/staging_area/commitMap");
        return Utils.readObject(hashMapFile, HashMap.class);
    }

    public static List<String> returnBranches() {
        return plainFilenamesIn(BRANCHES);
    }
    public String fromFileHeadName() {
        return Utils.readContentsAsString(head);
    }
    public Branch fromFileBranch(String name) {
        File branchFile = new File(".gitlet/branches/" + name);
        return Utils.readObject(branchFile, Branch.class);
    }

    public boolean isInitialized() {
        return GITLET_DIR.exists();
    }
}

