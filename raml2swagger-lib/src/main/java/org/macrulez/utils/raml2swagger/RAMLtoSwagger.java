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
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Note: Supports only RAML 0.8 conversion to Swagger 2.0
 */

@Slf4j
class RAMLtoSwagger implements Constants {

    private HashMap<Integer, String> respCodeMap = new HashMap<>();
    private JSONObject swaggerJSON;
    private Raml raml;
    private List<String> schemasList = new ArrayList<>();
    private Boolean baseUriParamPresent = false;
    private Integer indexBaseUriParam;

    private void initializeRespCodes() {
        respCodeMap.put(100, "Continue");
        respCodeMap.put(101, "Switching Protocols");
        respCodeMap.put(103, "Checkpoint");
        respCodeMap.put(200, "OK");
        respCodeMap.put(201, "Created");
        respCodeMap.put(202, "Accepted");
        respCodeMap.put(203, "Non-Authoritative Information");
        respCodeMap.put(204, "No Content");
        respCodeMap.put(205, "Reset Content");
        respCodeMap.put(206, "Partial Content");
        respCodeMap.put(300, "Multiple Choices");
        respCodeMap.put(301, "Moved Permanently");
        respCodeMap.put(302, "Found");
        respCodeMap.put(303, "See Other");
        respCodeMap.put(304, "Not Modified");
        respCodeMap.put(306, "Switch Proxy");
        respCodeMap.put(307, "Temporary Redirect");
        respCodeMap.put(308, "Resume Incomplete");
        respCodeMap.put(400, "Bad Request");
        respCodeMap.put(401, "Unauthorized");
        respCodeMap.put(402, "Payment Required");
        respCodeMap.put(403, "Forbidden");
        respCodeMap.put(404, "Not Found");
        respCodeMap.put(405, "Method Not Allowed");
        respCodeMap.put(406, "Not Acceptable");
        respCodeMap.put(407, "Proxy Authentication Required");
        respCodeMap.put(408, "Request Timeout");
        respCodeMap.put(409, "Conflict");
        respCodeMap.put(410, "Gone");
        respCodeMap.put(411, "Length Required");
        respCodeMap.put(412, "Precondition Failed");
        respCodeMap.put(413, "Request Entity Too Large");
        respCodeMap.put(414, "Request-URI Too Long");
        respCodeMap.put(415, "Unsupported Media Type");
        respCodeMap.put(416, "Requested Range Not Satisfiable");
        respCodeMap.put(417, "Expectation Failed");
        respCodeMap.put(500, "Internal Server Error");
        respCodeMap.put(501, "Not Implemented");
        respCodeMap.put(502, "Bad Gateway");
        respCodeMap.put(503, "Service Unavailable");
        respCodeMap.put(504, "Gateway Timeout");
        respCodeMap.put(505, "HTTP Version Not Supported");
        respCodeMap.put(511, "Network Authentication Required");
    }

    RAMLtoSwagger() {
        initializeRespCodes();
    }

    //Return the response for a response code
    private String getResponseMessage(int code) {
        return respCodeMap.get(code);
    }

    //Put the swagger version
    private void putSwaggerHeader() throws JSONException {
        swaggerJSON.put(SWAGGERVERSION_PARAM_KEY, SWAGGERVERSION_PARAM_VALUE);
    }

    //Get basePath from base URI
    private String getBasePath() {
        int start = raml.getBaseUri().indexOf("//") + 2;
        if (start == -1) {
            start = 0;
        }

        start = raml.getBaseUri().indexOf("/", start);

        if (start < 0) {
            return null;
        }

        return raml.getBaseUri().substring(start);
    }

    //Put all the API basic info
    private void getAPIInfo() throws JSONException {
        JSONObject info = new JSONObject();

        if (!raml.getTitle().isEmpty())
            info.put(API_TITLE_PARAM_KEY, raml.getTitle());

        if (raml.getVersion() != null)
            info.put(API_VERSION_PARAM_KEY, raml.getVersion());

        if (raml.getDocumentation() != null) {
            /* Uses stream API to create a single property from the documentation entries */
            info.put(DESCRIPTION_PARAM_KEY, raml.getDocumentation().stream().map(i -> i.getTitle() + " - " + i.getContent()).collect(Collectors.joining(" | ")));
        }


        swaggerJSON.put(API_INFO_PARAM_KEY, info);

        //Get the baseURI
        URL url = null;
        try {
            url = new URL(raml.getBaseUri());
        } catch (MalformedURLException e) {
            LOGGER.error("Error getting baseUri", e);
        }

        //Incase the schemes is mentioned in the RAML, use it. Else parse it from the API URL
        if (raml.getProtocols().size() > 0) {
            /* Uses Java8 stream API to convert things to lower case and return them as List
             * Stream api should be read left-to-right
             * .map() -> takes a lambda function for each element
             *   the inside lambda function then calls toString().toLowerCase() on each element (p)
             * .collect() -> the result is then collected as List
             */
            swaggerJSON.put(SCHEMES_PARAM_KEY, raml.getProtocols().stream().map(p -> p.toString().toLowerCase()).collect(Collectors.toList()));
        } else {
            if (url != null) swaggerJSON.put(SCHEMES_PARAM_KEY, Collections.singletonList(url.getProtocol()));
        }
        if (url != null) swaggerJSON.put(API_HOST_PARAM_KEY, url.getHost());

        //Basepath
        String basePath = getBasePath();
        if (basePath == null) {
            basePath = "/";
        }

        if (basePath.contains("{")) {
            int index = basePath.indexOf("/{");
            basePath = basePath.substring(0, index);

            //Base Uri params
            baseUriParamPresent = true;
            indexBaseUriParam = index;
        }

        swaggerJSON.put(PATH_PARAM_KEY, basePath);
    }

    //Remove all the required fields from the "properties" JSON Object
    private void hackPropertiesType(JSONObject jsonObject) throws JSONException {
        Iterator<?> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            if (jsonObject.get(key) instanceof JSONObject) {
                JSONObject jObj = ((JSONObject) jsonObject.get(key));
                if (jObj.has("required")) {
                    jObj.remove("required");
                }

                //Recurse through the child to remove any required fields
                hackPropertiesType((JSONObject) jsonObject.get(key));
            } else {
                if (key.equals("required")) {
                    jsonObject.remove(key);
                }
            }
        }
    }

    //Put all the definitions
    private void getDefinitions() throws JSONException {
        //Definitions key
        JSONObject def = new JSONObject();              //definitions JSONObject
        for (Map<String, String> m : raml.getSchemas()) {

            schemasList.addAll(m.keySet());

            //Collection for every field
            JSONObject coll = new JSONObject();

            //JSONObject containing names of "Required" fields
            JSONArray reqArr = new JSONArray();
            JSONObject newObj = new JSONObject(m.values().toArray()[0].toString());

            JSONObject propObj = null;
            if (newObj.has("properties")) {
                propObj = (JSONObject) newObj.get("properties");
            }

            //Iterate through all the keys and get required names
            if (propObj != null) {
                Iterator<?> keys = propObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next().toString();
                    JSONObject obj = new JSONObject(propObj.getString(key));
                    if (obj.has("required")) {
                        if (obj.getString("required").equals("true")) {
                            reqArr.put(key);
                        }
                    }
                }
                coll.put("properties", propObj);
            }
            hackPropertiesType(propObj);

            if (reqArr.length() > 0) {
                coll.put("required", reqArr);
            }

            //Put the type of the resource
            String type = new JSONObject(m.values().toArray()[0].toString()).getString("type");
            coll.put("type", type);

            //Put all the data into the def object
            def.put(m.keySet().iterator().next(), coll);
        }
        swaggerJSON.put(API_DEFINITION_PARAM_KEY, def);
    }

    //Put all the security schemes
    @SuppressWarnings("unchecked, EqualsBetweenInconvertibleTypes")
    private void getSecuritySchemes() throws JSONException {

        //Mapping between type names of Swagger and RAML
        HashMap<String, String> schemeTypeMapping = new HashMap<>();
        schemeTypeMapping.put(BASICAUTH_RAML, BASICAUTH_SWGR);
        schemeTypeMapping.put(OAUTH2_RAML, OAUTH2_SWGR);

        JSONObject securitySch = new JSONObject();
        for (Map<String, SecurityScheme> m : raml.getSecuritySchemes()) {
            for (Map.Entry<String, SecurityScheme> me : m.entrySet()) {

                if (me.getValue().getType().equals(OAUTH2_RAML)) {
                    //Get all te setting key value pairs
                    HashMap<String, Object> map = new HashMap<>(me.getValue().getSettings());

                    List<String> authGrants = (List<String>) map.get("authorizationGrants");
                    for (String s : authGrants) {
                        String key = "";
                        JSONObject value = new JSONObject();

                        value.put(TYPE_PARAM_KEY, schemeTypeMapping.get(me.getValue().getType()));
                        value.put(DESCRIPTION_PARAM_KEY, me.getValue().getDescription());

                        //Scope field
                        JSONObject jObj = new JSONObject();
                        for (String scopeString : (List<String>) map.get("scopes")) {
                            jObj.put(scopeString, "");
                        }
                        value.put(SCOPES_PARAM_KEY, jObj);

                        //Set the remaining value fields based on the grant type
                        if (s.toLowerCase().equals(CODEFLOW_PARAM_KEY)) {
                            key = me.getKey() + "_" + CODEFLOW_VALUE;
                            value.put(AUTHURL_PARAM_KEY, StringUtils.substring(map.get("authorizationUri").toString(), 1, -1));
                            value.put(TOKENURL_PARAM_KEY, StringUtils.substring(map.get("accessTokenUri").toString(), 1, -1));
                            value.put(FLOW_PARAM_KEY, CODEFLOW_VALUE);

                        } else if (s.toLowerCase().equals(TOKENFLOW_PARAM_KEY)) {
                            key = me.getKey() + "_" + TOKENFLOW_VALUE;
                            value.put(AUTHURL_PARAM_KEY, map.get("authorizationUri"));
                            value.put(AUTHURL_PARAM_KEY, StringUtils.substring(map.get("authorizationUri").toString(), 1, -1));
                            value.put(FLOW_PARAM_KEY, TOKENFLOW_VALUE);
                        } else if (s.toLowerCase().equals(OWNERFLOW_PARAM_KEY)) {
                            key = me.getKey() + "_" + OWNERFLOW_VALUE;
                            value.put(TOKENURL_PARAM_KEY, StringUtils.substring(map.get("accessTokenUri").toString(), 1, -1));
                            value.put(FLOW_PARAM_KEY, OWNERFLOW_VALUE);
                        } else if (s.toLowerCase().equals(CREDFLOW_PARAM_KEY)) {
                            key = me.getKey() + "_" + CREDFLOW_VALUE;
                            value.put(TOKENURL_PARAM_KEY, StringUtils.substring(map.get("accessTokenUri").toString(), 1, -1));
                            value.put(FLOW_PARAM_KEY, CREDFLOW_VALUE);
                        }

                        key = authGrants.size() == 1 ? me.getKey() : key;
                        securitySch.put(key, value);
                    }
                } else {
                    if (me.getValue().getDescribedBy().equals(BASICAUTH_RAML)) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put(TYPE_PARAM_KEY, schemeTypeMapping.get(me.getValue().getType()));
                        jsonObj.put(DESCRIPTION_PARAM_KEY, me.getValue().getDescription());
                        securitySch.put(me.getKey(), jsonObj);
                    }
                }
            }
        }

        if (securitySch.length() > 0) {
            swaggerJSON.put(SECDEF_PARAM_KEY, securitySch);
        }
    }

    //Put all the resources
    private void getResources() throws JSONException {
        JSONObject apiList = new JSONObject();
        if (raml.getResources().size() > 0) {
            retMethodsData(raml.getResources(), apiList, new HashMap<>());
        }
        swaggerJSON.put(PATHSVARIABLE_PARAM_KEY, apiList);
    }

    //Post process the json string like unescaping special chars (if any)
    private String postProcessString(String json) {
        String result = "";
        try {
            result = (new JSONObject(json)).toString(2).replace("\\/", "/");
        } catch (JSONException e) {
            LOGGER.error("JSON error", e);
        }

        return result;
    }

    @SuppressWarnings("WeakerAccess, unused")
    public String convertToSwagger(String raml) {
        return convertToSwagger(new ByteArrayInputStream(raml.getBytes()));
    }

    //Method called to convert RAML to Swagger
    @SuppressWarnings("WeakerAccess, unused")
    public String convertToSwagger(InputStream input) {

        //Pass the file stream to the RAML parser
        swaggerJSON = new JSONObject();
        raml = new RamlDocumentBuilder().build(input);

        try {
            //Swagger version
            putSwaggerHeader();

            //All the API info
            getAPIInfo();

            //All the definitions
            getDefinitions();

            //All the resources
            getResources();

            //All the security schemes
            getSecuritySchemes();
        } catch (JSONException e) {
            LOGGER.error("Error processing the RAML file");
            return null;
        }

        return postProcessString(swaggerJSON.toString());
    }

    //Method which recursively gets all the data for every resource
    private void retMethodsData(Map<String, Resource> resources, JSONObject apiList, HashMap<String, UriParameter> map) {
        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            try {
                HashMap<String, UriParameter> localMap = new HashMap<>(map);
                localMap.putAll(resourceEntry.getValue().getUriParameters());

                /*
                   If base URI param is present, that part in the baseUri must be scrapped from the base uri and put
                   before all the resource paths.
                   Reason: Swagger doesn't allow path templating in its 'host' and 'basePath' fields. But RAML supports it.
                   So, this has to be done.
                 */

                if (baseUriParamPresent) {
                    if (raml.getBaseUriParameters() != null && raml.getBaseUriParameters().size() > 0) {
                        localMap.putAll(raml.getBaseUriParameters());
                    }
                }

                JSONObject topResource = getSpecificResourceData(resourceEntry, new HashMap<>(localMap));
                String key = topResource.keys().next().toString();
                JSONObject value = (JSONObject) topResource.get(key);

                //Add the extra part in the base path to every resource
                if (baseUriParamPresent) {
                    key = raml.getBasePath().substring(indexBaseUriParam) + key;
                }
                apiList.put(key, value);

                //Incase there are subresources for a resource
                if (resourceEntry.getValue().getResources().size() > 0) {
                    //Send the map object by value(new HashMap<>(map)) so that values of a particular resource will have only its uriparams
                    retMethodsData(resourceEntry.getValue().getResources(), apiList, new HashMap<>(localMap));
                }
            } catch (JSONException e) {
                LOGGER.error("JSON error", e);
            }
        }
    }

    //Get all the params and other data for a given specific resource
    private JSONObject getSpecificResourceData(Map.Entry<String, Resource> resourceEntry, HashMap<String, UriParameter> map) {
        JSONObject apiMap = new JSONObject();
        try {

            JSONObject methodsList = new JSONObject();

            //Iterate for every method of the resource
            for (Map.Entry<ActionType, Action> action : resourceEntry.getValue().getActions().entrySet()) {
                getMethodsDescription(methodsList, action, map);
            }

            apiMap.put(resourceEntry.getValue().getUri(), methodsList);
        } catch (JSONException e) {
            LOGGER.error("JSON error", e);
        }
        return apiMap;
    }

    //Write the details relating to the method
    private void getMethodsDescription(JSONObject operations, Map.Entry<ActionType, Action> action, HashMap<String, UriParameter> map) {
        JSONObject operation = new JSONObject();
        try {

            //Get all the types that the method consumes
            operation.put(CONSUMES_MEDIATYPE_PARAM_KEY, getConsumesArray(action));

            //Get all the types that the method produces in response
            operation.put(PRODUCES_MEDIATYPE_PARAM_KEY, getProducesArray(action));

            //Method description
            operation.put(DESCRIPTION_PARAM_KEY, action.getValue().getDescription());

            //read parameters headers,body,query etc.
            Collection<JSONObject> parameters = new ArrayList<>();

            //add header/query & body params
            getHeaderParams(action, parameters);
            getQueryParams(action, parameters);
            getBodyParams(action, parameters);
            getPathParams(map.entrySet(), parameters);

            if (parameters.size() > 0) {
                operation.put(PARAMETERS_PARAM_KEY, parameters);
            }

            //Method responses
            JSONObject resp = getResponseInfo(action);
            if (resp.length() > 0) {
                operation.put(RESPONSES_PARAM_KEY, resp);
            } else {
                resp = new JSONObject();
                resp.put("200", new JSONObject().put("description", "OK"));
                operation.put(RESPONSES_PARAM_KEY, resp);
            }
            operations.put(action.getKey().toString().toLowerCase(), operation);

        } catch (JSONException e) {
            LOGGER.error("JSON error", e);
        }
    }

    //Return the MIME types that the method consumes
    private JSONArray getConsumesArray(Map.Entry<ActionType, Action> action) {
        List<String> consList = new ArrayList<>();
        for (Map.Entry<String, MimeType> map : action.getValue().getBody().entrySet()) {
            if (!consList.contains(map.getKey())) {
                consList.add(map.getKey());
            }
        }

        JSONArray consumesArr = new JSONArray(consList);
        return consumesArr.length() > 0 ? consumesArr : null;
    }

    //Return the MIME types that the method produces
    private JSONArray getProducesArray(Map.Entry<ActionType, Action> action) {
        List<String> prodList = new ArrayList<>();
        for (Map.Entry<String, Response> responsesMap : action.getValue().getResponses().entrySet()) {
            for (Map.Entry<String, MimeType> prodBody : responsesMap.getValue().getBody().entrySet()) {
                if (!prodList.contains(prodBody.getKey())) {
                    prodList.add(prodBody.getKey());
                }
            }
        }

        JSONArray prodArr = new JSONArray(prodList);
        return prodArr.length() > 0 ? prodArr : null;
    }

    // Generic function processing various parameters from RAML. All of them are subclasses of AbstractParam, so a
    // safe generalization can be made. The target collection/store is done using functional programing principles by
    // passing in the storing function. Later on lambda expressions or simple function references can be used in an
    // elegant way.
    private <T extends AbstractParam> void getParams(Set<Map.Entry<String, T>> entries, String paramType, Consumer<JSONObject> store) throws JSONException {
        for (Map.Entry<String, T> entry : entries) {

            HashMap<String, Object> values = new HashMap<>();
            values.put(NAME_MAP_KEY, entry.getKey());
            values.put(DEFVALUE_MAP_KEY, entry.getValue().getDefaultValue());
            values.put(DESC_MAP_KEY, entry.getValue().getDescription());
            values.put(ISREQD_MAP_KEY, entry.getValue().isRequired());
            values.put(TYPE_MAP_KEY, entry.getValue().getType().toString());
            values.put(ENUM_MAP_KEY, entry.getValue().getEnumeration());
            values.put(MAX_MAP_KEY, entry.getValue().getMaximum());
            values.put(MIN_MAP_KEY, entry.getValue().getMinimum());
            values.put(MAXLEN_MAP_KEY, entry.getValue().getMaxLength());
            values.put(MINLEN_MAP_KEY, entry.getValue().getMinLength());
            values.put(EXAMPLE_MAP_KEY, entry.getValue().getExample());
            values.put(PATTERN_MAP_KEY, entry.getValue().getPattern());
            values.put(REPEAT_MAP_KEY, entry.getValue().isRepeat());
            values.put(PARAMTYPE_MAP_KEY, paramType);

            // Calling the passed in store function. Note that not a structure is passed in, but a function. See
            // functional programing principles in Java.
            store.accept(getParametersInfo(values));
        }
    }

    // ---- The below code are the simplified version of their former implementation removing 3-times code duplication

    //Get all the path(URI) parameters for a specific method
    private void getPathParams(Set<Map.Entry<String, UriParameter>> entries, Collection<JSONObject> parameters)
            throws JSONException {

        getParams(entries, PARAMTYPE_PATH, parameters::add);
    }

    //Get all the header params for a specific method
    private void getHeaderParams(Map.Entry<ActionType, Action> action, Collection<JSONObject> parameters)
            throws JSONException {

        getParams(action.getValue().getHeaders().entrySet(), PARAMTYPE_HEADER, parameters::add);
    }

    //Get all the query params for a specific method
    private void getQueryParams(Map.Entry<ActionType, Action> action, Collection<JSONObject> parameters)
            throws JSONException {

        getParams(action.getValue().getQueryParameters().entrySet(), PARAMTYPE_QUERY, parameters::add);
    }

    //Get all the body params for a specific method
    private void getBodyParams(Map.Entry<ActionType, Action> action,
                                      Collection<JSONObject> parameters) throws JSONException {

        for (Map.Entry<String, MimeType> mimeType : action.getValue().getBody().entrySet()) {
            String schema = "";
            if (mimeType.getValue().getSchema() != null) {
                schema = mimeType.getValue().getSchema();
            }

            HashMap<String, Object> values = new HashMap<>();
            values.put(NAME_MAP_KEY, "body");
            values.put(ISREQD_MAP_KEY, Boolean.TRUE);
            values.put(SCHEMA_MAP_KEY, schema);
            values.put(PARAMTYPE_MAP_KEY, PARAMTYPE_BODY);

            JSONObject qp = getParametersInfo(values);
            parameters.add(qp);
        }
    }

    private JSONObject getResponseInfo(Map.Entry<ActionType, Action> action) throws JSONException {

        JSONObject retObj = new JSONObject();
        for (Map.Entry<String, Response> responsesMap : action.getValue().getResponses().entrySet()) {

            //Get the response description
            JSONObject fields = new JSONObject();
            String description = responsesMap.getValue().getDescription() == null ? getResponseMessage(Integer.valueOf(responsesMap.getKey())) : responsesMap.getValue().getDescription();
            fields.put(DESCRIPTION_PARAM_KEY, description);

            //Get the response schema
            for (Map.Entry<String, MimeType> me : responsesMap.getValue().getBody().entrySet()) {
                String schema = me.getValue().getSchema();

                //Suppose schema is well-formed
                if (schema != null && schema.length() > 0) {

                    //If schema definition is already defined in the "Definitions" field
                    if (schemasList.contains(schema)) {
                        fields.put(SCHEMA_PARAM_KEY, new JSONObject().put(REFERENCE_PARAM_KEY, "#/definitions/" + schema));
                    } else {
                        JSONObject jsonObj = new JSONObject(schema);
                        if (jsonObj.has("$schema"))
                            jsonObj.remove("$schema");
                        if (jsonObj.has("required"))
                            jsonObj.remove("required");
                        fields.put(SCHEMA_PARAM_KEY, jsonObj);
                    }
                }
            }
            retObj.put(responsesMap.getKey(), fields);
        }
        return retObj;
    }

    //Bundle all the parameter values into a JSON object
    @SuppressWarnings("unchecked")
    private JSONObject getParametersInfo(HashMap<String, Object> values)
            throws JSONException {

        JSONObject qp = new JSONObject();

        //Incase the parameter type is defined
        if (values.get(PARAMTYPE_MAP_KEY) != null) {
            qp.put(PARAMTYPE_PARAM_KEY, values.get(PARAMTYPE_MAP_KEY));
        }

        //Incase the parameter name is defined
        if (values.get(NAME_MAP_KEY) != null) {
            qp.put(NAME_PARAM_KEY, values.get(NAME_MAP_KEY));
        }

        //Incase the default value is defined
        if (values.get(DEFVALUE_MAP_KEY) != null) {
            qp.put(DEFAULTVALUE_PARAM_KEY, values.get(DEFVALUE_MAP_KEY));
        }

        //Incase the description field is defined
        if (values.get(DESC_MAP_KEY) != null) {
            qp.put(DESCRIPTION_PARAM_KEY, values.get(DESC_MAP_KEY));
        }

        //Incase the Required field is defined
        if (values.get(ISREQD_MAP_KEY) != null && (Boolean) values.get(ISREQD_MAP_KEY)) {
            qp.put(REQUIRED_PARAM_KEY, values.get(ISREQD_MAP_KEY));
        }

        //Incase repeat is defined, then the type is "array"
        if (values.get(REPEAT_MAP_KEY) != null && (Boolean) values.get(REPEAT_MAP_KEY)) {
            qp.put(TYPE_PARAM_KEY, ARRAYTYPE_PARAM_KEY);
            JSONObject jObj = new JSONObject();
            jObj.put(TYPE_MAP_KEY, ((String) values.get(TYPE_MAP_KEY)).toLowerCase());
            qp.put(ITEMS_PARAM_KEY, jObj);
        } else {
            if (values.get(TYPE_MAP_KEY) != null) {
                qp.put(TYPE_PARAM_KEY, ((String) values.get(TYPE_MAP_KEY)).toLowerCase());
            }
        }

        ////Incase the schema is defined
        if (values.get(SCHEMA_MAP_KEY) != null && !(values.get(SCHEMA_MAP_KEY)).equals("")) {

            //If schema definition is already defined in the "Definitions" field
            if (schemasList.contains(values.get(SCHEMA_MAP_KEY).toString())) {
                qp.put(SCHEMA_PARAM_KEY, new JSONObject().put(REFERENCE_PARAM_KEY, "#/definitions/" + values.get(SCHEMA_MAP_KEY)));
            } else {
                JSONObject jsonObj = new JSONObject((String) values.get(SCHEMA_MAP_KEY));
                if (jsonObj.has("$schema"))
                    jsonObj.remove("$schema");
                if (jsonObj.has("required"))
                    jsonObj.remove("required");
                qp.put(SCHEMA_PARAM_KEY, jsonObj);
            }
        }

        //Incase the enum array is not empty
        if (values.get(ENUM_MAP_KEY) != null && ((List<String>) values.get(ENUM_MAP_KEY)).size() > 0) {
            qp.put(ENUM_PARAM_KEY, (List<String>) values.get(ENUM_MAP_KEY));
        }

        //Incase the max value is defined
        if (values.get(MAX_MAP_KEY) != null) {
            qp.put(MAX_PARAM_KEY, values.get(MAX_MAP_KEY));
        }

        //Incase the min value is defined
        if (values.get(MIN_MAP_KEY) != null) {
            qp.put(MIN_PARAM_KEY, values.get(MIN_MAP_KEY));
        }

        //Incase the maxLength value is defined
        if (values.get(MAXLEN_MAP_KEY) != null) {
            qp.put(MAXLEN_PARAM_KEY, values.get(MAXLEN_MAP_KEY));
        }

        if (values.get(MINLEN_MAP_KEY) != null) {
            qp.put(MINLEN_PARAM_KEY, values.get(MINLEN_MAP_KEY));
        }

        //Incase the minLength value is defined
        if (values.get(PATTERN_MAP_KEY) != null) {
            qp.put(PATTERN_PARAM_KEY, values.get(PATTERN_MAP_KEY));
        }
        return qp;
    }
}
