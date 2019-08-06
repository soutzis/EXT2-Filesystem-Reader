import java.nio.*;
import java.io.*;
import java.util.ArrayList;

/**
 * This class has methods for reading the contents of the disk.
 *@author Petros Soutzis
 */

public class FileInfo {

    /**
     *This method will readBytes byte arrays from a list of byte arrays and print the content in a
     *readable Hexadecimal format, with 26 hex characters in each line for readability
     *@param raw is the ArrayList with bytes that this method will readBytes and print as hex
     */
     void readHexData(ArrayList<byte[]> raw) {
        int counter = 0;
        System.out.print("\n\nHex representation:\n");
        for (byte[] data : raw) {
            for (byte datum : data) {
                if (counter % 26 == 0 && counter != 0) {
                    System.out.print("\n");
                }
                if (datum != 0x00) {
                    System.out.print(String.format("%02X ", datum));
                    counter += 1;
                }
            }
            while(counter%26 != 0) {
                System.out.print("XX ");
                counter += 1;
            }
        }
        System.out.println("\n");
    }

    /**
     * This method will be used to display contents of a directory
     * @param inode The inode of the given path
     * @param inodeSize The size of the inode
     * @param ext2 The disk
     * @param superblock The superblock
     * @param groupDescriptor The group descriptor
     * @throws IOException If the disk can't be read
     */
    static void readDirectoryData(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                                  GroupDescriptor groupDescriptor) throws IOException {

         readBlockData(inode, inode.isFile(), inodeSize, ext2, superblock, groupDescriptor);
    }

    /**
     * This method will be used to print the contents of a file
     * @param inode The inode of the given path
     * @param inodeSize The size of the inode
     * @param ext2 The disk
     * @param superblock The superblock
     * @param groupDescriptor The group descriptor
     * @param path The path that was entered by the user
     * @throws IOException If the disk can't be read
     */
    static void readFileData(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                                  GroupDescriptor groupDescriptor, String path) throws IOException {
        if(!inode.isFile())
            System.out.println(path+": Is a directory.");
        else
            readBlockData(inode, inode.isFile(), inodeSize, ext2, superblock, groupDescriptor);
    }

    /**
     *This method will readBytes the block pointers values of an inode and if a pointer
     *points to real data, it will call the appropriate methods to print that data.
     *@param inode is the Inode, whose block pointers will be readBytes
     *@throws IOException e
     */
    private static void readBlockData(Inode inode, boolean isFile, int inodeSize, Ext2File ext2, Superblock superblock,
                              GroupDescriptor groupDescriptor) throws IOException {

        //The block pointers
        int[] blockPointers = inode.getBlockPointers();

        for (int i=0; i<12; i++) {
            if(blockPointers[i]!= 0)
                printBlockData(blockPointers[i], inodeSize, isFile, ext2, superblock, groupDescriptor);
        }
        /*If indirect pointer value is 0, then it means that it does not point to any data*/
        if(blockPointers[12] != 0)
            readIndirectData(blockPointers[12], inodeSize, isFile, ext2, superblock, groupDescriptor);
        if(blockPointers[13] != 0)
            readDoubleIndirectData(blockPointers[13], inodeSize, isFile, ext2, superblock, groupDescriptor);
        if(blockPointers[14] != 0)
            readTripleIndirectData(blockPointers[14], inodeSize, isFile, ext2, superblock, groupDescriptor);
    }

    /**
     *This method will print the data contained in a file or the metadata from the inode, if it is a Directory
     *@param blockNumber is the offset of the data, obtained by the direct pointers to data in the inode.
     *@throws IOException e
     */
    private static void printBlockData(int blockNumber, int inodeSize, boolean isFile, Ext2File ext2,
                               Superblock superblock, GroupDescriptor groupDescriptor) throws IOException {
        //multiplying offset by 1024, to get the correct block number (the correct offset)
        byte[] blockData = ext2.readBytes(blockNumber * Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);

        if (isFile) {
            //the bytes converting to a String and trim() removes whitespace
            String str = new String(blockData).trim();
            System.out.print(str);
        }

        // or if path is a directory
        else {
            ByteBuffer buffer = ByteBuffer.wrap(blockData);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            short dirLength;

            for(int i=0; i<buffer.limit(); i+=dirLength) {
                int inodeOffset = buffer.getInt(i);
                //because the index is 4 bytes long
                dirLength = buffer.getShort(i + Constants.BYTE_LENGTH);
                // 8 bits in size, located after dirLength in the
                byte nameBytes = buffer.get(i+ Constants.BYTE_LENGTH + Constants.SHORT_LENGTH);
                byte[] charBytes = new byte[nameBytes];

                for(int j=0; j<charBytes.length; j++) {
                    //fetch each char from the array of bytes
                    charBytes[j] = buffer.get(j + i + (Constants.BYTE_LENGTH * 2));
                }

                int containingBlock = Inode.getContainingBlock(inodeOffset, superblock, groupDescriptor);
                
                byte[] otherData = ext2.readBytes(containingBlock, inodeSize);
                Inode iData = new Inode(otherData);
                //Get the correct size of the file
                long fileSize = ((long)iData.getSizeUpper() << 32) | ((long)iData.getSizeLower() & 0xFFFFFFFFL);

                // Print inode metadata (permissions, date created, etc..)
                System.out.format("%-12s\t%-4d\t%-7s\t%-7s\t%-12d %-30s\t%-30s%n",
                        iData.readPermissions(),
                        iData.getHardLinks(),
                        iData.getUid(),
                        iData.getGid(),
                        fileSize,
                        iData.getDate(),
                        new String(charBytes).trim());
            }
        }
    }

    /**
     *This method will readBytes the indirect data block and if data exists it will call printBlockData()
     *@param blockNumber is the offset of the data, obtained by the block pointers from the inode.
     *@throws IOException e
     */
    private static void readIndirectData(int blockNumber, int inodeSize, boolean isFile, Ext2File ext2,
                                         Superblock superblock, GroupDescriptor groupDescriptor) throws IOException
    {
        byte[] blockData = ext2.readBytes(blockNumber * Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(blockData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0; i<buffer.limit(); i+= Constants.BYTE_LENGTH) {
            if(buffer.getInt(i) != 0)
                printBlockData(buffer.getInt(i),  inodeSize, isFile, ext2, superblock, groupDescriptor);
        }
    }

    /**
     *This method will readBytes the double indirect data block and if data exists it will call readIndirectData()
     *@param blockNumber is the offset of the data, obtained by the block pointers from the inode.
     *@throws IOException e
     */
    private static void readDoubleIndirectData(int blockNumber,  int inodeSize, boolean isFile, Ext2File ext2,
                                        Superblock superblock, GroupDescriptor groupDescriptor) throws IOException
    {
        byte[] blockData = ext2.readBytes(blockNumber* Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(blockData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0; i<buffer.limit(); i+= Constants.BYTE_LENGTH) {
            if(buffer.getInt(i) != 0)
                readIndirectData(buffer.getInt(i), inodeSize, isFile, ext2, superblock, groupDescriptor);
        }
    }

    /**
     *This method will readBytes the triple indirect data block and if data exists it will call readDoubleIndirectData()
     *@param blockNumber is the offset of the data, obtained by the block pointers from the inode.
     *@throws IOException e
     */
    private static void readTripleIndirectData(int blockNumber, int inodeSize, boolean isFile, Ext2File ext2,
                                        Superblock superblock, GroupDescriptor groupDescriptor) throws IOException
    {
        byte[] blockData = ext2.readBytes(blockNumber * Constants.BLOCK_SIZE, Constants.BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(blockData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0; i<buffer.limit(); i+=Constants.BYTE_LENGTH) {
            if(buffer.getInt(i) != 0)
                readDoubleIndirectData(buffer.getInt(i),  inodeSize, isFile, ext2, superblock, groupDescriptor);
        }
    }
}
