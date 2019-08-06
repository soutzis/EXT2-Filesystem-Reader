/**
 * This class contains constants (macros) that are used throughout the program.
 *
 * @author Petros Soutzis
 */
public class Constants {

    //The capacity of each block in the volume
    static final int BLOCK_SIZE = 1024;
    //The offset of the inode that holds all the information about other inodes.
    static final int ROOT_INODE_OFFSET = 2;
    //The offset byte, at which the Superblock starts at (and block group 0)
    static final long SUPERBLOCK_OFFSET = 1024;
    //The offset byte at which the Group Descriptor starts at
    static final long GDESCRIPTOR_OFFSET = 2048;
    //equivalent of 32 bits in bytes (an int)
    static final int BYTE_LENGTH = 4;
    //equivalent of 16 bits in bytes (a short)
    static final int SHORT_LENGTH = 2;
    //Root symbol
    static final String ROOT = "/";


    /*Offset Constants for extracting the information from the SuperBlock*/
    static final int S_MAGIC_OFFSET = 56;
    static final int S_INODE_COUNT_OFFSET = 0;
    static final int S_BLOCK_COUNT_OFFSET = 4;
    static final int S_BLOCKS_PER_GROUP_OFFSET = 32;
    static final int S_INODES_PER_GROUP_OFFSET = 40;
    static final int S_INODE_SIZE_OFFSET = 88;
    static final int S_FILESYSTEM_NAME_OFFSET = 120;
    static final int S_FILESYSTEM_NAME_LENGTH = 16;

    static final int INODE_POINTERS_COUNT = 15; // The inode has 15 pointers that point to data
    static final int NUMBER_OF_GROUPS = 3; // root, user, other

    /*INODE FILE MODES*/
    static final int IFSCK = 0xC000;  // Socket file mode
    static final int IFLNK = 0xA000;  // Symbolic Link file mode
    static final int IFREG = 0x8000;  // Regular File file mode
    static final int IFBLK = 0x6000;  // Block Device file mode
    static final int IFDIR = 0x4000;  // Directory file mode
    static final int IFCHR = 0x2000;  // Character Device file mode
    static final int IFIFO = 0x1000;  // FIFO file mode
    static final int ISUID = 0x0800;  // Set process User ID file mode
    static final int ISGID = 0x0400;  // Set process Group ID file mode
    static final int ISVTX = 0x0200;  // Sticky bit file mode
    static final int IRUSR = 0x0100;  // User readBytes file mode
    static final int IWUSR = 0x0080;  // User write file mode
    static final int IXUSR = 0x0040;  // User execute file mode
    static final int IRGRP = 0x0020;  // Group readBytes file mode
    static final int IWGRP = 0x0010;  // Group write file mode
    static final int IXGRP = 0x0008;  // Group execute file mode
    static final int IROTH = 0x0004;  // Others readBytes file mode
    static final int IWOTH = 0x0002;  // Others write file mode
    static final int IXOTH = 0x0001;  // Others execute file mode

    /*Inode Information Offset Constants*/
    static final int I_MODE_OFFSET = 0;
    static final int I_UID_OFFSET = 2;
    static final int I_SIZE_LOWER_OFFSET = 4;
    static final int I_ACCESS_TIME_OFFSET = 8;
    static final int I_CREATION_TIME_OFFSET = 12;
    static final int I_MODIFICATION_TIME_OFFSET = 16;
    static final int I_DELETION_TIME_OFFSET = 20;
    static final int I_GID_OFFSET = 24;
    static final int I_LINKS_COUNT_OFFSET = 26;
    static final int I_BLOCK_POINTERS_OFFSET = 40;
    static final int I_SIZE_UPPER_OFFSET = 108;
}
