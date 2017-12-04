import java.nio.*;
import java.io.*;
import java.util.*;

public class GroupDescriptor
{
  private ByteBuffer buffer;
  private int group_count;
  private int[] gd_pointer;
	private byte[] data;

  public GroupDescriptor(byte[] bytes, int group_count)
  {
    buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
    this.group_count = group_count;
    gd_pointer = new int[group_count];
  }

  public void read()
  {
    int table_size = 32; //each table has a size of 4 bytes
    for (int i=0; i<group_count; i++)
    {
      gd_pointer[i] = buffer.getInt(8+table_size*i);
      System.out.println("Block "+i+" :"+gd_pointer[i]);
    }
  }

  public int[] getGDpointer()
  {
    return gd_pointer;
  }
}
