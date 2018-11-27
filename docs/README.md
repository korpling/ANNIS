ANNIS Documentation
===================

The documentation is split in three parts:

- User Guide for users (including administrators) of the ANNIS servier version and the Kickstarter
- Developer Guide for people who want to participate and or extend ANNIS

Build
-----

For the developer documentation you have to install [mdBook](https://github.com/rust-lang-nursery/mdBook).
The `mdbook` executable must be in the system path.

Then you can execute
```
mvn clean package -P mdbook
```
to compile everything. The result will be located in the `target/mdbook` folder.
You can also use
```
mdbook serve src/mdbook/user-guide/
```
to let mdBook serve the user guide on the local computer at [http://localhost:3000/](http://localhost:3000/) and to update it whenever one of the source files is changed.