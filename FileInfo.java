import java.nio.*;
import java.io.*;
import java.util.*;
/**
 *This is a class that will read each directory or file name in a path, and if that pathname exists
 *in the volume, it will read and print the data contained there; both in UTF-8 and Hexadecimal.
 *@author Petros Soutzis, 2017-19
 */


//FIXME need to only display data of requested directory or file, not every directory
//FIXME need to remove duplicate code
//TODO need to optimize booleans for isFile (should be passed in a recursive manner

@SuppressWarnings("Duplicates")
public class FileInfo {
    //The inode to read
    private Inode inode;
    //Boolean to indicate if the program is trying to read a file or a directory and if data exists.
    private boolean isFile;
    //Inode size
    private int inodeSize;
    //The block pointers
    private int[] blockPointers;
    //ArrayList of byte arrays, to hold all the byte arrays whose data was printed by the program
    private ArrayList<byte[]> hexData;
    //The current path
    private String path;

    /*Constants*/
    private final int BLOCK_SIZE = 1024;
    //equivalent of 32 bits in bytes (an int)
    private final int BYTE_LENGTH = 4;
    //equivalent of 16 bits in bytes (a short)
    private final int SHORT_LENGTH = 2;

    /**
     *Constructor of the FileInfo class
     *@param inode is the inode that this class will use to read data
     * program will compare to the names of directories and files in the volume
     */
    public FileInfo(Inode inode, int inodeSize) {
        this.inode = inode;
        this.inodeSize = inodeSize;
        this.hexData = new ArrayList<>();
        this.path = "";
    }

    /**
     * This method will call findInode() method, to get the number of the inode, if the names
     * in pathFragments array match the names in the volume
     * If the inode number is 0, the program will be terminated.
     * The method will then parse the bytes to read, to the Inode instance, by calculating
     * the offset based on the inode number that the path returns.
     * If data exists, this method will call the readBlockData() method,
     * to read and print the data in a human-readable way and then it will call the readHexData,
     * in order to read and print that same data in Hexadecimal format
     *@param pathFragments is the array of names of directories and files, that the
     *@throws IOException ioe
     */
    void getFileInfo(String[] pathFragments) throws IOException {
        /*NOTE:i starts from 1, because the first item in the array is whitespace*/
        for(int i = 1; i< pathFragments.length; i++) {
            int inodeOfPathFragment = findInode(pathFragments[i]);

            // Check if path does exist and if not, print an error message (bad path); then exit the loop
            if(inodeOfPathFragment > 0) {
                byte[] inodeData = Driver.ext2.read((Driver.getContainingBlock(inodeOfPathFragment)), inodeSize);
                Inode currentInode = new Inode(inodeData);
                currentInode.read();
                isFile = currentInode.isFile();
                System.out.print("\n");
                readBlockData(currentInode);

                if(Driver.DEBUG)
                    readHexData(hexData);
                if(!isFile) {
                    this.inode = currentInode;
                    this.path = String.join("/", pathFragments);
                }
            }
            else {
                System.out.println("BAD PATH - NO DATA FOUND");
                break;
            }
        }
    }

    /**
     * @return The path of the file just accessed (or "" if it was a bad path).
     */
    String getFilePath(){
        return this.path;
    }

    /**
     * @return The inode that contains that latest file that was read using an instance of this class
     */
    Inode getFileInode(){
        return this.inode;
    }

    /**
     *This class calculates the inode number, needed to read the data contained in the block that the inode points to
     *If there was no match, method returns integer 0
     *@param path is the name of the directory or file to look for
     *@throws IOException e
     *@return the inode number that points to the data requested in the path
     */
    private int findInode(String path) throws IOException {
        blockPointers = inode.getBlockPointers();
        int noData = 0;

        /*the names of the directories or files are small and will
         **always be pointed by the direct pointers of the inode*/
        for (int i=0; i<12; i++) {
            if(blockPointers[i] != 0) {
                /*multiplying the block pointer offset by the
                 **max block size to get the correct offset*/
                byte[] data = Driver.ext2.read((blockPointers[i]* BLOCK_SIZE), BLOCK_SIZE);
                ByteBuffer buffer = ByteBuffer.wrap(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                short recLength;

                /*Jumping + recLength each time, to read the next name, if exists
                 * (check http://cs.smith.edu/~nhowe/262/oldlabs/ext2.html Q6)
                 * recLength is the value for jumping to the next directory location*/
                for(int j=0; j<buffer.limit(); j+=recLength) {
                    recLength = buffer.getShort(j+ BYTE_LENGTH);
                    byte nameBytes = buffer.get(j+ BYTE_LENGTH + SHORT_LENGTH);
                    byte[] charBytes = new byte[nameBytes];

                    /*get the character array, the file or directory
                     *name for splitPath[i]*/
                    for(int k = 0; k < charBytes.length; k++) {
                        charBytes[k] = buffer.get(k + j + (BYTE_LENGTH * 2));
                    }

                    /* TRIM REMOVES WHITESPACE, to check if the pathname
                     * entered with the one discovered are 'equal'.
                     * If they are 'equal',it retrieves the number of the inode
                     * and then exits this loop*/
                    if(path.equals(new String(charBytes).trim())) {
                        return buffer.getInt(j);
                    }
                }
            }
        }
        return noData;
    }

    /**
     *This method will read byte arrays from a list of byte arrays and print the content in a
     *readable Hexadecimal format, with 26 hex characters in each line for readability
     *@param raw is the ArrayList with bytes that this method will read and print as hex
     */
    private void readHexData(ArrayList<byte[]> raw) {
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
     *This method will read the block pointers values of an blockInode and if a pointer
     *points to real data, it will call the appropriate methods to print that data.
     *@param blockInode is the Inode, whose block pointers will be read
     *@throws IOException e
     */
    private void readBlockData(Inode blockInode) throws IOException
    {
        blockPointers = blockInode.getBlockPointers();

        for (int i=0; i<12; i++) {
            if(blockPointers[i]!= 0)
                printBlockData(blockPointers[i]);
        }
        /*If indirect pointer value is 0, then it means that it does not point to any data*/
        if(blockPointers[12] != 0)
            readIndirectData(blockPointers[12]);
        if(blockPointers[13] != 0)
            readDoubleIndirectData(blockPointers[13]);
        if(blockPointers[14] != 0)
            readTripleIndirectData(blockPointers[14]);
    }

    /**
     *This method will print the data contained in a file or the metadata from the inode, if it is a Directory
     *@param blockNumber is the offset of the data, obtained by the direct pointers to data in the inode.
     *@throws IOException e
     */
    private void printBlockData(int blockNumber) throws IOException {
        //multiplying offset by 1024, to get the correct block number (the correct offset)
        byte[] blockData = Driver.ext2.read(blockNumber * BLOCK_SIZE, BLOCK_SIZE);
        hexData.add(blockData);

        if (isFile) {
            //the bytes converting to a String and trim() removes whitespace
            String str = new String(blockData).trim();
            System.out.print(str);
        }

        else {
            ByteBuffer buffer = ByteBuffer.wrap(blockData);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            short dirLength;

            for(int i=0; i<buffer.limit(); i+=dirLength) {
                int inodeOffset = buffer.getInt(i);
                //because the index is 4 bytes long
                dirLength = buffer.getShort(i + BYTE_LENGTH);
                // 8 bits in size, located after dirLength in the
                byte nameBytes = buffer.get(i+ BYTE_LENGTH + SHORT_LENGTH);
                byte[] charBytes = new byte[nameBytes];

                for(int j=0; j<charBytes.length; j++) {
                    //fetch each char from the array of bytes
                    charBytes[j] = buffer.get(j + i + (BYTE_LENGTH * 2));
                }

                int containingBlock = Driver.getContainingBlock(inodeOffset);
                byte[] otherData = Driver.ext2.read(containingBlock, inodeSize);
                Inode iData = new Inode(otherData);
                iData.read();
                long fileSize = ((long)iData.getSizeUpper() << 32) | ((long)iData.getSizeLower() & 0xFFFFFFFFL);

                System.out.print(iData.readPermissions()+"\t");//prints the metadata from the inode
                System.out.print(iData.getHardLinks()+"\t");
                System.out.print(iData.getUid()+"\t");
                System.out.print(iData.getGid()+"\t");
                System.out.print(fileSize+"\t");
                System.out.print(iData.getDate()+"\t");
                System.out.print(new String(charBytes).trim()+" \n");
            }
        }
    }

    /**
     *This method will read the indirect data block and if data exists it will call printBlockData()
     *@param blockNumber is the offset of the data, obtained by the block pointers from the inode.
     *@throws IOException e
     */
    private void readIndirectData(int blockNumber) throws IOException
    {
        byte[] blockData = Driver.ext2.read(blockNumber * BLOCK_SIZE, BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(blockData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0; i<buffer.limit(); i+= BYTE_LENGTH)
        {
            if(buffer.getInt(i) != 0)
                printBlockData(buffer.getInt(i));
        }
    }

    /**
     *This method will read the double indirect data block and if data exists it will call readIndirectData()
     *@param blockNumber is the offset of the data, obtained by the block pointers from the inode.
     *@throws IOException e
     */
    private void readDoubleIndirectData(int blockNumber) throws IOException
    {
        byte[] blockData = Driver.ext2.read(blockNumber* BLOCK_SIZE, BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(blockData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0; i<buffer.limit(); i+= BYTE_LENGTH)
        {
            if(buffer.getInt(i) != 0)
                readIndirectData(buffer.getInt(i));
        }
    }

    /**
     *This method will read the triple indirect data block and if data exists it will call readDoubleIndirectData()
     *@param blockNumber is the offset of the data, obtained by the block pointers from the inode.
     *@throws IOException e
     */
    private void readTripleIndirectData(int blockNumber) throws IOException
    {
        byte[] blockData = Driver.ext2.read(blockNumber * BLOCK_SIZE, BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.wrap(blockData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i=0; i<buffer.limit(); i+=BYTE_LENGTH) {
            if(buffer.getInt(i) != 0)
                readDoubleIndirectData(buffer.getInt(i)); //print contents of file or directory data
        }
    }
}
