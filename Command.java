import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class Command{
    static final String CD = "cd", EXIT = "exit", LS = "ls", CAT="cat";

    /**
     * Is called when user types exit. It prints an informative message and then exits the running process
     */
    static void doExit(){
        System.out.print("\nEXT2 filesystem image scanning was terminated by the user.\n");
        System.exit(0);
    }

    static void doLs(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                     GroupDescriptor groupDescriptor, String path){
        try {
            FileInfo.readDirectoryData(inode, inodeSize, ext2, superblock, groupDescriptor, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void doCat(Inode inode, int inodeSize, Ext2File ext2, Superblock superblock,
                     GroupDescriptor groupDescriptor, String path){
        try {
            FileInfo.readFileData(inode, inodeSize, ext2, superblock, groupDescriptor, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    static String getCurrentPathName(Inode inode, Ext2File ext2) throws IOException {
//        int[] blockPointers = inode.getBlockPointers();
//        StringBuilder path = new StringBuilder();
//        /*the names of the directories or files are small and will
//         **always be pointed by the direct pointers of the inode*/
//        for (int i = 0; i < 12; i++) {
//            //if blockPointer[i] is not equal to 0, then data exists.
//            if (blockPointers[i] != 0) {
//                /*multiplying the block pointer offset by the
//                 **max block size to get the correct offset*/
//                byte[] data = ext2.readBytes((blockPointers[i] * Constants.BLOCK_SIZE), Constants.BLOCK_SIZE);
//                ByteBuffer buffer = ByteBuffer.wrap(data);
//                buffer.order(ByteOrder.LITTLE_ENDIAN);
//                short recLength;
//
//                for (int j = 0; j < buffer.limit(); j += recLength) {
//                    recLength = buffer.getShort(j+Constants.BYTE_LENGTH);
//                    //used set the length of the bytearray holding the matches the searched path's name
//                    byte[] nameBytes = new byte[buffer.get(j + Constants.BYTE_LENGTH + Constants.SHORT_LENGTH)];
//
//                    //retrieve each byte of the name
//                    for (int k = 0; k < nameBytes.length; k++) {
//                        nameBytes[k] = buffer.get(k + j + (Constants.BYTE_LENGTH * 2));
//                    }
//                    path.append(new String(nameBytes).trim());
//                }
//            }
//        }
//
//        return path.toString();
//    }
}
