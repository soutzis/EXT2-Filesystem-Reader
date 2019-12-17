# Release
You can find the latest release [here](https://github.com/soutzis/EXT2-Filesystem-Reader/releases).
> The ext2-filesystem image does not come bundled within the release. You can use "[ext2img](https://github.com/soutzis/EXT2-Filesystem-Reader/blob/master/ext2img)" which is included with the source-code in /master, or just use your own.

# Usage Instructions
Just run the .jar from the latest release (command: *java -jar ext2reader.jar*) and you will be asked to provide the path to your ext2-filesystem image.

# Overview
***EXT2 Filesystem Reader*** is a program written in pure Java, that can read the contents of an **ext2** filesystem
image. It imitates some of the core utilities of GNU.

The most recent version implements the following GNU commands/utilities, but currently there is no support for *options* (flags) or pipes:

1. **cd**
2. **ls**
3. **cat**
4. **exit**

# Structure
The first 1024 bytes of an ext2 disk is the **boot block**, hence the **super-block** offset is 1024 and the 
**group-descriptor** offset is 2048.

## Boot-block
The contents of the boot-block are reserved for the partition boot sectors and are unused by the Ext2 filesystem 
([taken from here](http://cs.smith.edu/~nhowe/Teaching/csc262/oldlabs/ext2.html)).

STEP-BY-STEP WALKTHROUGH COMING SOON [HERE](https://soutzis.github.io/EXT2-Filesystem-Reader/)!
