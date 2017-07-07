# RAML-to-Swagger-Converter

## About the project
This repository contains a java project that converts RAML v0.8 API definition to Swagger v2.0 definition. 
For more info on swagger definitions, [Click here](https://swagger.io/docs/). For RAML docs, [Click here](https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md)

## Steps to run the project
* Open the project in Intellij, Eclipse or any other IDE which you are comfortable with.
* Enter the source file's (RAML file) path in the "inputFilePath" string inside the file MainProgram.java.
* Make sure all the dependent files required for the RAML (like schema files/example files) are in the classpath so that the IDE can locate the dependent file during runtime. Else FileNotFound exception might arise.
* Also, all the jars required for the project are located in the project's libs folder. Make sure to add those jars to the classpath before running the project.
* Finally, run the MainProgram.java which will create a new file containing the Swagger 2.0 definition for the given RAML source file. The destination file will be in the project directory with the file name being the same name as the RAML file (but with .json extension).
* An example is provided in the example directory which will convert jukebox-api.raml (RAML 0.8) to jukebox-api.json (Swagger 2.0).

## Todos
* This project currently supports only conversion of RAML v0.8 (RAML 1.0 parser library is not yet available for java as of now). The support for RAML 1.0 conversion to Swagger 2.0 has to be made once RAML 1.0 parser is available.
* Almost all the edge cases (rarely used definition fields in API definition) are taken care of. But incase something is missed out, please raise an issue.

## Footnotes
* Incase you find any bugs or want to add any new feature that's included in the Swagger definition (say, in the future), then you are highly welcome to contribute to the project.
