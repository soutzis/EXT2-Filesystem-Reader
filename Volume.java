import java.nio.*;
import java.io.*;

public class Volume
{
  private long file_length;
  private RandomAccessFile raf;

  public Volume(String filename)
  {
		try
    {
      //File file = new File(filename);
		  raf = new RandomAccessFile(filename,"r");
      file_length = raf.length();
		}
		catch(IOException e)
    {
			e.printStackTrace();
		}
  }

  public RandomAccessFile getRandomAccessFile()
  {
    return raf;
  }

  public long getLength()
  {
    return file_length;
  }
}
