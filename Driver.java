import java.io.IOException;
import java.util.*;

/**
 *This is the main class of the program. It provides a way to readBytes an Ext2 Filesystem image,
 *based on the path that will be provided by the user, as a string.
 *@author Petros Soutzis, 2017-19
 */

public class Driver {
    static Inode inode = null;
    static String currentDir = "/", previousDir="/";
    //static boolean debug = false;
    private static boolean running = false;  //could be only in main(), but was added in 'getCommandAndExecute()'

    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("USER"))
            return env.get("USER")+":";
        else return env.getOrDefault("USERNAME", "UnknownHost")+":";
    }

    private static void getCommandAndExecute(String input, int inodeSize,
                                             Superblock sBlock, GroupDescriptor groupDesc, Ext2File ext2)
    throws IOException{
        String manySpacesRegex = " +";
        String[] inputPipeline = (input.replaceAll(manySpacesRegex, " ")).trim().split(" ");

        String command = inputPipeline[0], path = inputPipeline.length > 1 ? inputPipeline[1] : ".";
        String[] pathArray = path.split("/");
        pathArray = Arrays.stream(pathArray)
                .filter(s -> s.length() > 0)
                .toArray(String[]::new);

        Inode currentInode = Inode.getContainingInode(pathArray, inodeSize, ext2, inode, sBlock, groupDesc);

        //determine what command it is and act appropriately
        switch (command){
            case Command.EXIT:
                running = false;
                Command.doExit();
                break;
            case Command.LS:
                Command.doLs(currentInode, inodeSize, ext2, sBlock, groupDesc, pathArray[pathArray.length-1]);
                break;
            case Command.CAT:
                Command.doCat(currentInode, inodeSize, ext2, sBlock, groupDesc, pathArray[pathArray.length-1]);
                break;
            case Command.CD:
                currentInode.read();
                inode = currentInode;
                if(path.equals(".."))
                    currentDir = previousDir;
                else
                    currentDir = currentDir+"/"+path;
                break;

            default:
                System.out.println(command+": command not found");
        }
    }

    // Main method of program
    //FIXME NEED TO RETRIEVE CURRENTDIR CORRECTLY
    public static void main (String[] args) throws IOException {
        running = true;
        String compName = getComputerName(), promptSymbol = "$ ";
        System.out.println("EXT2 Filesystem Reader version " + Metadata.VERSION + " BY P.SOUTZIS");

        //Initialize a Volume instance and pass it to the Ext2File constructor, to readBytes the ext2 fs image
        Volume vol = new Volume("ext2fs");
        Ext2File ext2 = new Ext2File(vol);

        //Byte array to hold the data of the SuperBlock, as returned by Ext2File's readBytes() method
        byte[] superBlockData = ext2.readBytes(Constants.SUPERBLOCK_OFFSET, Constants.BLOCK_SIZE);
        Superblock sBlock = new Superblock(superBlockData);
        sBlock.read();

        /*Extract data from SuperBlock*/
        final int blockCount = sBlock.getBlockCount();
        final int blocksPerGroup = sBlock.getBlocksPerGroup();
        final int inodeSize = sBlock.getInodeSize();
        final int blockGroupCount = sBlock.getBlockGroupCount(blockCount, blocksPerGroup);

        sBlock.printGenericData(inodeSize, blockGroupCount);

        /*Initialize Group Descriptor*/
        byte[] gDescData = ext2.readBytes(Constants.GDESCRIPTOR_OFFSET, Constants.BLOCK_SIZE);
        //The Group Descriptor class instance, which reads the group descriptor
        GroupDescriptor groupDesc = new GroupDescriptor(gDescData, blockGroupCount);
        groupDesc.read();

        int[] inodeTable = groupDesc.getGDescPointer();
        for(int i=0; i<inodeTable.length; i++)
            System.out.println("Inode Table "+(i+1)+" offset in Group Descriptor is: "+inodeTable[i]);

        //The containing block offset for the Root Inode
        int rootInodeBlockNumber =
                Inode.getContainingBlock(Constants.ROOT_INODE_OFFSET, sBlock, groupDesc);

        //Initialize root inode
        byte[] rootInodeData = ext2.readBytes(rootInodeBlockNumber, inodeSize);
        //Contains pointers to the filesystem blocks, which contain the data
        inode = new Inode(rootInodeData);
        inode.read();

        while(running) {
            //Get the user input
            System.out.print("\n"+compName+currentDir+promptSymbol);
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();

            getCommandAndExecute(input, inodeSize, sBlock, groupDesc, ext2);
        }
    }
}
