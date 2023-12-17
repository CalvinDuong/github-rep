package gitlet;
import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Calvin Duong and Vivi Thai
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        Repository repo = new Repository();
        String firstArg = args[0];
        if (!repo.isInitialized() && !Objects.equals(args[0], "init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (firstArg) {
            case "init":
                repo.init();
                break;
            case "add":
                repo.add(args[1]);
                break;
            case "commit":
                repo.commit(args[1]);
                break;
            case "log":
                repo.log();
                break;
            case "global-log":
                repo.globalLog();
                break;
            case "find":
                repo.find(args[1]);
                break;
            case "rm":
                repo.rm(args[1]);
                break;
            case "branch":
                repo.branch(args[1]);
                break;
            case "status":
                repo.status();
                break;
            case "rm-branch":
                repo.removeBranch(args[1]);
                break;
            case "reset":
                repo.reset(args[1]);
                break;
            case "merge":
                repo.merge(args[1]);
                break;
            case "checkout":
                switch (args.length) {
                    case 2 -> repo.checkoutBranch(args[1]);
                    case 3 -> repo.checkout(args[2]);
                    case 4 -> {
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            break;
                        }
                        repo.checkout(args[1], args[3]);
                    }
                    default -> System.out.println("Incorrect operands");
                }
                break;
            default: System.out.println("No command with that name exists.");
            break;
        }
    }
}

