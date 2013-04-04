Making a new ANNIS release  {#dev-release}
==========================

\warning
This is work and progress and neither finished or tested for a real release.

Unfurtunally this requires some manual work. We used to invoke the Maven Release plugin in the process, 
but the only real usefull task was the version update and we still needed to do a lot of manual work.
Thus we decided to write down the necessary steps to perform a new ANNIS release instead of
relaying to a half-working solution with the Maven Release plugin.

1. Make and switch to a new *local* branch for the release in git. Do not push this branch to a remote location since you may want to throw 
   it away when the testing of the temporary release unveils release critical problems.
   Normally commit the changes after each major step.
2. Update the version information
  1. Internal version information in pom.xml files
\code{.sh}
mvn release:update-versions -DautoVersionSubmodules=true
\endcode
  2. The `ANNIS_VERSION` variable in the `buildbot_scripts/copyService.sh` script file.
3. Update the licenses in the `THIRD-PARTY` folder
\code{.sh}
mvn license:add-third-party
mvn license:download-licenses
\endcode
4. Regenerate this documentation.
\code{.sh}
cd doc/
doxygen
cd ..
\endcode
5. Build the complete project *with* tests.
\code{.sh}
mvn clean
mvn install
\endcode
6. Do manual tests.
7. Add new changelog entry. There is a helper script in the `Misc` folder to make this easier
  1. Get the GitHub Milestone id associated the release (is visible in the URL if you view the issues of the release tracking milestone).
  2. replace the ID in the `Misc/changelog.py` script
  3. execute this script
  4. check the output, if something is missing create an enhancement issue in GitHub and repeat the script execution
  5. add the output to the `CHANGELOG` file
8. Tag the release and merge it with the `releases` branch, publish the new `releases` branch
9. Deploy release to our Maven server
\code{.sh}
mvn deploy
\endcode
