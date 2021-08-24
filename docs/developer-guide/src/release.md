# Making a new ANNIS release


You must have [mdBook](https://github.com/rust-lang-nursery/mdBook) installed to make a release.
Otherwise the documentation can't be created.


1. Check which changes have been made since the last release in the `CHANGELOG.md` file.
2. Release the **source code** using Maven.  The command will ask you for the new version number, use [semantic versioning](https://semver.org/).
```
mvn release:clean release:prepare release:perform
```
This will update versions, the changelog, our citation file (`CITATION.cff`) and the contents of the `THIRD-PARTY` folder.

3. Create a new **release on GitHub** including the changelog. The release binaries and a new version of the User and Developer Guide will be deployed by the GitHub Actions CI.

