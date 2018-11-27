# Making a new ANNIS release

## Introduction

Unfortunately creating a new release requires some manual work. We used to invoke the Maven Release plugin in the process, 
but the only real useful task was the version update and we still needed to do a lot of manual work.
Thus we decided to write down the necessary steps to perform a new ANNIS release instead of
relaying to a half-working solution with the Maven Release plugin.

The release process might take several days and includes fixing bugs that are only discovered in the 
release testing process. **Never ever add new features in this release process**, there is the separate
"develop" branch which you can use for this purposes.

You must have [mdBook](https://github.com/rust-lang-nursery/mdBook) installed to make a release.
Otherwise the documentation can't be created.

## General techniques 

### Updating the version

1. Update the parent pom.xml and set the version there
2. execute
~~~bash
mvn versions:update-child-modules
~~~

### Creating a changelog entry 

1. Get the GitHub Milestone id associated the release (is visible in the URL if you view the issues of the release tracking milestone).
2. replace the ID in the `Misc/changelog.py` script
3. execute this script
4. add the output to the `CHANGELOG` file

## Stable Release Process 

### Initialization phase 

1. Make a new branch for the release in the GitHub main repository named with the complete release number
Normally commit and push the changes after each major step.
2. Update the version as described in the [general techniques section](#updating-the-version) 
3. Update the licenses in the `THIRD-PARTY` folder and commit/push to release branch
~~~bash
mvn license:add-third-party
mvn license:download-licenses
~~~
4. Add new changelog entry as described in the [general techniques section](#creating-a-changelog-entry), 
if some important information is missing create an enhancement issue in GitHub and repeat

### Testing cycle

1. Build the complete project *with* tests.
~~~bash
mvn clean
mvn -DskipTests=true install
mvn test
~~~
2. Do manual tests. If you have to fix any bug document it in the issue tracker, [update the changelog](#creating-a-changelog-entry) and start over at step 1.
If no known bugs are left to fix go to the next section. 

### Finish phase

1. Deploy release to Maven Central
\code{.sh}
mvn deploy -P release,mdbook
\endcode
3. Tag the release and merge it into the `master` branch, publish the new `master` branch
5. Reintegrate the "master" branch into the "develop" branch and set the "develop" branch to the [next SNAPSHOT version](#updating-the-version)
6. Create a new release on GitHub including the changelog. Upload the binaries from Maven repository to GitHub release as well.

## Preview Release Process

Preview releases are like named Snapshots of the the `develop` branch.
They have the version number of the next upcoming release but with the 
appendix "-previewX" (X is a number).
An example preview release number is "3.4.0-preview2".
Preview releases are not published in the master branch but are deployed to Maven Central.

### Initialization phase 

1. Pull latest updates from the `develop` branch
2. Update the version as described in the [general techniques section](#updating-the-version) 
3. Update the licenses in the `THIRD-PARTY` folder and commit/push to release branch
~~~bash
mvn license:add-third-party
mvn license:download-licenses
~~~
4. Add new changelog entry as described in the [general techniques section](#creating-a-changelog-entry), 
if some important information is missing create an enhancement issue in GitHub and repeat.
Preview releases have their own milestones and thus also their own changelog section.

### Testing cycle

1. Build the complete project *with* tests.
~~~bash
mvn clean
mvn -DskipTests=true install 
mvn test
~~~
2. Do manual tests. If you have to fix any bug document it in the issue tracker, [update the changelog](#creating-a-changelog-entry) and start over at step 1.
If no known bugs are left to fix go to the next section. 

### Finish phase

1. Deploy release to Maven Central
~~~bash
mvn deploy -P release,preview,mdbook
~~~
3. Tag the release and push it to the `develop` branch. If new commits have been introduced in `develop` merge them (but don't use rebasing).
5. Set the "develop" branch to the [previous SNAPSHOT version](#updating-the-version)
6. Create a new release on GitHub including the changelog. Mark this release as "pre-release". Upload the binaries from Maven repository to GitHub release as well.