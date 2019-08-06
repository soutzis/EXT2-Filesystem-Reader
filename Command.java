import java.io.IOException;

/**
 * This class holds the command name literals for each of the command supported by this project (ls, cd, exit, cat).
 * No command flag support has been implemented.
 *
 * @author psoutzis
 */

class Command{
    static final String CD = "cd", EXIT = "exit", LS = "ls", CAT="cat";

    /**
     * Is called when user types exit. It prints an informative message and then exits the running process
     */
    static void doExit(){
        System.out.print("\nEXT2 filesystem image scanning was terminated by the user.\n");
        System.exit(0);
    }

    /**
     * Used for the "ls" command
     * @param inode The inode to use
     * @param inodeSize The inode size
     * @param ext2 The Ext2File instance to provide a bytebuffer to use
     * @param superblock The superblock instance
     * @param groupDescriptor The group descriptor instance
     */
    static void doLs(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                     GroupDescriptor groupDescriptor){
        try {
            FileInfo.readDirectoryData(inode, inodeSize, ext2, superblock, groupDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used for the "cat" command
     * @param inode The inode to use
     * @param inodeSize The inode size
     * @param ext2 The Ext2File instance to provide a bytebuffer to use
     * @param superblock The superblock instance
     * @param groupDescriptor The group descriptor instance
     * @param path The path the user issued "cat" to
     */
    static void doCat(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                     GroupDescriptor groupDescriptor, String path){
        try {
            FileInfo.readFileData(inode, inodeSize, ext2, superblock, groupDescriptor, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
