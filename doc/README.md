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
mvn clean install exec:exec -P doxygen
```
to compile the everything.

A pre-compiled version is accessable at http://korpling.github.com/ANNIS/doc/ .
