import java.util.HashMap;

enum Command {
    CD("CD"),
    LS("LS");

    private String command;
    private static final HashMap<String, Command> map;

    Command(String command) {
        this.command = command;
    }

    //static code block will only be executed once, no matter how many instances of Command are created.
    static {
        HashMap<String, Command> temp = new HashMap<>();
        for (Command cmd : Command.values())
            temp.put(cmd.getCommandName(), cmd);
        //Add contents of temporary Map to the immutable static map of this class
        map = new HashMap<>(temp);
    }

    private String getCommandName(){
        return command;
    }

    public static Command getCommand(String cmd){
        return map.get(cmd);
    }
}
