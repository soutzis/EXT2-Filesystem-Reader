import java.io.IOException;
import java.util.*;
/**
 *This is the main class of the program. It provides a way to read an Ext2 Filesystem image,
 *based on the path that will be provided by the user, as a string.
 *@author Petros Soutzis, 2017-19
 */

public class Driver {
    //The ext2 filesystem image
    static Ext2File ext2;
    //The Group Descriptor class instance, which reads the group descriptor
    private static GroupDescriptor groupDesc;
    //The super block instance
    private static Superblock sBlock;

    //The capacity of each block in the volume
    private static final int BLOCK_SIZE = 1024;
    //The offset of the inode that holds all the information about other inodes.
    private static final int ROOT_INODE_OFFSET = 2;
    //The offset byte, at which the Superblock starts at (and block group 0)
    private static final long SUPERBLOCK_OFFSET = 1024;
    //The offset byte at which the Group Descriptor starts at
    private static final long GDESCRIPTOR_OFFSET = 2048;
    //The hardcoded application version.
    private static final String VERSION = "1.2";

    static boolean DEBUG = false;


    /**
     *A static method, used for fetching the block number (offset), that an inode points to. Otherwise, it returns 0
     *@param rootInodeOffset The number of the inode, with which this method will calculate the containing block number
     *@return the offset of the containing block
     */
    static int getContainingBlock(int rootInodeOffset) {

        //Total number of inodes in the filesystem
        int inodeCount = sBlock.getInodeCount();
        //Total number of inodes per block group
        int inodesPerGroup = sBlock.getInodesPerGroup();
        //Size of inodes, as read from the superblock
        int inodeSize = sBlock.getInodeSize();

        //The block group that the inode resides in
        int pointerDiv;
        //the group descriptor table pointers
        int[] gDescPointer = groupDesc.getGDescPointer();
        //the inode table pointer of block group n.
        int inodeTablePointer;
        //the index of the inode as a double to avoid data loss, when calculating for block group 0
        double pointer;
        //the number of the containing block, as a double to avoid data loss, when calculating for block group 0
        double containingBlock;

        //only perform calculations for inodes 2 and up. 2 Because inodes start from 1.
        //But the pointer returned will be 0 for the first block.
        if (rootInodeOffset >= 2) {
            //validate inode index, by checking if it is not any bigger that the total number of inodes
            if(rootInodeOffset < inodeCount) {
                //because inodes start counting from 1, but start from 0 in the inode table
                rootInodeOffset -= 1;
                //dividing the inode number with the number of inodes per group,
                //to get the index of the inode in the Descriptor table
                pointerDiv = rootInodeOffset/inodesPerGroup;
                //the remainder of the above equation will be
                //used in calculating the containing block below. http://cs.smith.edu/~nhowe/262/oldlabs/ext2.html
                pointer = rootInodeOffset % inodesPerGroup;
                inodeTablePointer = gDescPointer[pointerDiv];
                containingBlock = ((pointer * inodeSize/ BLOCK_SIZE) + inodeTablePointer) * BLOCK_SIZE;

                //convert to int to return
                return (int)containingBlock;
            }
        }
        //otherwise return 0, which is the first block
        return 0;
    }

    // Main method of program
    public static void main (String[] args) throws IOException {

        String[] exitCommands = {"exit", "quit", "end", "stop"};
        String currentDir = null, promptSymbol = " >> ";

        System.out.println("EXT2 FILESYSTEM READER " + Driver.VERSION);
        System.out.println("\nReading ext2 image..\n");

        //Initialize a Volume instance and pass it to the Ext2File constructor, to read the ext2 fs image
        Volume vol = new Volume("ext2fs");
        ext2 = new Ext2File(vol);

        //Byte array to hold the data of the SuperBlock, as returned by Ext2File's read() method
        byte[] superBlockData = ext2.read(SUPERBLOCK_OFFSET, BLOCK_SIZE);
        sBlock = new Superblock(superBlockData);
        sBlock.read();

        /*Extract data from SuperBlock*/
        int inodeCount = sBlock.getInodeCount();
        int inodesPerGroup = sBlock.getInodesPerGroup();
        int blockCount = sBlock.getBlockCount();
        int blocksPerGroup = sBlock.getBlocksPerGroup();
        int inodeSize = sBlock.getInodeSize();
        int blockGroupCount = sBlock.getBlockGroupCount(blockCount, blocksPerGroup);
        String volumeName = sBlock.getVolumeName();

        System.out.println("VOLUME NAME: " + volumeName);
        System.out.println("Total number of inodes is: " + inodeCount);
        System.out.println("Total number of inodes per group is: " + inodesPerGroup);
        System.out.println("Total size of inodes is: " + inodeSize);
        System.out.println("Total number of blocks is: " + blockCount);
        System.out.println("Total number of blocks per group is: " + blocksPerGroup);
        System.out.println("Total number of block groups is: " + blockGroupCount);

        /*Initialize Group Descriptor*/
        byte[] gDescData = ext2.read(GDESCRIPTOR_OFFSET, BLOCK_SIZE);
        groupDesc = new GroupDescriptor(gDescData, blockGroupCount);
        groupDesc.read();

        int[] inodeTable = groupDesc.getGDescPointer();
        for(int i=0; i<inodeTable.length; i++)
            System.out.println("Inode Table "+(i+1)+" offset in Group Descriptor is: "+inodeTable[i]);

        //The containing block offset for the Root Inode
        int rootInodeBlockNumber = getContainingBlock(ROOT_INODE_OFFSET);

        //Initialize root inode
        byte[] rootInodeData = ext2.read(rootInodeBlockNumber, inodeSize);
        //Contains pointers to the filesystem blocks, which contain the data
        Inode currentInode = new Inode(rootInodeData);
        currentInode.read();

        currentDir = "root";  // FIXME. This HAS to be retrieved dynamically! (100% required)

        while(true) {
            //Get the user input
            System.out.print("\n"+currentDir+promptSymbol);
            Scanner scan = new Scanner(System.in);
            String input = scan.nextLine();

            //If user enters command to stop the program, then exit.
            for(String command : exitCommands) {
                if (input.equalsIgnoreCase(command)) {
                    System.out.print("\nExt2 filesystem image scanning was terminated by the user.\n");
                    System.exit(0);
                }
            }

            //The path that the program will read from
            String path = "";

            //Error check and correction for user input
            if(input.equals("/"))
                path = "/.";
            else if(input.endsWith("/"))
                path = input.substring(0, input.lastIndexOf('/'));
            else if((!input.startsWith("/")))
                path = "/"+input;

            //An array of strings, with each string consisting of the names of the path's directories or files
            String[] pathFragments = path.split("/");

            //Will locate data based on the path (array of directory names and/or file name)
            FileInfo info = new FileInfo(currentInode, inodeSize);
            info.getFileInfo(pathFragments);
            currentInode = info.getFileInode();
            currentDir += info.getFilePath();  //FIXME (this is obviously incorrect. Leave it for now, fix later)
        }
    }
}
