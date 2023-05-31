# ilishaper - creates simplified, derived Interlis data

## Features
- creates a simplified ili-model from a source ili-model
- creates a dervied xtf-file from a source xtf-file

## License
ilishaper is licensed under the LGPL (Lesser GNU Public License).

## System Requirements
For the current version of ilishaper, you will need a JRE (Java Runtime Environment) installed on your system, version 1.8 or later. Any OpenJDK based JRE will do.
The JRE (Java Runtime Environment) can be downloaded from the Website <http://www.java.com/>.

## Software Download 
TBD

## Installing ilishaper
To install the ilishaper, choose a directory and extract the distribution file there. 

## Running ilishaper
The ilishaper can be started with

    java -jar ilishaper.jar [options]

## Building from source
To build the `ilishaper.jar`, use

    gradle build

To build a binary distribution, use

    gradle bindist

### Development dependencies
* JDK 1.8 or higher (OpenJDK will do)
* Gradle
* Python and docutils installed (`pip install docutils`)
    * rst2html command is used by `userdoc` gradle task
    * rst2html location can be provided in file _user.properties_

## Documentation
[docs/ilishaper.rst](docs/ilishaper.rst)
