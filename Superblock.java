import java.nio.*;

/**
 *This class reads the data of an ext2 filesystem's SuperBlock
 *@author Petros Soutzis, 2017-19
 */

@SuppressWarnings("FieldCanBeLocal")
public class Superblock {
    //the magic number which should always be 0xef53 for an ext2 filesystem
    private short sMagic;
    //Total number of inodes in filesystem
    private int inodeCount;
    //Total number of blocks in filesystem
    private int blockCount;
    //Number of blocks per Group
    private int blocksPerGroup;
    //Number of inodes per Group
    private int inodesPerGroup;
    //Size of each inode in bytes
    private int sInodeSize;
    //Volume label (disk name)
    private String volumeName;
    //ByteBuffer, where the bytes to be read, will be parsed to
    private ByteBuffer buffer;

    /*Offset Constants for extracting the information from the SuperBlock*/
    private final int S_MAGIC_OFFSET = 56;
    private final int S_INODE_COUNT_OFFSET = 0;
    private final int S_BLOCK_COUNT_OFFSET = 4;
    private final int S_BLOCKS_PER_GROUP_OFFSET = 32;
    private final int S_INODES_PER_GROUP_OFFSET = 40;
    private final int S_INODE_SIZE_OFFSET = 88;
    private final int S_FILESYSTEM_NAME_OFFSET = 120;
    private final int S_FILESYSTEM_NAME_LENGTH = 16;


    /**
     *Constructor of the Superblock class
     *@param bytes is the byte array that contains the Superblock's data
     */
    public Superblock(byte[] bytes) {
        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     *This method will read all the data that is contained in the Superblock,
     *from the buffer in which the byte array was parsed
     */
    public void read() {
        sMagic = buffer.getShort(S_MAGIC_OFFSET);
        inodeCount = buffer.getInt(S_INODE_COUNT_OFFSET);
        blockCount = buffer.getInt(S_BLOCK_COUNT_OFFSET);
        blocksPerGroup = buffer.getInt(S_BLOCKS_PER_GROUP_OFFSET);
        inodesPerGroup = buffer.getInt(S_INODES_PER_GROUP_OFFSET);
        sInodeSize = buffer.getInt(S_INODE_SIZE_OFFSET);

        //Get the Volume name
        byte[] char_bytes = new byte[S_FILESYSTEM_NAME_LENGTH];

        for(int i=0; i<S_FILESYSTEM_NAME_LENGTH; i++) {
            //Get the characters 1 by 1
            char_bytes[i]=buffer.get(S_FILESYSTEM_NAME_OFFSET + i);
        }

        //converting the bytes that contain the name to a String
        volumeName = new String(char_bytes);
    }

    /**
     *@return The total size of each Inode
     */
    int getInodeSize() {

        return sInodeSize;
    }

    /**
     *@return The total number of inodes in filesystem
     */
    int getInodeCount() {

        return inodeCount;
    }

    /**
     *@return The total number of inodes in every block group
     */
    int getInodesPerGroup() {

        return inodesPerGroup;
    }

    /**
     *@return The total number of blocks in filesystem
     */
    int getBlockCount() {

        return blockCount;
    }

    /**
     *@return The total number of blocks in every block group
     */
    int getBlocksPerGroup() {

        return blocksPerGroup;
    }

    /**
     *@return The disk's name
     */
    String getVolumeName() {

        return volumeName;
    }

    /**
     *@param num_of_blocks the total number of blocks in the filesystem
     *@param blocks_per_group the total number of blocks in every block group
     *@return The total number of block groups that the volume has
     */
    int getBlockGroupCount(int num_of_blocks, int blocks_per_group) {
        //Get the total number of blocks, divided with the number of blocks per group
        int count = num_of_blocks/blocks_per_group;

        //If the remainder of the above division is not 0, then add "1" to the count.
        if((num_of_blocks % blocks_per_group) != 0)
            count++;

        return count;
    }

}
