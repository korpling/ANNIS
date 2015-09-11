ANNIS Documentation
===================

The documentation is split in three parts:

- User Guide for users of the Web-Frontend and Kickstarter
- Adminstration Guide for administrators of server installations of ANNIS
- Developer Guide for people who want to participate and or extend ANNIS

Build
-----

For the developer documentation you have to install Doxygen (http://www.doxygen.org).
The "doxygen" executable must be in the system path.

Then you can execute
```
mvn clean package -P doxygen
```
to compile everything. The result will be located in the "target/doxygen" folder.

A pre-compiled version is accessable at http://korpling.github.com/ANNIS/doc/ for the current stable release
and http://korpling.github.com/ANNIS/doc-develop/ for development versions.
