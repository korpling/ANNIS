Building  {#dev-building}
========

ANNIS uses [Maven3](http://maven.apache.org/)  as build tool. Maven itself is
based on Java and should run on every major operating system. You have to
download and install the appropriate version for your operating system from
http://maven.apache.org/download.html before you can build ANNIS. Maven will
download all needed dependencies from central servers on the first build so you
will need to have a working internet connection. The dependencies are cached
locally once their are downloaded.

When you have downloaded or checked out the source of ANNIS the top-level
directory of the source code is the parent project for all ANNIS sub-projects. If
you want to build every project that is part of ANNIS just execute
~~~{.sh}
cd <annis-sources>/
mvn install
~~~
This might take a while on the first execution. `mvn clean` will remove all compiled
code if necessary.

If you only want to compile a sub-project execute `mvn install` in the
corresponding sub-directory. Every folder with a sub-project will have a pom.xml
file. These files configure the whole build process. The Maven documenation
contains detailed explanations of the structure and possible content of the
configuration files.

Some sub-projects don't provide a library but will produce a zip or tar/gz-
file when they are compiled. These assembly steps (see [Maven Assembly documentation](http://maven.apache.org/plugins/maven-assembly-plugin/)) are automatically
invoked on `mvn install`.


