Making a new ANNIS release  {#dev-release}
==========================

[TOC]

Introduction {#dev-release-intro}
============

Unfurtunally creating a new release requires some manual work. We used to invoke the Maven Release plugin in the process, 
but the only real usefull task was the version update and we still needed to do a lot of manual work.
Thus we decided to write down the necessary steps to perform a new ANNIS release instead of
relaying to a half-working solution with the Maven Release plugin.

The release process might take several days and includes fixing bugs that are only discovered in the 
release testing process. **Never ever add new features in this release process**, there is the separate
"develop" branch which you can use for this purposes.


General techniques {#dev-release-general}
==================

Updating the version {#dev-release-version-update}
--------------------

1. Update the parent pom.xml and set the version there
2. execute
\code{.sh}
mvn -N versions:update-child-modules
\endcode
3. Set the `ANNIS_VERSION` variable in the `buildbot_scripts/copyService.sh` script file.
4. Set the `PROJECT_NUMBER` variable in the `doc/Doxyfile` documentation descriptor file.

Creating a changelog entry {#dev-release-changelog}
--------------------------

1. Get the GitHub Milestone id associated the release (is visible in the URL if you view the issues of the release tracking milestone).
2. replace the ID in the `Misc/changelog.py` script
3. execute this script
5. add the output to the `CHANGELOG` file

Release Cycle {#dev-release-cycle}
=============

Initialization phase {#dev-release-init}
--------------------

1. Make a new branch for the release in the GitHub main repository named with the complete release number
Normally commit and push the changes after each major step.
2. Update the version as described in the [general techniques section](@ref dev-release-version-update) 
3. Update the licenses in the `THIRD-PARTY` folder and commit/push to release branch
\code{.sh}
mvn license:add-third-party
mvn license:download-licenses
\endcode
4. Add new changelog entry as described in the [general techniques section](@ref dev-release-changelog), 
if some important information is missing create an enhancement issue in GitHub and repeat

Testing cycle {#dev-release-test}
-------------

1. Build the complete project *with* tests.
\code{.sh}
mvn clean
mvn install
\endcode
2. Do manual tests. If you have to fix any bug document it in the issue tracker, [update the changelog](@ref dev-release-changelog) and start over at step 1.
If no bugs are left to fix go to the next section. 

Finish phase {#dev-release-finish}
-------------
1. Tag the release and merge it into the `master` branch, publish the new `master` branch
2. Deploy release to our Maven server
\code{.sh}
mvn deploy
\endcode
3. Regenerate this documentation and copy it to the github page (via the gh-page repository)
\code{.sh}
cd doc/
doxygen
cd ..
\endcode
4. Reintegrate the "master" branch into the "develop" branch and set the "develop" branch to the [next SNAPSHOT version](@ref dev-release-version-update)

