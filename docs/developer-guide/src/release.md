# Making a new ANNIS release


You must have [mdBook](https://github.com/rust-lang-nursery/mdBook) installed to make a release.
Otherwise the documentation can't be created.


1. Check the changelog (`CHANGELOG.md`): note the last release version number and which kind of changes have been made since the last release.
   Determine if this is a major, minor or patch release according to [semantic versioning](https://semver.org/). 
2. **Create a release** using Maven.  The command will ask you for the new version number use the most appropriate with respect to the previous version number and the changes made.
```
mvn release:clean release:prepare release:perform
```
This will update versions, the changelog, our citation file (`CITATION.cff`) and the contents of the `THIRD-PARTY` folder.
An GitHub action will automatically create a release on the GitHub platform which includes the binary assets.
