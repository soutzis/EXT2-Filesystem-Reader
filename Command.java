

class Command{
    static final String CD = "cd", EXIT = "exit", LS = "ls";

    /**
     * Is called when user types exit. It prints an informative message and then exits the running process
     */
    static void doExit(){
        System.out.print("\nEXT2 filesystem image scanning was terminated by the user.\n");
        System.exit(0);
    }
}
