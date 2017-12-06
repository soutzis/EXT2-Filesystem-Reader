import java.nio.*;
import java.io.*;
import java.util.*;

public class FileInfo
{
  private Inode inode;
  private int inode_index, counter, size, path_offset;
  private final int block_size = 1024;
  private final int inode_size = 128;
  private int[] block_pointers;
  private short length, links_count;
  private String permissions, name, u_id, g_id, path;
  private Date date;
  private String[] split_path;
  private byte[] char_bytes;
  public FileInfo(Inode inode, String path) throws IOException
  {
    this.inode = inode;
    this.path = path;
  	if (path.equals("/"))
      path += ".";
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
    readBlockData(inode);
  }

  public int findPath(String path) throws IOException
  {
    int[] block_pointers = inode.getBlockPointers();
    short name_length;

    for (int i=0; i<12; i++)
    {
      if(block_pointers[i]!= 0)
      {
        byte[] data = Driver.ext2.read((block_pointers[i])*block_size, block_size);
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


  public void readBlockData(Inode inode) throws IOException
  {
    //counter = 0;
    //this.inode = inode;
    block_pointers = inode.getBlockPointers();

    /*for(int i=0; i<12; i++)   //check if direct pointers point to data
    {
      if (block_pointers[i]==0)
        counter+=1;
    }

    if(counter < 12)    //DO I NEED COUNTER ?
    {*/
    for (int i=0; i<12; i++)
    {
      if(block_pointers[i]!= 0)
        printBlockData(block_pointers[i]);
    }
    //}
    if(block_pointers[12] != 0)
      readIndirectData(block_pointers[12]);
    if(block_pointers[13] != 0)
      readDblIndirectData(block_pointers[13]);
    if(block_pointers[14] != 0)
      readTrplIndirectData(block_pointers[14]);
  }

  public void printBlockData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read((long)startByte*block_size, (long)block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    if (inode.file())
      System.out.println(new String(block_data));
    //if(inode.file() == false)
    //{
      for(int i=0; i<buffer.limit(); i+=length)
      {
        inode_index = buffer.getInt(i);
        length = buffer.getShort(i+4); //because the index is 4bytes long
        byte[] char_bytes = new byte[length-8]; //8 bytes long

        for(int j=0; j<char_bytes.length; j++)
        {
          char_bytes[j] = buffer.get(j+8+i);  //fetch each char in the char_array of bytes
        }

        int containing_block = Driver.getContainingBLock(inode_index);  //should this be an integer/double or long ?
        byte[] other_data = Driver.ext2.read((long)containing_block, (long)block_size);
        Inode iData = new Inode(other_data);
        iData.read();

        permissions = iData.readPermissions();
        links_count = iData.getHardLinks();
        u_id = iData.getUid();
        g_id = iData.getGid();
        size = iData.getSize();
        date = iData.getDate();
        name = new String(char_bytes);
        System.out.print(permissions+"\t");
        System.out.print(links_count+"\t");
        System.out.print(u_id+"\t");
        System.out.print(g_id+"\t");
        System.out.print(size+"\t");
        System.out.print(date+"\t");
        System.out.print(name+"\t\n");
      }
    //}
  }

  public void readIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read((long)startByte*block_size, (long)block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=4)
    {
      if(buffer.getInt(i) != 0)
        printBlockData(buffer.getInt(i));     //print contents of file or directory data
    }
  }

  public void readDblIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read((long)startByte*block_size, (long)block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=4)
    {
      if(buffer.getInt(i) != 0)
        readIndirectData(buffer.getInt(i));     //print contents of file or directory data
    }
  }

  public void readTrplIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read((long)startByte*block_size, (long)block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=4)
    {
      if(buffer.getInt(i) != 0)
        readDblIndirectData(buffer.getInt(i));     //print contents of file or directory data
    }
  }

}
