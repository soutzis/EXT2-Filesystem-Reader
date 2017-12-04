import java.io.IOException;

public class Driver
{
  static Volume vol;
  static Ext2File ext2;
  static Superblock sblock;
  static GroupDescriptor gdesc;
  static int block_location;
  static byte[] data;
  static final int block_size = 1024;
  static final long superblock_offset = 1024;
  static final long gdescriptor_offset = 2048;

  public static int getContainingBLock(int inode_index)
  {
    int s_inodes_count = sblock.getInodeCount();
    int s_indodes_per_group = sblock.getInodesPerGroup();
    int s_inode_size = sblock.getInodeSize();
    int block_group;                                                    //the block group that the inode resides in
    int[] group_descriptor_pointer = gdesc.getGDpointer();              //the groupdescription pointers
    int i_table_pointer;                                                //the inode table pointer of block group n.
    double pointer;                                                     //the index of the inode
    double containing_block;                                            //the number of the block that the inode points to

		if (inode_index >= 2)
    {
      if(inode_index < s_inodes_count)
      {
        inode_index -= 1; //because inodes start from 1 but something else starts from 0
        block_group = inode_index/s_indodes_per_group; //inodes start from 1, so substracting 1 (To locate in which block this inode resides)
        pointer = inode_index % s_indodes_per_group;
        i_table_pointer = group_descriptor_pointer[block_group];
			  containing_block = ((pointer * s_inode_size/block_size) + i_table_pointer) * block_size;

			  return (int)containing_block;
      }
		}
		return 0;
	}

  //public static
  public static void main (String[] args) throws IOException
  {
    vol = new Volume("ext2fs");
    ext2 = new Ext2File(vol, "petros");
    data = ext2.read(superblock_offset, block_size);
    System.out.println(ext2.size()+" bytes");
    sblock = new Superblock(data);
    sblock.read();
    int block_group_count = sblock.getBlockGroupCount(sblock.getBlocksCount(), sblock.getBlocksPerGroup());
    data = ext2.read(gdescriptor_offset,block_size);
    gdesc = new GroupDescriptor(data, block_group_count);
    gdesc.read();
    block_location = getContainingBLock(2);
    /*Inode inode = new Inode(data);
    data = ext2.read((long)rootInfo_inode, 128);
    inode.read();
    System.out.println(inode.readPermissions()+" "+inode.getDate());*/

    System.out.println("containing block is: "+block_location);
    System.out.println("buffer pointer is: "+ext2.position());
  }
}
