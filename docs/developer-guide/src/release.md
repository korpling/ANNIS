# Making a new ANNIS release

## Introduction

You must have [mdBook](https://github.com/rust-lang-nursery/mdBook) installed to make a release.
Otherwise the documentation can't be created.

## Release Process 

### Initialization phase 

1. **Start** the release process by executing `mvn gitflow:release-start` for a regular release (branched from `develop`) or `mvn gitflow:hotfix-start` for a hotfix that is branched from `master`. The command will ask you for the new version number, use [semantic versioning](https://semver.org/).
2. **Add new changelog entries**, if some important information is missing add an entry to the changelog.
When the changelog is up-to-date, execute

~~~bash
mvn -N keepachangelog:release cff:create cff:third-party-folder
~~~
and commit your changes.
This will also update the citation file (`CITATION.cff`) and the contents of the `THIRD-PARTY` folder.

### Testing cycle

1. **Build** the complete project *with* tests.

~~~bash
mvn clean install
~~~

2. **Do manual tests.** If you have to fix any bug document it in the issue tracker, update the changelog and start over at step 1.
If no known bugs are left to fix go to the next section. 

### Finish phase


1. **Finish** the release by executing either `mvn gitflow:release-finish` for regular releases or `mvn gitflow:hotfix-finish` for hotfixes.
2. **Release** the staging repository to Maven Central with the Nexus interface: [https://oss.sonatype.org/](https://oss.sonatype.org/)
3. Create a new **release on GitHub** including the changelog. The release binaries and a new version of the User and Developer Guide will be deployed by the GitHub Actions CI.

