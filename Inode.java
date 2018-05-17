import java.nio.*;
import java.io.*;
import java.util.*;
/**
*This class reads an Inode and all the metadata it provides
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/

public class Inode
{
  private short i_mode;                       //file mode
  private short i_uid;                        //user ID
  private int	i_size_lower;                   //file size in bytes (lower 32 bits)
  private int i_atime;                        //time of last access
  private int	i_ctime;                        //creation time
  private int i_mtime;                        //last modified
  private int	i_dtime;                        //time of file deletion
	private short i_gid;                        //group ID of owners
	private short i_links_count;                //number of'hard link references to file'
  private int[]	i_block_pointer;              //pointers to data blocks or to other pointer blocks
  private int i_size_upper;                   //file size in bytes (lower 64 bits)
  private ByteBuffer buffer;

  private int IFSCK = 0xC000;                 // Socket file mode
  private int IFLNK = 0xA000;                 // Symbolic Link file mode
  private int IFREG = 0x8000;                 // Regular File file mode
  private int IFBLK = 0x6000;                 // Block Device file mode
  private int IFDIR = 0x4000;                 // Directory file mode
  private int IFCHR = 0x2000;                 // Character Device file mode
  private int IFIFO = 0x1000;                 // FIFO file mode

  private int ISUID = 0x0800;                 // Set process User ID file mode
  private int ISGID = 0x0400;                 // Set process Group ID file mode
  private int ISVTX = 0x0200;                 // Sticky bit file mode

  private int IRUSR = 0x0100;                 // User read file mode
  private int IWUSR = 0x0080;                 // User write file mode
  private int IXUSR = 0x0040;                 // User execute file mode

  private int IRGRP = 0x0020;                 // Group read file mode
  private int IWGRP = 0x0010;                 // Group write file mode
  private int IXGRP = 0x0008;                 // Group execute file mode

  private int IROTH = 0x0004;                 // Others read file mode
  private int IWOTH = 0x0002;                 // Others wite file mode
  private int IXOTH = 0x0001;                 // Others execute file mode

  /**
  *Constructor of the inode class
  *@param bytes is the byte array that contains the inode's data
  */
  public Inode(byte[] bytes)
  {
    buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    i_block_pointer = new int[15]; // The inode has 15 pointers that point to data
  }

  /**
  *This method will read all the data that is contained in the inode, from the buffer, in which the byte array was parsed
  */
  public void read()
  {
    i_mode = buffer.getShort(0);
    i_uid = buffer.getShort(2);
    i_size_lower = buffer.getInt(4);
    i_atime = buffer.getInt(8);
    i_ctime = buffer.getInt(12);
    i_mtime = buffer.getInt(16);
    i_dtime = buffer.getInt(20);
    i_gid = buffer.getShort(24);
    i_links_count = buffer.getShort(26);
    for(int i=0; i<15; i++) i_block_pointer[i] = buffer.getInt(40 + (i*4));
    i_size_upper = buffer.getInt(108);
  }

  /**
  *This method uses bitwise ANDing on FileSystem's i_mode variable value,
  *to determing what permissions a directory or file has.
  *@return a String with the full permissions of a file or directory
  */
  public String readPermissions()
  {
    String permissions = "";

    if(((int)i_mode & IFSCK) == IFSCK) permissions="socket";
    if(((int)i_mode & IFLNK) == IFLNK) permissions="symbolic link";
    if(((int)i_mode & IFREG) == IFREG) permissions = "-";
    if(((int)i_mode & IFBLK) == IFBLK) permissions="block device";
    if(((int)i_mode & IFDIR) == IFDIR) permissions="d";
    if(((int)i_mode & IFCHR) == IFCHR) permissions="c";
    if(((int)i_mode & IFIFO) == IFIFO) permissions="fifo";

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
  public int[] getBlockPointers()
  {
    return i_block_pointer;
  }

  /**
  *@return If the user has root access or not
  */
  public String getUid()
  {
    return i_uid == 0 ? "root" : "user";
	}

  /**
  *@return If the guest has root access or not
  */
  public String getGid()
  {
    return i_gid == 0 ? "root" : "group";
	}

  /**
  *@return the file size (lower 32 bits)
  */
  public int getSizeLower()
  {
		return i_size_lower;
	}

  /**
  *@return the file size (upper 32 bits)
  */
  public int getSizeUpper()
  {
    return i_size_upper;
  }

  /**
  *@return if this is a file
  */
  public boolean isFile()
  {
    //boolean is primitive, so used method Boolean
    return new Boolean(((int)i_mode & IFREG) == IFREG ? true : false);
  }

  /**
  *@return the date that this file was lastly modified
  */
  public Date getDate()
  {
		return new Date((long)i_mtime*1000);
	}

  /**
  *@return the number of hard link references to a file
  */
  public short getHardLinks()
  {
		return i_links_count;
	}

}
