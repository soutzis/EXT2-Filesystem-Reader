import java.nio.*;
import java.util.*;
/**
 *This class reads an Inode and all the metadata it provides
 *@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
 */

public class Inode
{
    private short i_mode; //file mode
    private short i_uid; //user ID
    private int	i_size_lower; //file size in bytes (lower 32 bits)
    private int i_atime; //time of last access
    private int	i_ctime; //creation time
    private int i_mtime; //last modified
    private int	i_dtime; //time of file deletion
    private short i_gid; //group ID of owners
    private short i_links_count; //number of'hard link references to file'
    private int[] i_block_pointer; //pointers to data blocks or to other pointer blocks
    private int i_size_upper; //file size in bytes (lower 64 bits)

    private ByteBuffer buffer; // The ByteBuffer to hold the data readBytes from the inode


    /**
     *Constructor of the inode class
     *@param bytes is the byte array that contains the inode's data
     */
    public Inode(byte[] bytes) {
        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        i_block_pointer = new int[Constants.INODE_POINTERS_COUNT];
    }

    /**
     *This method will readBytes all the data that is contained in the inode, from the buffer, in which the byte array was parsed
     */
    public void read() {
        i_mode = buffer.getShort(Constants.I_MODE_OFFSET);
        i_uid = buffer.getShort(Constants.I_UID_OFFSET);
        i_size_lower = buffer.getInt(Constants.I_SIZE_LOWER_OFFSET);
        i_atime = buffer.getInt(Constants.I_ACCESS_TIME_OFFSET);
        i_ctime = buffer.getInt(Constants.I_CREATION_TIME_OFFSET);
        i_mtime = buffer.getInt(Constants.I_MODIFICATION_TIME_OFFSET);
        i_dtime = buffer.getInt(Constants.I_DELETION_TIME_OFFSET);
        i_gid = buffer.getShort(Constants.I_GID_OFFSET);
        i_links_count = buffer.getShort(Constants.I_LINKS_COUNT_OFFSET);

        for(int i=0; i<15; i++)
            i_block_pointer[i] = buffer.getInt(Constants.I_BLOCK_POINTERS_OFFSET + (i*4));

        i_size_upper = buffer.getInt(Constants.I_SIZE_UPPER_OFFSET);
    }

    /**
     *This method uses bitwise ANDing on FileSystem's i_mode variable value,
     *to determine what permissions a directory or file has.
     *@return a String with the full permissions of a file or directory
     */
    String readPermissions()
    {
        String permissions = "";

        if(((int)i_mode & Constants.IFSCK) == Constants.IFSCK)
            permissions = "socket";
        else if(((int)i_mode & Constants.IFLNK) == Constants.IFLNK)
            permissions = "symbolic link";
        else if(((int)i_mode & Constants.IFREG) == Constants.IFREG)
            permissions = "-";
        else if(((int)i_mode & Constants.IFBLK) == Constants.IFBLK)
            permissions = "block device";
        else if(((int)i_mode & Constants.IFDIR) == Constants.IFDIR)
            permissions = "d";
        else if(((int)i_mode & Constants.IFCHR) == Constants.IFCHR)
            permissions = "c";
        else if(((int)i_mode & Constants.IFIFO) == Constants.IFIFO)
            permissions = "fifo";

        //USER GROUP PERMISSIONS
        permissions += ((int)i_mode & Constants.IRUSR) == Constants.IRUSR ? "r" : "-";
        permissions += ((int)i_mode & Constants.IWUSR) == Constants.IWUSR ? "w" : "-";
        permissions += ((int)i_mode & Constants.IXUSR) == Constants.IXUSR ? "x" : "-";

        //GROUP PERMISSIONS
        permissions += ((int)i_mode & Constants.IRGRP) == Constants.IRGRP ? "r" : "-";
        permissions += ((int)i_mode & Constants.IWGRP) == Constants.IWGRP ? "w" : "-";
        permissions += ((int)i_mode & Constants.IXGRP) == Constants.IXGRP ? "x" : "-";

        //OTHER GROUP PERMISSIONS
        permissions += ((int)i_mode & Constants.IROTH) == Constants.IROTH ? "r" : "-";
        permissions += ((int)i_mode & Constants.IWOTH) == Constants.IWOTH ? "w" : "-";
        permissions += ((int)i_mode & Constants.IXOTH) == Constants.IXOTH ? "x" : "-";
        permissions += ((int)i_mode & Constants.ISVTX) == Constants.ISVTX ? "t" : "";

        return permissions;
    }

    /**
     *A static method, used for fetching the block number (offset), that an inode points to. Otherwise, it returns 0
     *@param rootInodeOffset The number of the inode, with which this method will calculate the containing block number
     *@return the offset of the containing block
     */
    static int getContainingBlock(int rootInodeOffset, Superblock superblock, GroupDescriptor groupDescriptor) {

        //Total number of inodes in the filesystem
        int inodeCount = superblock.getInodeCount();
        //Total number of inodes per block group
        int inodesPerGroup = superblock.getInodesPerGroup();
        //Size of inodes, as readBytes from the superblock
        int inodeSize = superblock.getInodeSize();

        //The block group that the inode resides in
        int pointerDiv;
        //the group descriptor table pointers
        int[] gDescPointer = groupDescriptor.getGDescPointer();
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
                containingBlock = ((pointer*inodeSize/Constants.BLOCK_SIZE)+inodeTablePointer) * Constants.BLOCK_SIZE;

                //convert to int to return
                return (int)containingBlock;
            }
        }
        //otherwise return 0, which is the first block
        return 0;
    }

    /**
     *@return the array of all the data pointers in an inode
     */
    int[] getBlockPointers() {
        return i_block_pointer;
    }

    /**
     *@return If the user has root access or not
     */
    String getUid() {
        return i_uid == 0 ? "root" : "user";
    }

    /**
     *@return If the guest has root access or not
     */
    String getGid() {
        return i_gid == 0 ? "root" : "group";
    }

    /**
     *@return the file size (lower 32 bits)
     */
    int getSizeLower() {
        return i_size_lower;
    }

    /**
     *@return the file size (upper 32 bits)
     */
    int getSizeUpper() {
        return i_size_upper;
    }

    /**
     *@return true, if this is a file. Otherwise, return false.
     */
    public boolean isFile() {
        return ((int) i_mode & Constants.IFREG) == Constants.IFREG;
    }

    /**
     *@return the date that this file was lastly modified
     */
    public Date getDate() {
        return new Date((long)i_mtime*1000);
    }

    /**
     *@return the number of hard link references to a file
     */
    short getHardLinks() {
        return i_links_count;
    }

}
