import java.nio.*;
import java.io.*;
/**
*This class reads the data of an ext2 filesystem's SuperBlock
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/

public class Superblock
{
  private short s_magic;                              //the magic number which should always be 0xef53 for an ext2 filesystem
  private int s_inodes_count;                         //Total number of inodes in filesystem
  private int s_blocks_count;                         //Total number of blocks in filesystem
  private int s_blocks_per_group;                     //Number of blocks per Group
  private int s_indodes_per_group;                    //Number of inodes per Group
  private int s_inode_size;                           //Size of each inode in bytes
  private String s_volume_name;                       //Volume label (disk name)
  private ByteBuffer buffer;                          //ByteBuffer, where the bytes to be read, will be parsed to

  /**
  *Constructor of the Superblock class
  *@param bytes is the byte array that contains the Superblock's data
  */
  public Superblock(byte[] bytes)
  {
    buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

  /**
  *This method will read all the data that is contained in the Superblock,
  *from the buffer in which the byte array was parsed
  */
  public void read()
  {
    s_magic = buffer.getShort(56);
    s_inodes_count = buffer.getInt(0);
		s_blocks_count = buffer.getInt(4);
		s_blocks_per_group = buffer.getInt(32);
		s_indodes_per_group = buffer.getInt(40);
		s_inode_size = buffer.getInt(88);
    byte[] char_bytes = new byte[16];
    for(int i=0;i<16; i++)
    {
			char_bytes[i]=buffer.get(120+i);
		}

    s_volume_name = new String(char_bytes);              //converting the bytes that contain the name to a String
  }

  /**
  *@return The total size of each Inode
  */
  public int getInodeSize()
  {
    return s_inode_size;
  }

  /**
  *@return The total number of inodes in filesystem
  */
  public int getInodeCount()
  {
    return s_inodes_count;
  }

  /**
  *@return The total number of inodes in every block group
  */
  public int getInodesPerGroup()
  {
    return s_indodes_per_group;
  }

  /**
  *@return The total number of blocks in filesystem
  */
  public int getBlocksCount()
  {
    return s_blocks_count;
  }

  /**
  *@return The total number of blocks in every block group
  */
  public int getBlocksPerGroup()
  {
    return s_blocks_per_group;
  }

  /**
  *@return The disk's name
  */
  public String getVolumeName()
  {
    return s_volume_name;
  }

  /**
  *@param num_of_blocks the total number of blocks in the filesystem
  *@param blocks_per_group the total number of blocks in every block group
  *@return The total number of block groups that the volume has
  */
  public int getBlockGroupCount(int num_of_blocks, int blocks_per_group)
  {
    int count = num_of_blocks/blocks_per_group;
    if((num_of_blocks % blocks_per_group) > 0) count++;

    return count;
  }

}
