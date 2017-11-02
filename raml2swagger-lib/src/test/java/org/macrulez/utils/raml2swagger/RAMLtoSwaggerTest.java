package org.macrulez.utils.raml2swagger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@NoArgsConstructor
public class RAMLtoSwaggerTest {
    private static final String PRODUCT_API_RAML = "/product-api.raml";
    private final RAMLtoSwagger raml2Swagger = new RAMLtoSwagger();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void convertToSwaggerValidJson() throws Exception {
        String raml = IOUtils.resourceToString("/product-api.raml", Charset.forName("UTF-8"));
        Assert.assertNotNull(raml);

        String swagger = raml2Swagger.convertToSwagger(raml);
        Assert.assertNotNull(swagger);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> json = objectMapper.readValue(swagger, typeRef);
        Assert.assertNotNull(json);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void convertToSwaggerFromString() throws Exception {
        String raml = IOUtils.resourceToString("/product-api.raml", Charset.forName("UTF-8"));
        Assert.assertNotNull(raml);

        String swagger = raml2Swagger.convertToSwagger(raml);
        Assert.assertNotNull(swagger);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> json = objectMapper.readValue(swagger, typeRef);
        Assert.assertNotNull(json);

        /* A bunch of checks based on the test RAML, to prove recursive functions work correctly */
        Assert.assertEquals("Swagger version should be 2.0", json.get("swagger").toString(), "2.0");
        Assert.assertTrue("Info section should be a map", json.get("info") instanceof Map);
        Assert.assertEquals("Info section should have three keys", 3, ((Map<String, Object>)json.get("info")).size());
        Assert.assertTrue("Schemes section should be a list", json.get("schemes") instanceof List);
        Assert.assertEquals("Info section should have two values", 2, ((List<String>)json.get("schemes")).size());
        Assert.assertTrue("Paths section should be a map", json.get("paths") instanceof Map);
        Assert.assertEquals("Paths section should have 62 keys", 62, ((Map<String, Object>)json.get("paths")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkQueryParameters() throws Exception {
        String raml = IOUtils.resourceToString("/product-api.raml", Charset.forName("UTF-8"));
        Assert.assertNotNull(raml);

        String swagger = raml2Swagger.convertToSwagger(raml);
        Assert.assertNotNull(swagger);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> json = objectMapper.readValue(swagger, typeRef);
        Assert.assertNotNull(json);

        Map<String, Object> paths = (Map<String, Object>)json.get("paths");
        Map<String, Object> path = (Map<String, Object>)paths.get("/{version}/cache/clear");
        Map<String, Object> method = (Map<String, Object>)path.get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>)method.get("parameters");
        Assert.assertEquals("There should be 4 parameters", 4, parameters.size());
        Assert.assertEquals("Parameter type should be 'query'", "query", parameters.get(0).get("in"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkPathParameters() throws Exception {
        String raml = IOUtils.resourceToString("/product-api.raml", Charset.forName("UTF-8"));
        Assert.assertNotNull(raml);

        String swagger = raml2Swagger.convertToSwagger(raml);
        Assert.assertNotNull(swagger);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> json = objectMapper.readValue(swagger, typeRef);
        Assert.assertNotNull(json);

        Map<String, Object> paths = (Map<String, Object>)json.get("paths");
        Map<String, Object> path = (Map<String, Object>)paths.get("/{version}/used-families/{familyName}/{modelName}/{type}");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>)path.get("parameters");
        Assert.assertEquals("There should be 3 parameters", 3, parameters.size());
        Assert.assertEquals("Parameter type should be 'query'", "path", parameters.get(0).get("in"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkCorrectParametersResolution() throws Exception {
        String raml = IOUtils.resourceToString("/product-api.raml", Charset.forName("UTF-8"));
        Assert.assertNotNull(raml);

        String swagger = raml2Swagger.convertToSwagger(raml);
        Assert.assertNotNull(swagger);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> json = objectMapper.readValue(swagger, typeRef);
        Assert.assertNotNull(json);

        Map<String, Object> paths = (Map<String, Object>)json.get("paths");
        Map<String, Object> path = (Map<String, Object>)paths.get("/{version}/models/{externalId}/derivativeFeatureCombos/{featureList}");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>)path.get("parameters");
        Assert.assertEquals("There should be 2 parameters", 2, parameters.size());
    }


    @Test
    public void convertToSwaggerFromInputStream() throws Exception {
        String raml = IOUtils.resourceToString(PRODUCT_API_RAML, Charset.forName("UTF-8"));
        Assert.assertNotNull(raml);

        String swagger = raml2Swagger.convertToSwagger(raml);
        Assert.assertNotNull(swagger);

        String swagger2 = raml2Swagger.convertToSwagger(getClass().getResourceAsStream(PRODUCT_API_RAML));
        Assert.assertNotNull(swagger2);
        Assert.assertEquals("The two swaggers output should be the same", swagger, swagger2);
    }
}