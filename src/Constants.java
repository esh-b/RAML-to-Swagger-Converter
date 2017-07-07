
public interface Constants {

    String SWAGGERVERSION_PARAM_KEY = "swagger";
    String SWAGGERVERSION_PARAM_VALUE = "2.0";
    String PATH_PARAM_KEY = "basePath";
    String API_HOST_PARAM_KEY = "host";
    String API_INFO_PARAM_KEY = "info";
    String API_TITLE_PARAM_KEY = "title";
    String API_VERSION_PARAM_KEY = "version";
    String API_DEFINITION_PARAM_KEY = "definitions";
    String SECDEF_PARAM_KEY = "securityDefinitions";

    String PATHSVARIABLE_PARAM_KEY = "paths";
    String SCHEMES_PARAM_KEY = "schemes";
    String RESPONSES_PARAM_KEY = "responses";

    String PARAMETERS_PARAM_KEY = "parameters";
    String TYPE_PARAM_KEY = "type";
    String DESCRIPTION_PARAM_KEY = "description";
    String PRODUCES_MEDIATYPE_PARAM_KEY = "produces";
    String CONSUMES_MEDIATYPE_PARAM_KEY = "consumes";

    String NAME_PARAM_KEY = "name";
    String ENUM_PARAM_KEY = "enum";
    String MAX_PARAM_KEY = "maximum";
    String MIN_PARAM_KEY = "minimum";
    String MAXLEN_PARAM_KEY = "maxLength";
    String MINLEN_PARAM_KEY = "minLength";
    String PATTERN_PARAM_KEY = "pattern";
    String SCHEMA_PARAM_KEY = "schema";
    String DEFAULTVALUE_PARAM_KEY = "default";
    String REQUIRED_PARAM_KEY = "required";
    String PARAMTYPE_PARAM_KEY = "in";

    String PARAMTYPE_PATH = "path";
    String PARAMTYPE_HEADER = "header";
    String PARAMTYPE_QUERY = "query";
    String PARAMTYPE_BODY = "body";

    String MIMETYPE_JSON = "application/json";
    String MIMETYPE_XML = "application/xml";

    //Security schemes
    String SCOPES_PARAM_KEY = "scopes";
    String AUTHURL_PARAM_KEY = "authorizationUrl";
    String TOKENURL_PARAM_KEY = "tokenUrl";

    //Flow Strings
    String FLOW_PARAM_KEY = "flow";
    String CODEFLOW_PARAM_KEY = "code";
    String CODEFLOW_VALUE = "accessCode";
    String TOKENFLOW_PARAM_KEY = "token";
    String TOKENFLOW_VALUE = "implicit";
    String OWNERFLOW_PARAM_KEY = "owner";
    String OWNERFLOW_VALUE = "password";
    String CREDFLOW_PARAM_KEY = "credentials";
    String CREDFLOW_VALUE = "application";

    //Parameter Strings
    String NAME_MAP_KEY = "name";
    String DEFVALUE_MAP_KEY = "defValue";
    String DESC_MAP_KEY = "desc";
    String TYPE_MAP_KEY = "type";
    String ISREQD_MAP_KEY = "isReqd";
    String ENUM_MAP_KEY = "Enum";
    String SCHEMA_MAP_KEY = "schema";
    String MAX_MAP_KEY = "max";
    String MIN_MAP_KEY = "min";
    String MAXLEN_MAP_KEY = "maxLen";
    String MINLEN_MAP_KEY = "minLen";
    String EXAMPLE_MAP_KEY = "ex";
    String PATTERN_MAP_KEY = "ptrn";
    String REPEAT_MAP_KEY = "repeat";
    String PARAMTYPE_MAP_KEY = "paramType";

    //Type array Strings
    String ITEMS_PARAM_KEY = "items";
    String ARRAYTYPE_PARAM_KEY = "array";
    String REFERENCE_PARAM_KEY = "$ref";

    //Security types
    String BASICAUTH_RAML = "Basic Authentication";
    String OAUTH2_RAML = "OAuth 2.0";
    String BASICAUTH_SWGR = "basic";
    String OAUTH2_SWGR = "oauth2";

}
