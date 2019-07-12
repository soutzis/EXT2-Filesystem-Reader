import java.nio.*;
import java.io.*;
import java.util.*;
/**
 *This class reads an Inode and all the metadata it provides
 *@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
 */

@SuppressWarnings({"FieldCanBeLocal", "Duplicates"})
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

    private ByteBuffer buffer; // The ByteBuffer to hold the data read from the inode

    private final int INODE_POINTERS_COUNT = 15; // The inode has 15 pointers that point to data
    private final int NUMBER_OF_GROUPS = 3; // root, user, other

    /*INODE FILE MODES*/
    private final int IFSCK = 0xC000;  // Socket file mode
    private final int IFLNK = 0xA000;  // Symbolic Link file mode
    private final int IFREG = 0x8000;  // Regular File file mode
    private final int IFBLK = 0x6000;  // Block Device file mode
    private final int IFDIR = 0x4000;  // Directory file mode
    private final int IFCHR = 0x2000;  // Character Device file mode
    private final int IFIFO = 0x1000;  // FIFO file mode

    private final int ISUID = 0x0800;  // Set process User ID file mode
    private final int ISGID = 0x0400;  // Set process Group ID file mode
    private final int ISVTX = 0x0200;  // Sticky bit file mode

    private final int IRUSR = 0x0100;  // User read file mode
    private final int IWUSR = 0x0080;  // User write file mode
    private final int IXUSR = 0x0040;  // User execute file mode

    private final int IRGRP = 0x0020;  // Group read file mode
    private final int IWGRP = 0x0010;  // Group write file mode
    private final int IXGRP = 0x0008;  // Group execute file mode

    private final int IROTH = 0x0004;  // Others read file mode
    private final int IWOTH = 0x0002;  // Others write file mode
    private final int IXOTH = 0x0001;  // Others execute file mode

    /*Inode Information Offset Constants*/
    private final int I_MODE_OFFSET = 0;
    private final int I_UID_OFFSET = 2;
    private final int I_SIZE_LOWER_OFFSET = 4;
    private final int I_ACCESS_TIME_OFFSET = 8;
    private final int I_CREATION_TIME_OFFSET = 12;
    private final int I_MODIFICATION_TIME_OFFSET = 16;
    private final int I_DELETION_TIME_OFFSET = 20;
    private final int I_GID_OFFSET = 24;
    private final int I_LINKS_COUNT_OFFSET = 26;
    private final int I_BLOCK_POINTERS_OFFSET = 40;
    private final int I_SIZE_UPPER_OFFSET = 108;


    /**
     *Constructor of the inode class
     *@param bytes is the byte array that contains the inode's data
     */
    public Inode(byte[] bytes) {
        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        i_block_pointer = new int[INODE_POINTERS_COUNT];
    }

    /**
     *This method will read all the data that is contained in the inode, from the buffer, in which the byte array was parsed
     */
    public void read() {
        i_mode = buffer.getShort(I_MODE_OFFSET);
        i_uid = buffer.getShort(I_UID_OFFSET);
        i_size_lower = buffer.getInt(I_SIZE_LOWER_OFFSET);
        i_atime = buffer.getInt(I_ACCESS_TIME_OFFSET);
        i_ctime = buffer.getInt(I_CREATION_TIME_OFFSET);
        i_mtime = buffer.getInt(I_MODIFICATION_TIME_OFFSET);
        i_dtime = buffer.getInt(I_DELETION_TIME_OFFSET);
        i_gid = buffer.getShort(I_GID_OFFSET);
        i_links_count = buffer.getShort(I_LINKS_COUNT_OFFSET);

        for(int i=0; i<15; i++)
            i_block_pointer[i] = buffer.getInt(I_BLOCK_POINTERS_OFFSET + (i*4));

        i_size_upper = buffer.getInt(I_SIZE_UPPER_OFFSET);
    }

    /**
     *This method uses bitwise ANDing on FileSystem's i_mode variable value,
     *to determine what permissions a directory or file has.
     *@return a String with the full permissions of a file or directory
     */
    String readPermissions()
    {
        String permissions = "";

        if(((int)i_mode & IFSCK) == IFSCK)
            permissions = "socket";
        else if(((int)i_mode & IFLNK) == IFLNK)
            permissions = "symbolic link";
        else if(((int)i_mode & IFREG) == IFREG)
            permissions = "-";
        else if(((int)i_mode & IFBLK) == IFBLK)
            permissions = "block device";
        else if(((int)i_mode & IFDIR) == IFDIR)
            permissions = "d";
        else if(((int)i_mode & IFCHR) == IFCHR)
            permissions = "c";
        else if(((int)i_mode & IFIFO) == IFIFO)
            permissions = "fifo";

//        int[] permissionsForEachGroup = {
//                IRUSR, IWUSR, IXUSR, IRGRP, IWGRP, IXGRP, IROTH, IWOTH, IXOTH
//        };

        //USER GROUP PERMISSIONS
        permissions += ((int)i_mode & IRUSR) == IRUSR ? "r" : "-";
        permissions += ((int)i_mode & IWUSR) == IWUSR ? "w" : "-";
        permissions += ((int)i_mode & IXUSR) == IXUSR ? "x" : "-";

        //GROUP PERMISSIONS
        permissions += ((int)i_mode & IRGRP) == IRGRP ? "r" : "-";
        permissions += ((int)i_mode & IWGRP) == IWGRP ? "w" : "-";
        permissions += ((int)i_mode & IXGRP) == IXGRP ? "x" : "-";

        //OTHER GROUP PERMISSIONS
        permissions += ((int)i_mode & IROTH) == IROTH ? "r" : "-";
        permissions += ((int)i_mode & IWOTH) == IWOTH ? "w" : "-";
        permissions += ((int)i_mode & IXOTH) == IXOTH ? "x" : "-";
        permissions += ((int)i_mode & ISVTX) == ISVTX ? "t" : "";

        return permissions;
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
        return ((int) i_mode & IFREG) == IFREG;
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
