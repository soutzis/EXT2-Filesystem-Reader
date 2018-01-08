import java.nio.*;
import java.io.*;
import java.util.*;
/**
*This is a class that will read each directory or file name in a path, and if that pathname exists
*in the volume, it will read and print the data contained there; both in UTF-8 and Hexadecimal.
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/

public class FileInfo
{
  private Inode inode, iData;

  private String[] split_path;                   //Directory and file names stored in the right sequence in this String array

  private boolean reg_file;                      //Boolean to indicate if the program is trying to read a file or a directory
  private boolean dataexists = true;             //Boolean to indicate if the pathname provided leads to existing data

  private short dir_length, rec_length;          //The size of a directory
  private int inode_index, inode_of_path;
  private int[] block_pointers;
  private final int block_size = 1024;
  private final int inode_size;
  private final int one_byte = 4;               //equivalent of 32 bits in bytes (an int)
  private final int two_bytes = 8;              //equivalent of 64 bits in bytes (a long)
  private final int half_byte = 2;              //equivalent of 16 bits in bytes (a short)

  private ArrayList<byte[]> hex_data;           //ArrayList of byte arrays, to hold all the byte arrays whose data was printed by the program
  private byte name_length, namelength;         //The length of the name of a directory or file
  private byte[] char_bytes, inode_data;

  /**
  *Constructor of the FileInfo class
  *@param inode is the inode that this class will use to read data
  *@param path is the array of names of directoriesandfiles, that the program will compare to the names of directories and files in the volume
  *@throws IOException e
  */
  public FileInfo(Inode inode, String[] path) throws IOException
  {
    this.inode = inode;
    this.inode_size = Driver.inode_size;
    split_path = path;
    hex_data = new ArrayList<byte[]>();
  }

  /**
  *This method will call findPath() method, to get the number of the inode, if the names in spli_path array match the names in the volume
  *If the inode number is 0, the program will be terminated. The method will then parse the bytes to read, to the Inode instance, by calculating
  *the offset based on the inode number that the path returns. If data exists, this method will call the readBlockData() method,
  *to read and print the data in a human-readable way and then it will call the readHexData, in order to read and print that same data in Hexadecimal format
  *@throws IOException e
  */
  public void getFileInfo() throws IOException
  {
    for(int i=1; i<split_path.length; i++)                      //i starts from 1, because the first item in the array is whitespace
    {
      findPath(split_path[i]);
      if(inode_of_path == 0)
      {
        System.out.println("BAD PATH - NO DATA FOUND");
        dataexists = false;
        break;
      }

      inode_data = Driver.ext2.read((Driver.getContainingBLock(inode_of_path)), inode_size);
      inode = new Inode(inode_data);
      inode.read();
      reg_file = inode.isFile();
    }
    if(dataexists)
    {
      System.out.println("\nTotal number of inodes is: "+Driver.sblock.getInodeCount());
      System.out.println("Total number of inodes per group is: "+Driver.sblock.getInodesPerGroup());
      System.out.println("Total size of inodes is: "+Driver.inode_size);
      System.out.println("Total number of blocks is: "+Driver.sblock.getBlocksCount());
      System.out.println("Total number of blocks per group is: "+Driver.sblock.getBlocksPerGroup());
      System.out.println("VOLUME NAME: "+Driver.sblock.getVolumeName());
      System.out.println("Total number of block groups is: "+Driver.block_group_count);
      int[] inode_table = Driver.gdesc.getGDpointer();
      for(int i=0; i<inode_table.length; i++)
        System.out.println("Inode Table "+(i+1)+" offset in Group Descriptor is: "+inode_table[i]);
      System.out.print("\n");
      readBlockData(inode);
      readHexData(hex_data);
    }
  }

  /**
  *This class calculates the inode number, needed to read the data contained in the block that the inode points to
  *@param path is the name of the directory or file to look for
  *@throws IOException e
  *@return the inode number that points to the data requested in the path
  */
  public int findPath(String path) throws IOException
  {
    block_pointers = inode.getBlockPointers();
    inode_of_path = 0;

    for (int i=0; i<12; i++)                                                       //the names of the directories or files are small and will always be pointed by the direct pointers of the inode
    {
      if(block_pointers[i] != 0)
      {
        byte[] data = Driver.ext2.read((block_pointers[i]*block_size), block_size); //multiplaying the block pointer offset by the max block size to get the correct offset
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        rec_length = buffer.getShort(4);

        for(int j=0; j<buffer.limit(); j+=rec_length)                               //Jumping +rec_length each time, to read the next name, if exists
        {
          rec_length = buffer.getShort(j+one_byte);                                 //The value for jumping to the next directory location
          name_length = buffer.get(j+one_byte+half_byte);
          char_bytes = new byte[name_length];                                       //(check http://cs.smith.edu/~nhowe/262/oldlabs/ext2.html Q6)
          for(int k=0; k<char_bytes.length; k++)
          {
            char_bytes[k] = buffer.get(k+j+two_bytes);                              //get the character array, the file or directory name for split_path[i]
          }

          if(path.equals(new String(char_bytes).trim()))                            //TRIM REMOVES WHITESPACE, to check if the one provided with the one discovered are equal
          {
            inode_of_path = buffer.getInt(j);                                       //get the number of the inode and exit this loop
            break;
          }
        }
      }
    }
    return inode_of_path;                                                           //returns integer 0 to indicate that there was no match
  }

  /**
  *This method will read byte arrays from a list of byte arrays and print the content in a
  *readable Hexadecimal format, with 26 hex characters in each line for readability
  *@param raw is the ArrayList with bytes that this method will read and print as hex
  */
  public void readHexData(ArrayList<byte[]> raw)
  {
    int counter = 0;
    System.out.print("\n\t\t\t** READABLE HEX FORMAT **\n");
    for (byte[] data : raw)
    {
      for (int i=0; i<data.length; i++)
      {
        if(counter%26 == 0 && counter != 0)
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
    }
    System.out.print("\n\t\t\t************************* \n");
  }

  /**
  *This method will read the block pointers values of an inode and if a pointer
  *points to real data, it will call the appropriate methods to print that data.
  *@param inode is the Inode, whose block pointers will be read
  *@throws IOException e
  */
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

  /**
  *This method will print the data contained in a file or the metadata from the inode, if it is a Directory
  *@param startByte is the offset of the data, obtained by the direct pointers to data in the inode.
  *@throws IOException e
  */
  public void printBlockData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);       //mulitplying offset by 1024, to get the correct block number (the correct offset)
    hex_data.add(block_data);

    if (reg_file)
    {
      String str = new String(block_data).trim();                                //the bytes converting to a String and trim() removes whitespace
      System.out.print(str);
    }

    else if(!reg_file)
    {
      ByteBuffer buffer = ByteBuffer.wrap(block_data);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      dir_length = buffer.getShort(one_byte);

      for(int i=0; i<buffer.limit(); i+=dir_length)
      {
        inode_index = buffer.getInt(i);
        dir_length = buffer.getShort(i+one_byte);                              //because the index is 4bytes long
        namelength = buffer.get(i+one_byte+half_byte);                         // 8 bits in size, located after dir_length in the
        byte[] char_bytes = new byte[namelength];

        for(int j=0; j<char_bytes.length; j++)
        {
          char_bytes[j] = buffer.get(j+i+two_bytes);                           //fetch each char in the char_array of bytes
        }

        int containing_block = Driver.getContainingBLock(inode_index);
        byte[] other_data = Driver.ext2.read(containing_block, inode_size);
        iData = new Inode(other_data);
        iData.read();
        long file_size = ((long)iData.getSizeUpper() << 32) | ((long)iData.getSizeLower() & 0xFFFFFFFFL);

        System.out.print(iData.readPermissions()+"\t");                        //prints the metadata from the inode
        System.out.print(iData.getHardLinks()+"\t");
        System.out.print(iData.getUid()+"\t");
        System.out.print(iData.getGid()+"\t");
        System.out.print(file_size+"\t");
        System.out.print(iData.getDate()+"\t");
        System.out.print(new String(char_bytes).trim()+" \n");
      }
    }
  }

  /**
  *This method will read the indirect data block and if data exists it will call printBlockData()
  *@param startByte is the offset of the data, obtained by the block pointers from the inode.
  *@throws IOException e
  */
  public void readIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=one_byte)
    {
      if(buffer.getInt(i) != 0)
        printBlockData(buffer.getInt(i));
    }
  }

  /**
  *This method will read the double indirect data block and if data exists it will call readIndirectData()
  *@param startByte is the offset of the data, obtained by the block pointers from the inode.
  *@throws IOException e
  */
  public void readDblIndirectData(int startByte) throws IOException
  {
    byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);
    ByteBuffer buffer = ByteBuffer.wrap(block_data);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for(int i=0; i<buffer.limit(); i+=one_byte)
    {
      if(buffer.getInt(i) != 0)
        readIndirectData(buffer.getInt(i));
    }
  }

  /**
  *This method will read the triple indirect data block and if data exists it will call readDblIndirectData()
  *@param startByte is the offset of the data, obtained by the block pointers from the inode.
  *@throws IOException e
  */
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
