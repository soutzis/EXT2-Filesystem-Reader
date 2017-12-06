import java.nio.*;
import java.io.*;
import java.util.*;

public class Ext2File
{
  private Volume vol;
  private RandomAccessFile raf;
  private String filename = "ext2fs";
  private String path;

  public Ext2File(Volume vol, String path)
  {
    this.vol = vol;
    this.path = path;
    raf = vol.getRandomAccessFile();
  }

  public byte[] read(long startByte, long length) throws IOException, EOFException
  {
    byte[] data = new byte[(int) length];
    seek(startByte);
    raf.read(data);

    return data;
  }

  public byte[] read(long length) throws IOException
  {
    byte[] data = new byte[(int)length];
    seek(position());
    raf.read(data);

    return data;
  }

  public void seek(long position) throws IOException
  {
    raf.seek(position);
  }

  public long position() throws IOException
  {
    return raf.getFilePointer();
  }

  public long size() throws IOException
  {
    return vol.getLength();
  }

  public String getPath()
  {
    return path;
  }
}
