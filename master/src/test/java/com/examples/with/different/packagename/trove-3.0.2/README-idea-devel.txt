If you are developing using IntelliJ IDEA from JetBrains, you can use the
project file contained in the "idea" directory. When you do so, you will
need to define three "Path Variables" (IDEA will prompt you to do this when
you open the project):
  - TROVE_TOP_LEVEL - Top level directory containing Trove files. Note
                      that this directory should *NOT* contain the CVS
                      files, but should contain the directory that does.
                        Example: C:/Users/reden/Documents/Trove
  - TROVE_VCS_ROOT  - Directory within TROVE_TOP_LEVEL that contains
                      the CVS files. This should be an absoulte path.
                        Example: C:/Users/reden/Documents/Trove/working
  - TROVE_JDK_NAME  - Name of the JDK for Trove to use.
                        Example: 1.6.0_17