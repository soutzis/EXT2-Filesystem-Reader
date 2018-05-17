import java.nio.*;
import java.io.*;
import java.util.*;

/**
*This class provides a way to read bytes in a Randomly Accessed File, either from a given offset
*or from the current file pointer
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/
public class Ext2File
{
  private Volume vol;
  private RandomAccessFile raf;

  /**
  *Constructor of Ext2File class
  *Will fix minor errors of the path input by the user
  *@param vol is the Volume that the Ext2File will read bytes from
  *@param path is the path that the user will input and the program will read from
  */
  public Ext2File(Volume vol, String path)
  {
    this.vol = vol;
    String temp_path = path;

    temp_path += path.equals("/") ? "." : "";
    temp_path = path.endsWith("/")? path.substring(0, path.lastIndexOf('/')) : path ;
    temp_path = (!path.startsWith("/")) ? "/" + path : path;

    Driver.path = temp_path;
    raf = vol.getRandomAccessFile();
  }

  /**
  *Reads at most length bytes starting at byte offset startByte from start of file. Byte 0 is the first byte in the file.
  *StartByte must be such that, 0 less or equal than, startByte less than file.size or an exception should be raised.
  *@param startByte the offset from which the file will start reading from
  *@param length the size that the bytearray will have
  *@throws IOException e
  *@throws EOFException eof
  *@return the byte array that the random access file read from the volume
  */
  public byte[] read(long startByte, long length) throws IOException, EOFException, FileNotFoundException
  {
    byte[] data = new byte[(int) length];
    seek(startByte);
    raf.readFully(data);

    return data;
  }

  /**
  *Reads at most length bytes, starting from the current file pointer.
  *@param length the size that the bytearray will have
  *@throws IOException e
  *@return the byte array that the random access file read from the volume
  */
  public byte[] read(long length) throws IOException
  {
    byte[] data = new byte[(int)length];
    seek(position());
    raf.readFully(data);

    return data;
  }

  /**
  *Moves through the RandomAccessFile, to the position specified
  *@param position the offset that the random access file will search for
  *@throws IOException e
  */
  public void seek(long position) throws IOException
  {
    raf.seek(position);
  }

  /**
  *@return the current pointer position in the random accessed file
  *@throws IOException e
  */
  public long position() throws IOException
  {
    return raf.getFilePointer();
  }

  /**
  *@return the total size of the file opened by Volume class
  *@throws IOException e
  */
  public long size() throws IOException
  {
    return vol.getLength();
  }
}
