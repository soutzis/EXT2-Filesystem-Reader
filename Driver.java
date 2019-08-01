import java.io.IOException;
import java.util.*;

/**
 *This is the main class of the program. It provides a way to readBytes an Ext2 Filesystem image,
 *based on the path that will be provided by the user, as a string.
 *@author Petros Soutzis, 2017-19
 */

public class Driver {
    private static Inode inode = null;
    private static String currentDir;
    private static Stack<String> pathStack = new Stack<>();
    //static boolean debug = false;
    private static boolean running = false;  //could be only in main(), but was added in 'getCommandAndExecute()'

    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("USER"))
            return env.get("USER")+":";
        else return env.getOrDefault("USERNAME", "UnknownHost")+":";
    }

    /**
     * Method to get the current-path. Utilises a global stack and String.
     * @param pathArray The array containing the path values (leaps)
     */
    private static void getCurrentPathName(String[] pathArray) {
        for(String path : pathArray){
            if(path.equals("..") && !pathStack.peek().equals(Constants.ROOT)) {
               pathStack.pop();
            }
            else if(!path.equals(".")){
                if(pathStack.peek().equals("/"))
                    pathStack.push(path);
                else
                    pathStack.push("/"+path);
            }
        }
        Stack<String> pathStackCopy = new Stack<>();
        pathStackCopy.addAll(pathStack);
        StringBuilder sb = new StringBuilder();

        while(!pathStackCopy.empty()){
            sb.insert(0,pathStackCopy.pop());
        }
        currentDir = sb.toString();
    }

    /**
     * If the user enters anything else than Y/y or N/n, they will be prompted to re-enter their decision.
     * @return true if user enters Y/y, or false if user enters N/n.
     */
    private static boolean getInputYesNo(){
        Scanner scanner = new Scanner(System.in);
        String yes="y", no="n", input = scanner.nextLine();
        while((!input.equalsIgnoreCase(yes)) && (!input.equalsIgnoreCase(no))){
            System.out.println("Only acceptable inputs are \"Y\" and \"N\" (case insensitive).");
            System.out.print("(Y/N): ");
            input = scanner.nextLine();
        }

        return input.equalsIgnoreCase(yes);
    }

    /**
     * This method will divide the user's input into a command and a target path and execute the given command.
     * @param input The user's raw input
     * @param inodeSize The size of any inode on this FS.
     * @param sBlock The superblock of this FS.
     * @param groupDesc The group descriptor of this FS.
     * @param ext2 The Ext2File instance of the FS.
     * @throws IOException This is not actually correct, the exception should be handled in Ext2File, but who cares.
     */
    private static void getCommandAndExecute(String input, int inodeSize,
                                             Superblock sBlock, GroupDescriptor groupDesc, Ext2File ext2)
    throws IOException{
        String manySpacesRegex = " +";
        //Replace 1 or more spaces with a single space, then remove starting and ending spaces and split into array
        String[] inputPipeline = (input.replaceAll(manySpacesRegex, " ")).trim().split(" ");

        //If ls command is issued with no path, then use current directory
        String command = inputPipeline[0], path = inputPipeline.length > 1 ? inputPipeline[1] : ".";
        //Split based on foreslash, then remove any elements from the array that are "the empty string".
        String[] pathArray = path.split("/");
        pathArray = Arrays.stream(pathArray)
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);

        Inode currentInode = Inode.getContainingInode(pathArray, inodeSize, ext2, inode, sBlock, groupDesc);

        if(currentInode == null)
            return;

        //determine what command it is and act appropriately
        switch (command){
            case Command.EXIT:
                running = false;
                Command.doExit();
                break;
            case Command.LS:
                if(currentInode.isFile())
                    System.out.println(pathArray[pathArray.length-1]);
                else
                    Command.doLs(currentInode, inodeSize, ext2, sBlock, groupDesc, pathArray[pathArray.length-1]);
                break;
            case Command.CAT:
                Command.doCat(currentInode, inodeSize, ext2, sBlock, groupDesc, pathArray[pathArray.length-1]);
                break;
            case Command.CD:
                if(currentInode.isFile())
                    System.out.println("Not a directory");
                else {
                    inode = currentInode;
                    getCurrentPathName(pathArray);
                }
                break;

            default:
                System.out.println(command+": command not found");
        }
    }

    // Main method of program
    public static void main (String[] args) throws IOException {
        running = true;
        String compName = getComputerName(), promptSymbol = "$ ";
        System.out.println("EXT2 Filesystem Reader version " + Metadata.VERSION + " BY P.SOUTZIS\n");

        //Initialize a Volume instance and pass it to the Ext2File constructor, to readBytes the ext2 fs image
        String fsName = Ext2File.getFileSystemName();
        Volume vol = new Volume(fsName);
        Ext2File ext2 = new Ext2File(vol);

        //Byte array to hold the data of the SuperBlock, as returned by Ext2File's readBytes() method
        byte[] superBlockData = ext2.readBytes(Constants.SUPERBLOCK_OFFSET, Constants.BLOCK_SIZE);
        Superblock sBlock = new Superblock(superBlockData);

        /*Extract data from SuperBlock*/
        final int blockCount = sBlock.getBlockCount();
        final int blocksPerGroup = sBlock.getBlocksPerGroup();
        final int inodeSize = sBlock.getInodeSize();
        final int blockGroupCount = sBlock.getBlockGroupCount(blockCount, blocksPerGroup);

        /*Initialize Group Descriptor*/
        byte[] gDescData = ext2.readBytes(Constants.GDESCRIPTOR_OFFSET, Constants.BLOCK_SIZE);
        //The Group Descriptor class instance, which reads the group descriptor
        GroupDescriptor groupDesc = new GroupDescriptor(gDescData, blockGroupCount);

        //If the user chooses to, then print this information.
        System.out.print("Would you like to view generic information about the mounted volume? (Y/N): ");
        if(getInputYesNo()) {
            sBlock.printGenericData(inodeSize, blockGroupCount);
            int[] inodeTable = groupDesc.getGDescPointer();
            for (int i = 0; i < inodeTable.length; i++)
                System.out.println("Inode Table " + (i + 1) + " offset in Group Descriptor is: " + inodeTable[i]);
        }

        //Push root symbol ("/") into stack and initialize currentDir
        pathStack.push(Constants.ROOT);
        currentDir = pathStack.peek();

        //The containing block offset for the Root Inode
        int rootInodeBlockNumber =
                Inode.getContainingBlock(Constants.ROOT_INODE_OFFSET, sBlock, groupDesc);

        //Initialize root inode
        byte[] rootInodeData = ext2.readBytes(rootInodeBlockNumber, inodeSize);
        //Contains pointers to the filesystem blocks, which contain the data
        inode = new Inode(rootInodeData);

        while(running) {
            //Get the user input
            System.out.print("\n"+compName+currentDir+promptSymbol);
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();

            getCommandAndExecute(input, inodeSize, sBlock, groupDesc, ext2);
        }
    }
}
