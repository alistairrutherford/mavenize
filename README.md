mavenize
========

A tool which will attempt to "mavenize' any number of java projects. 


mavenize-core
-------------

Core library which does the mavenizing process. As well as a library it also builds to a command line launchable JAR file.

mavenize-fx
-----------

JavaFX based user interface.

You can download an executable JAR for this from https://code.google.com/p/mavenize-tool/downloads/list

![User interface](https://github.com/alistairrutherford/images/raw/master/mavenizefx.png)


It uses the java-fx-maven plugin from here https://github.com/zonski/javafx-maven-plugin/wiki

You will need to follow the instruction on the plugin page to ensure JavaFX is visible to the build.

Once you have JavaFX hooked into maven:

- go to the mavenize-fx folder
- execute "mvn jfx:build-jar" to build an executable jar.

License
--------
[Copyright - Alistair Rutherford 2013 - www.netthreads.co.uk]

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
