import java.io.*;

/**
 *This class provides a way to readBytes bytes in a Randomly Accessed File, either from a given offset
 *or from the current file pointer
 *@author Petros Soutzis, 2017-19
 */
public class Ext2File
{
    private RandomAccessFile raf;

    /**
     *Constructor of Ext2File class. Will readBytes the ext2fs image and return the bytes that were readBytes.
     *@param vol is the Volume that the Ext2File will readBytes bytes from.
     */
    public Ext2File(Volume vol) {

        raf = vol.getRandomAccessFile();
    }

    /**
     *Reads at most length bytes starting at byte offset startByte from start of file.
     *Byte 0 is the first byte in the file.
     *StartByte must be such that, 0 less or equal than, startByte less than file.size or an exception should be raised.
     *@param startByte the offset from which the file will start reading from
     *@param length the size that the byte-array will have
     *@throws IOException is thrown when an error happens during the reading of the RAF
     *@return the byte array that the random access file readBytes from the volume
     */
    byte[] readBytes(long startByte, long length) throws IOException{
        byte[] data = new byte[(int) length];
        raf.seek(startByte);
        raf.readFully(data);

        return data;
    }

//    /**
//     *Reads at most length bytes, starting from the current file pointer.
//     *@param length the size that the byte-array will have
//     *@throws IOException e
//     *@return the byte array that the random access file readBytes from the volume
//     */
//    public byte[] readBytes(long length) throws IOException {
//        byte[] data = new byte[(int)length];
//        raf.seek(raf.getFilePointer());
//        raf.readFully(data);
//
//        return data;
//    }
}
