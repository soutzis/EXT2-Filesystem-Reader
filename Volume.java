import java.nio.*;
import java.io.*;
/**
*This class provides a way to open a file, using Random Access and read-only attribute. Which means any point of
*the file can be read and this doesn't have to be done sequentially.
*@author Petros Soutzis (p.soutzis@lancaster.ac.uk)
*/

public class Volume
{
  private long file_length;                       //variable that holds the total length of the file
  private RandomAccessFile raf;                   //Class that provides the random access functionality

  /**
  *Constructor of the Volume Class
  *@param filename is the name of the file that will be accessed randomly
  */
  public Volume(String filename)
  {
		try
    {
		  raf = new RandomAccessFile(filename,"r");
      file_length = raf.length();                  //Gets the total length of the file
		}
		catch(IOException e)
    {
			e.printStackTrace();
		}
  }
  /**
  *Accessor method for the Random Access File
  *@return the Randomly Accessible file
  */
  public RandomAccessFile getRandomAccessFile()
  {
    return raf;
  }

  /**
  *@return the file's length
  */
  public long getLength()
  {
    return file_length;
  }
}
