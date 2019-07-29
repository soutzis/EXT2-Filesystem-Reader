import java.io.IOException;

class Command{
    static final String CD = "cd", EXIT = "exit", LS = "ls", CAT="cat";

    /**
     * Is called when user types exit. It prints an informative message and then exits the running process
     */
    static void doExit(){
        System.out.print("\nEXT2 filesystem image scanning was terminated by the user.\n");
        System.exit(0);
    }

    static void doLs(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                     GroupDescriptor groupDescriptor, String path){
        try {
            FileInfo.readDirectoryData(inode, inodeSize, ext2, superblock, groupDescriptor, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void doCat(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                     GroupDescriptor groupDescriptor, String path){
        try {
            FileInfo.readFileData(inode, inodeSize, ext2, superblock, groupDescriptor, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
