import java.nio.*;
import java.io.*;
import java.util.*;

public class Directory
{
  //private int[] block_pointers;
  private final long block_size = 1024;
  private final int inode_size = 128;
  private int path_offset;
  private Inode inode;
  private byte[] char_bytes;
  private String[] split_path;

  public Directory(Inode inode, String path)
  {
    this.inode = inode;
    split_path = path.split("/");
  }

  public void checkPathName() throws IOException
  {
    for(int i=1; i<split_path.length; i++)
    {
      findPath(split_path[i]);
      if(path_offset == 0)
      {
        System.out.println("BAD PATH - NO DATA FOUND");
        break;
      }
      double new_offset = Driver.getContainingBLock(path_offset);
      //int new_offset = (int) block
      byte[] inode_data = Driver.ext2.read((long)new_offset, inode_size);
      inode = new Inode(inode_data);
      inode.read();

    }
    //FileInfo f = new FileInfo(inode);
  }

  public int findPath(String path) throws IOException
  {
    int[] block_pointers = inode.getBlockPointers();
    short name_length;

    for (int i=0; i<12; i++)
    {
      if(block_pointers[i]!= 0)
      {
        byte[] data = Driver.ext2.read(((long)block_pointers[i])*block_size, (long)block_size);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int j=0; j<buffer.limit(); j+=name_length)
        {
          name_length = buffer.getShort(i+4);
          char_bytes = new byte[name_length-8];   //because length is a short, but the data for name is stored in just 1 byte. (check http://cs.smith.edu/~nhowe/262/oldlabs/ext2.html Q6)
          for(int k=0; k<char_bytes.length; k++)
          {
            char_bytes[k] = buffer.get(k+j+8);
          }

          String path_name = new String(char_bytes);
          if(path.equals(path_name))
            return path_offset = buffer.getInt(j);
        }
      }
    }
    return path_offset = 0;
  }
}
