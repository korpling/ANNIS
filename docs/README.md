# ANNIS Documentation


The documentation is split in two parts:

- User Guide for users (including administrators) of the ANNIS servier version and the Kickstarter
- Developer Guide for people who want to participate and or extend ANNIS

## Build


For the developer documentation you have to install [mdBook](https://github.com/rust-lang-nursery/mdBook).
The `mdbook` executable must be in the system path.

Then you can execute
```bash
mdbook build developer-guide/
mdbook build user-guide/
```
to compile the documentation. 
The result will be located in the `book` folder of each book.
You can also use
```
mdbook serve user-guide/
```
to let mdBook serve the user guide on the local computer at [http://localhost:3000/](http://localhost:3000/) and to update it whenever one of the source files is changed.