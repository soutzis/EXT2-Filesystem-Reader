import java.io.*;

/**
*This class provides a way to open a file, using Random Access and read-only attribute. Which means any point of
*the file can be read and this doesn't have to be done sequentially.
*@author Petros Soutzis 2017-19
*/

public class Volume {
    //variable that holds the total length of the file
    //private long fileLength;
    //Class that provides the random access functionality
    private RandomAccessFile raf;


    /**
    *Constructor of the Volume Class
    *@param filename is the name of the file that will be accessed randomly
    */
    public Volume(String filename) {
        System.out.println("\nReading ext2 image..\n");
		try {
		    raf = new RandomAccessFile(filename,"r");
		}
		catch(IOException e) {
            System.out.println("\nSomething went wrong, could not read file." +
                    "Please check if a file called \""+filename+"\" exists.");
			e.printStackTrace();
		}
    }

    /**
    *Accessor method for the Random Access File
    *@return the Randomly Accessible file
    */
    RandomAccessFile getRandomAccessFile() {
        return raf;
    }
}
