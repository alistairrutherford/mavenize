mavenize
========

A tool which will attempt to "mavenise' any number of java projects. 


mavenize-core
-------------

Core library which does the mavenizing process. Also builds to a command line launchable JAR file.

mavenize-fx
-----------

JavaFX base user interface.

![User interface](https://github.com/alistairrutherford/images/raw/master/mavenizefx.png)

You will need to use the maven-javafx-plug.

It uses the java-fx-maven plugin from here https://github.com/zonski/javafx-maven-plugin/wiki

You will need to follow the instruction on the plugin page to ensure JavaFX is visible to the build.
