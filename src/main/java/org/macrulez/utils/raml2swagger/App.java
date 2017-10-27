/*
    Raml2Swagger utility
    Copyright (C) 2017 github.com/esh-b
    Copyright (C) 2017 Szabolcs Gyurko

    This utility is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This utility is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this utility; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.macrulez.utils.raml2swagger;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;

import java.io.*;

/*
    Program to convert RAML 0.8 definition to Swagger 2.0 definition
 */
@Slf4j
public class App {
    public static void main(String[] args) {
        RAMLtoSwagger converter = new RAMLtoSwagger();

        if (args.length < 1) {
            LOGGER.error("Usage: java -jar raml2swagger.jar <raml-file>");
            return;
        }


        try {
            LOGGER.info("Converting...");
            converter.convertToJSON(args[0]);
            LOGGER.info("Done.");
        } catch (IOException | JSONException e) {
            LOGGER.error("Error converting file: {}", args[0]);
        }
    }
}
