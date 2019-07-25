import java.io.IOException;
import java.util.*;
/**
 *This is the main class of the program. It provides a way to readBytes an Ext2 Filesystem image,
 *based on the path that will be provided by the user, as a string.
 *@author Petros Soutzis, 2017-19
 */

public class Driver {
    static boolean debug = false;
    static boolean running = false;

    static String getCommandAndExecute(String input){
        String cmd = (input.trim().substring(0, input.trim().indexOf(" ")));

        //determine what command it is and act appropriately
        switch (cmd){
            case Command.EXIT:
                running = false;
                Command.doExit();
                break;
            case Command.CD:

            default:
                System.out.println(cmd+": command not found");
                return null;
        }
        return null;
    }

    static String calculateCurrentPath(String input){
        return null;
    }

    // Main method of program
    //TODO Make the passing of the file as a runtime argument. User will have to enter the image name
    //FIXME, need currentDir to be retrieved correctly
    public static void main (String[] args) throws IOException {
        running = true;
        String currentDir = "~", promptSymbol = "$ ";
        System.out.println("EXT2 Filesystem Reader version " + Metadata.VERSION);

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
        Inode currentInode = new Inode(rootInodeData);
        currentInode.read();

        while(running) {
            //Get the user input
            System.out.print("\n"+currentDir+promptSymbol);
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();

            //The path that the program will readBytes from
            String path = "";

            //Error check and correction for user input
            if(input.equals("/"))
                path = "/.";
            else if(input.endsWith("/"))
                path = input.substring(0, input.lastIndexOf('/'));
            else if((!input.startsWith("/")))
                path = "/"+input;

            //An array of strings, with each string consisting of the names of the path's directories or files
            String[] pathArray = path.split("/");

            //Will locate data based on the path (array of directory names and/or file name)
            FileInfo info = new FileInfo(currentInode, inodeSize, sBlock, groupDesc, ext2);
            info.getFileInfo(pathArray);
            currentInode = info.getFileInode();
            currentDir += info.getFilePath();  //FIXME (this is obviously incorrect. Leave it for now, fix later)
        }
    }
}
