import java.nio.*;
import java.io.*;
import java.util.*;

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
	private short i_links_count;                //'hard link references to file' counter
  private int[]	i_block_pointer;              //direct pointers to data blocks
  private int i_size_upper;                   //file size in bytes (lower 64 bits)
  private boolean reg_file;
  private String file_permissions;            //the permissions mode granted to the user/group, as a readable String
  private ByteBuffer buffer;
  //private byte[] inode_bytes;                 //byte array where the inode will be read from

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

  public Inode(byte[] bytes)
  {
    //this.inode_bytes = bytes;
    buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    i_block_pointer = new int[15];
    reg_file = false;
  }

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
    for(int i=0; i<12; i++)
    {
      i_block_pointer[i] = buffer.getInt(40 + (i*4));
    }
    i_block_pointer[12] = buffer.getInt(88);
    i_block_pointer[13] = buffer.getInt(92);
    i_block_pointer[14] = buffer.getInt(96);
    i_size_upper = buffer.getInt(108);
  }

  public String readPermissions()
  {
    String permissions = "";
    if((i_mode & IFSCK) == i_mode) permissions="socket";
    if((i_mode & IFLNK) == i_mode) permissions="symbolic link";
    if((i_mode & IFREG) == i_mode)
    {
      permissions="-";
      reg_file = true;
    }
    if((i_mode & IFBLK) == i_mode) permissions="block device";
    if((i_mode & IFDIR) == i_mode) permissions="d";
    if((i_mode & IFCHR) == i_mode) permissions="c";
    if((i_mode & IFIFO) == i_mode) permissions="fifo";

    if((i_mode & IRUSR) == i_mode) permissions+="r"; else permissions+="-";
    if((i_mode & IWUSR) == i_mode) permissions+="w"; else permissions+="-";
    if((i_mode & IXUSR) == i_mode) permissions+="x";
    if((i_mode & ISUID) == i_mode)
    {
      if(permissions.endsWith("x"))
        permissions = permissions.substring(0, permissions.length() - 1);
      permissions+="s";
    } else permissions+="-";
    if((i_mode & IRGRP) == i_mode) permissions+="r"; else permissions+="-";
    if((i_mode & IWGRP) == i_mode) permissions+="w"; else permissions+="-";
    if((i_mode & IXGRP) == i_mode) permissions+="x";
    if((i_mode & ISGID) == i_mode)
    {
      if(permissions.endsWith("x"))
        permissions = permissions.substring(0, permissions.length() - 1);
      permissions+="s";
    } else permissions+="-";
    if((i_mode & IROTH) == i_mode) permissions+="r"; else permissions+="-";
    if((i_mode & IWOTH) == i_mode) permissions+="w"; else permissions+="-";
    if((i_mode & IXOTH) == i_mode) permissions+="x"; else permissions+="-";
    if((i_mode & ISVTX) == i_mode) permissions+="t";

    return permissions;
  }

  public int[] getBlockPointers()
  {
    return i_block_pointer;
  }

  public String getUid()
  {
		if(i_uid == 0) return "root";
    else return "user";
	}

  public String getGid()
  {
    if(i_gid == 0) return "root";
    else return "group";
	}

  public int getSizeLower()
  {
		return i_size_lower;
	}

  public boolean file()
  {
    return reg_file;
  }

  public Date getDate()
  {
    Date d = new Date((long)i_mtime*1000);
		return d;
	}

  public short getHardLinks()
  {
		return i_links_count;
	}

}
