# RAML-to-Swagger-Converter

## About the project
This repository contains a java project that converts RAML v0.8 API definition to Swagger v2.0 definition. 
For more info on swagger definitions, [Click here](https://swagger.io/docs/). For RAML docs, [Click here](https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md)

## Steps to run the project
* Open the project in Intellij, Eclipse or any other IDE which you are comfortable with.
* Enter the source file's path in the "filePath" string in the file named MainProgram.java.
* Make sure all the dependent files mentioned in the RAML are in the classpath so that the IDE can locate the dependent file during runtime.
* Finally, run the MainProgram.java which will create a new file containing the Swagger 2.0 definition for the given RAML source file. The destionation file name is the same name as RAML file but with .json extension.

## Todos
* Almost all the edge cases (rarely used definition fields in API definition) are taken care of. But incase something is missed out, please raise an issue.

#### Footnotes
* Incase you find any bugs or want to add any new feature that's included in the Swagger definition (say, in the future), then you are highly welcome to contribute to the project.
