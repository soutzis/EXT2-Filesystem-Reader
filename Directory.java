import java.nio.*;
import java.io.*;
import java.util.*;

public class Directory
{

  private class FileInfo
  {
    private Inode inode;
    private int inode_index;
    private int counter;
    private final int block_size = 1024;
    private int[] block_pointers;
    private short length;
    private byte[] char_bytes;

    public void readBlockData(Inode inode)
    {
      this.inode = inode;
      counter = 0;
      block_pointers = inode.getBlockPointers();

      for(int i=0; i<12; i++)   //check if direct pointers point to data
      {
  			if (block_pointers[i]==0)
          counter+=1;
  		}

  		for (int i=0; i<12 && counter<12; i++)
      {
  			if(block_pointers[i]!=0)
          printBlockData(block_pointers[i]);
  		}
      if(block_pointers[12] != 0)
        readIndirectData(inode.getIndirectPointer());
  		if(block_pointers[13] != 0)
        readDblIndirectData(inode.getdIndirectPointer());
  		if(block_pointers[14] != 0)
        readTrplIndirectData(inode.gettIndirectPointer());
    }

    public void printBlockData(int startByte){
  		byte[] block_data = Driver.ext2.read(startByte*block_size, block_size);

  		ByteBuffer buffer = ByteBuffer.wrap(block_data);
  		buffer.order(ByteOrder.LITTLE_ENDIAN);

  		if (inode.file())
        System.out.println(new String(block_data));

  		else{

  			byte[] tempFile = new byte[1024];
  			IndexNode tempInode = null;

  			while(i<buff.limit()){

  				inodeIndex=buff.getInt(i);
  				length=buff.getShort(i+4);
  				inodeFilename=new byte[length-8];
  				for(int j=0;j<inodeFilename.length;j++){
  					inodeFilename[j]=buff.get(j+8+i);
  				}
  				double containingBlock;
  				containingBlock=FileReader.getContainingBLock(inodeIndex);
  				tempInode=new IndexNode(tempFile, (int)containingBlock,filename);
  				tempInode.read();

  				System.out.println("");
  				if(i==0)System.out.println("Permissions:\t\t"+"Hard Links:\t\t"+"User:\t\t"+"Group:\t\t"+"Size:\t\t\t"+"Modified:\t\t\t"+"Filename:\t\t");
  				System.out.print(tempInode.getPermission()+"\t\t");
  				System.out.print(tempInode.getHardLinks()+"\t\t");
  				System.out.print(tempInode.getUserIDlower()+"\t\t");
  				System.out.print(tempInode.getGroupIDlower()+"\t\t");
  				System.out.print(tempInode.getSizeLower()+"\t\t");
  				System.out.print(tempInode.getModified()+"\t\t");
  				System.out.print(new String(inodeFilename).trim()+"\t\t");
  				i+=length;
  			}
  		}
  	}

  }
}
