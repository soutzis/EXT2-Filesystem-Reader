import java.nio.*;
import java.io.*;

public class Superblock
{
  private short s_magic; //offset 56
  private int s_inodes_count; //offset 0
  private int s_blocks_count; //offset 4
  private int s_blocks_per_group; //offset 32
  private int s_indodes_per_group; //offset 40
  private int s_inode_size; //offset 88
  private String s_volume_name; //offset 120 (128bit)
  private ByteBuffer buffer;

  public Superblock(byte[] bytes)
  {
    buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

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
    s_volume_name = new String(char_bytes);

    /*System.out.println("magic number: 0x"+String.format("%04x", s_magic)+"\ntotal number of inodes: "+s_inodes_count+"\ntotal number of blocks: "+s_blocks_count+"\nmax number of blocks per group: "+s_blocks_per_group+"\ntotal number of groups: "+getBlockGroupCount(s_blocks_count, s_blocks_per_group));
    System.out.println("Size of each inode is: "+s_inode_size);
    System.out.println("Volume name is\t-> "+s_volume_name+" <-");*/
  }

  public int getInodeSize()
  {
    return s_inode_size;
  }

  public int getInodeCount()
  {
    return s_inodes_count;
  }

  public int getInodesPerGroup()
  {
    return s_indodes_per_group;
  }

  public int getBlocksCount()
  {
    return s_blocks_count;
  }

  public int getBlocksPerGroup()
  {
    return s_blocks_per_group;
  }

  public String getVolumeName()
  {
    return s_volume_name;
  }
  public int getBlockGroupCount(int num_of_blocks, int blocks_per_group)
  {
    int count = num_of_blocks/blocks_per_group;
    if((num_of_blocks % blocks_per_group) > 0) count++;

    return count;
  }
}
