import java.nio.*;
import java.io.*;
import java.util.*;
/**
*This class opens the Group Descriptor of a block group and reads the Inode Table Pointers for all the block groups
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/

public class GroupDescriptor
{
  private ByteBuffer buffer;
  private int group_count;                                  //The number of block groups
  private int[] gd_pointer;                                 //array Inode tables pointers
	private byte[] data;

  /**
  *Constructor of the GroupDescriptor class
  *@param bytes is the arry of bytes that the group descriptor class will use
  *@param group_count is the number of block groups, calculated from data obtained from the SuperBlock
  */
  public GroupDescriptor(byte[] bytes, int group_count)
  {
    buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
    this.group_count = group_count;
    gd_pointer = new int[group_count];
  }

  /**
  *This method reads the group descripor for each block group and adds the offset
  *of the inode table pointer to the gd_pointer[] array
  */
  public void read()
  {
    int gd_size = 32;                                      //each inode table is 32bytes long
    for (int i=0; i<group_count; i++)
    {
      gd_pointer[i] = buffer.getInt(gd_size*i+8);         //in the Group Descriptor the Inode table pointer is located 8 bytes later
    }
  }

  /**
  *@return the array of Inode Tables
  */
  public int[] getGDpointer()
  {
    return gd_pointer;
  }
}
