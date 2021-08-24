# Making a new ANNIS release


You must have [mdBook](https://github.com/rust-lang-nursery/mdBook) installed to make a release.
Otherwise the documentation can't be created.


1. Check the changelog (`CHANGELOG.md`): note the last release version number and which kind of changes have been made since the last release.
   Determine if this is a major, minor or patch release according to [semantic versioning](https://semver.org/). 
2. Release the **source code** using Maven.  The command will ask you for the new version number use the most appropriate with respect to the previous version number and the changes made.
```
mvn release:clean release:prepare release:perform
```
This will update versions, the changelog, our citation file (`CITATION.cff`) and the contents of the `THIRD-PARTY` folder.

3. Create a new **release on GitHub** including the changelog. The release binaries and a new version of the User and Developer Guide will be deployed by the GitHub Actions CI.

