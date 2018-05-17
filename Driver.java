import java.io.IOException;
import java.util.*;
/**
*This is the main class of the program. It provides a way to read an Ext2 Filesystem image,
*based on the path that will be provided by the user, as a string.
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/

public class Driver
{
  static Scanner scan;
  static Volume vol;                                                  //The Volume class instance, to open the file
  static Ext2File ext2;                                               //The Ext2File class instance, to return the bytes to be read from the file
  static Superblock sblock;                                           //The Superblock class instance, which reads the superblock of the file
  static GroupDescriptor gdesc;                                       //The Group Descriptor class instance, which reads the group descriptor
  static FileInfo info;                                               //The FileInfo class instance, which will locate data based on the path, and if data exists, it will print the data in UTF-8 and Hexadecimal
  static Inode inode;                                                 //The inode class instance, which containts pointers to the filesystem blocks, which contain the data that will be read and printed
  static byte[] data;                                                 //Byte array to hold the data that will be read, as returned by Ext2File's read() method
  static String path;                                                 //String variable that will hold the Path, that the program will search for in the ext2 filesystem image
  static String[] split_path;                                         //An array of strings, with each string consisting of the names of the path's directories or files
  static int block_location, block_group_count, inode_size;
  static final int root_inode = 2;                                    //The number of the inode that holds all the information about root.
  static final int block_size = 1024;                                 //The capacity of each block in the volume
  static final long superblock_offset = 1024;                         //The offset byte, at which the Superblock starts at (and block group 0)
  static final long gdescriptor_offset = 2048;                        //The offset byte at which the Group Descriptor starts at

  /**
  *A static method, used for fetching the block number (offset), that an inode points to. Otherwise, it returns 0
  *@param inode_index The number of the inode, with which this method will calculate the containing block number
  *@return the offset of the containing block
  */
  public static int getContainingBLock(int inode_index)
  {
    int s_inodes_count = sblock.getInodeCount();                        //Total number of inodes in the file
    int s_indodes_per_group = sblock.getInodesPerGroup();               //Total number of inodes per block group
    int s_inode_size = sblock.getInodeSize();                           //Size of inodes, as read from the superblock
    int pointer_div;                                                    //the block group that the inode resides in
    int[] group_descriptor_pointer = gdesc.getGDpointer();              //the group descriptor table pointers
    int i_table_pointer;                                                //the inode table pointer of block group n.
    double pointer;                                                     //the index of the inode as a double to avoid data loss, when calculating for block group 0
    double containing_block;                                            //the number of the containing block, as a double to avoid data loss, when calculating for block group 0

		if (inode_index >= 2)                                               //only perform calculations for inodes 2 and up
    {
      if(inode_index < s_inodes_count)                                  //validate inode index, by checking if it is not any bigger that the total number of inodes
      {
        inode_index -= 1;                                               //because inodes start counting from 1, but start from 0 in the inode table
        pointer_div = inode_index/s_indodes_per_group;                  //dividing the inode number with the number of inodes per group, to get the index of the inode in the Descriptor table
        pointer = inode_index % s_indodes_per_group;                    //the remainder of the above equation will be used in calculating the containing block below. http://cs.smith.edu/~nhowe/262/oldlabs/ext2.html
        i_table_pointer = group_descriptor_pointer[pointer_div];
			  containing_block = ((pointer * s_inode_size/block_size) + i_table_pointer) * block_size;

			  return (int)containing_block;
      }
		}
		return 0;
	}

  public static void main (String[] args) throws IOException
  {
    while(true)
    {
      System.out.print("Enter the path to search for: ");
      scan = new Scanner(System.in);
      path = scan.next();
      if(path.equals("exit") || path.equals("quit") || path.equals("end") || path.equals("stop"))
      {
        System.out.print("\nExt2 filesystem image scanning was terminated by the user\n");
        break;
      }
      else
      {
        vol = new Volume("ext2fs");
        ext2 = new Ext2File(vol, path);

        split_path = path.split("/");

        data = ext2.read(superblock_offset, block_size);
        sblock = new Superblock(data);
        sblock.read();
        inode_size = sblock.getInodeSize();
        block_group_count = sblock.getBlockGroupCount(sblock.getBlocksCount(), sblock.getBlocksPerGroup()); //The number of block groups in the filesystem image

        data = ext2.read(gdescriptor_offset,block_size);
        gdesc = new GroupDescriptor(data, block_group_count);
        gdesc.read();
        block_location = getContainingBLock(root_inode);   //the block offset

        data = ext2.read(block_location, inode_size);
        inode = new Inode(data);
        inode.read();

        info = new FileInfo(inode, split_path);
        info.getFileInfo();
      }
    }
  }
}
