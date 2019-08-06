# Overview

***EXT2 Filesystem Reader*** is a program written in pure Java, that can read the contents of an ext2-filesystem
image and output that data in a style that imitates the Linux terminal.
This implementation only supports little-endian format.

The most recent version supports the following commands, that once again
try to imitate the way that they work on the Linux kernel:

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
