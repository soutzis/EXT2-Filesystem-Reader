import java.nio.*;

/**
 *This class opens the Group Descriptor of a block group and reads the Inode Table Pointers for all the block groups
 *@author Petros Soutzis, 2017-19
 */

public class GroupDescriptor
{
    private ByteBuffer buffer; //A bytebuffer to hold the group descriptor data
    private int groupCount;  //The number of block groups
    private int[] gdPointer; //array Inode tables pointers

    /**
     *Constructor of the GroupDescriptor class
     *@param bytes is the array of bytes that the group descriptor class will use
     *@param groupCount is the number of block groups, calculated from data obtained from the SuperBlock
     */
    public GroupDescriptor(byte[] bytes, int groupCount) {
        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.groupCount = groupCount;
        gdPointer = new int[groupCount];
        read(); //init
    }

    /**
     *This method reads the group descriptor for each block group and adds the offset
     *of the inode table pointer to the gdPointer[] array
     */
    private void read()
    {
        int gdSize = 32; //each inode table is 32bytes long
        int inodeTablePointerOffset = 8;
        for (int i = 0; i< groupCount; i++) {
            //In the Group Descriptor, the Inode table pointer is located 8 bytes later
            gdPointer[i] = buffer.getInt((gdSize * i) + inodeTablePointerOffset);
        }
    }

    /**
     *@return the array of Inode Tables
     */
    int[] getGDescPointer() {

        return gdPointer;
    }
}
