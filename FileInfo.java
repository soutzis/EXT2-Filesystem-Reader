import java.nio.*;
import java.io.*;
import java.util.*;

public class FileInfo
{
  private Inode inode, iData;
  private int inode_index, inode_of_path, new_offset;
  private int[] block_pointers;
  private short dir_length, rec_length;
  private String[] split_path;
  private byte name_length, namelength;
  private byte[] char_bytes, inode_data, hex_data;
  private boolean reg_file;
  private boolean dataexists = true;
  private final int block_size = 1024;
  private final int inode_size = 128;
  private final int one_byte = 4;   //equivalent of 32 bits in bytes (an int)
  private final int two_bytes = 8;  //equivalent of 64 bits in bytes (a long)
  private final int half_byte = 2;  //equivalent of 16 bits in bytes (a short)
  public FileInfo(Inode inode, String[] path) throws IOException
  {
    this.inode = inode;
    split_path = path;
  }

  public void getFileInfo() throws IOException
  {
    for(int i=1; i<split_path.length; i++)
    {
      findPath(split_path[i]);
      if(inode_of_path == 0)
      {
        System.out.println("BAD PATH - NO DATA FOUND");
        dataexists = false;
        break;
      }
      new_offset = Driver.getContainingBLock(inode_of_path);

      inode_data = Driver.ext2.read(new_offset, inode_size);
      inode = new Inode(inode_data);
      inode.read();
      reg_file = inode.isFile();
    }
    if(dataexists)
    {
      readBlockData(inode);
      if(reg_file)
        readHexData(hex_data);
    }
  }

  public int findPath(String path) throws IOException
  {
    block_pointers = inode.getBlockPointers();
    inode_of_path = 0;

    for (int i=0; i<12; i++)
    {
      if(block_pointers[i] != 0)
      {
        byte[] data = Driver.ext2.read((block_pointers[i]*block_size), block_size); //multiplaying by block size to get the correct offset
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        rec_length = buffer.getShort(4);

        for(int j=0; j<buffer.limit(); j+=rec_length)
        {
          rec_length = buffer.getShort(j+one_byte);  //fetching this value to jump to the next dir
          name_length = buffer.get(j+one_byte+half_byte);
          char_bytes = new byte[name_length];   //because length of the name is stored in a byte (check http://cs.smith.edu/~nhowe/262/oldlabs/ext2.html Q6)
          for(int k=0; k<char_bytes.length; k++)
          {
            char_bytes[k] = buffer.get(k+j+two_bytes);  //get the character array of the file or dir that you are currently at -> (split_path[i])
          }
                                                //System.out.println("DEBUGG TEST: char_bytes[] :"+new String(char_bytes));
          if(path.equals(new String(char_bytes).trim()))     //JESUS CHRIST !!! TRIM ????? REMEMBER THIS ! IMPORTANT
          {
            inode_of_path = buffer.getInt(j);
            break;
          }
        }
      }
    }
    return inode_of_path;
  }

  public void readHexData(byte[] data)
  {
    /*for(int j=1; j<27; j++)
    {
      System.out.print(String.format("%c", data[j]));
    }*/
    int counter = 0;
    System.out.print("\n\t\t\t** READABLE HEX FORMAT **");
    for (int i=0; i<data.length; i++)
    {
      if(counter%26 == 0)
      {
        System.out.print("\n");
      }
      if(data[i] != 0x00)
      {
        System.out.print(String.format("%02X ", data[i]));
        counter += 1;
      }
    }
    while(counter%26 != 0)
    {
      System.out.print("XX ");
      counter += 1;
    }
    System.out.print("\n\t\t\t************************* ");
  }

  public void readBlockData(Inode inode) throws IOException
  {
    block_pointers = inode.getBlockPointers();

    for (int i=0; i<12; i++)
    {
      if(block_pointers[i]!= 0)
        printBlockData(block_pointers[i]);
    }
    if(block_pointers[12] != 0)
      readIndirectData(block_pointers[12]);
    if(block_pointers[13] != 0)
      readDblIndirectData(block_pointers[13]);
    if(block_pointers[14] != 0)
      readTrplIndirectData(block_pointers[14]);
  }

  public void printBlockData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);
    hex_data = block_data;

    if (reg_file)
    {
      System.out.println("\n"+(new String(block_data).trim()));    //get rid of extra bytes that are printed as whitespace with trim()
    }

    else if(!reg_file)
    {
      ByteBuffer buffer = ByteBuffer.wrap(block_data);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      dir_length = buffer.getShort(one_byte);
      for(int i=0; i<buffer.limit(); i+=dir_length)
      {
        inode_index = buffer.getInt(i);
        dir_length = buffer.getShort(i+one_byte); //because the index is 4bytes long
        namelength = buffer.get(i+one_byte+half_byte);      // 8 bits in size, located after dir_length in the
        byte[] char_bytes = new byte[namelength];

        for(int j=0; j<char_bytes.length; j++)
        {
          char_bytes[j] = buffer.get(j+i+two_bytes);  //fetch each char in the char_array of bytes
        }

        int containing_block = Driver.getContainingBLock(inode_index);  //should this be an integer/double or long ?
        byte[] other_data = Driver.ext2.read(containing_block, block_size);
        iData = new Inode(other_data);
        iData.read();

        System.out.print(iData.readPermissions()+"\t");
        System.out.print(iData.getHardLinks()+"\t");
        System.out.print(iData.getUid()+"\t");
        System.out.print(iData.getGid()+"\t");
        System.out.print(iData.getSize()+"\t\t");
        System.out.print(iData.getDate()+"\t");
        System.out.print(new String(char_bytes).trim()+"\t\n");
      }
    }
  }

  public void readIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=one_byte)
    {
      if(buffer.getInt(i) != 0)
        printBlockData(buffer.getInt(i));     //print contents of file or directory data
    }
  }

  public void readDblIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=one_byte)
    {
      if(buffer.getInt(i) != 0)
        readIndirectData(buffer.getInt(i));     //print contents of file or directory data
    }
  }

  public void readTrplIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=one_byte)
    {
      if(buffer.getInt(i) != 0)
        readDblIndirectData(buffer.getInt(i));     //print contents of file or directory data
    }
  }
}
