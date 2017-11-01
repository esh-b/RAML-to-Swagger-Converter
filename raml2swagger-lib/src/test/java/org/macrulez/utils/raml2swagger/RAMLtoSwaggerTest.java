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