import org.codehaus.jettison.json.JSONException;

import java.io.*;

/*
    Program to convert RAML 0.8 definition to Swagger 2.0 definition
 */
public class MainProgram {

    public static void main(String[] args) throws IOException, JSONException {

        //Convert RAML 0.8 to Swagger 2.0
        String inputFilePath = "jukebox-api.raml";
        RAMLtoSwagger converter = new RAMLtoSwagger();
        converter.convertToJSON(inputFilePath);
    }
}
