package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.fail;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class StreamLambdaHandlerTest {

  private static StreamLambdaHandler handler;
  private static Context lambdaContext;

  @BeforeAll
  public static void setUp() {
    handler = new StreamLambdaHandler();
    lambdaContext = new MockLambdaContext();
  }

  @Test
  @Disabled
  public void ping_streamRequest_respondsWithHello() {
    InputStream requestStream = new AwsProxyRequestBuilder("/api/posts", HttpMethod.GET)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .buildStream();
    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

    handle(requestStream, responseStream);

    AwsProxyResponse response = readResponse(responseStream);
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());

    assertFalse(response.isBase64Encoded());

    assertFalse(response.getBody().isEmpty());

    assertTrue(response.getMultiValueHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
    assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
        .startsWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @Disabled
  public void invalidResource_streamRequest_responds404() {
    InputStream requestStream = new AwsProxyRequestBuilder("/api/pasts", HttpMethod.GET)
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .buildStream();
    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

    handle(requestStream, responseStream);

    AwsProxyResponse response = readResponse(responseStream);
    assertNotNull(response);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode());
  }

  private void handle(InputStream is, ByteArrayOutputStream os) {
    try {
      handler.handleRequest(is, os, lambdaContext);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private AwsProxyResponse readResponse(ByteArrayOutputStream responseStream) {
    try {
      return LambdaContainerHandler.getObjectMapper()
          .readValue(responseStream.toByteArray(), AwsProxyResponse.class);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error while parsing response: " + e.getMessage());
    }
    return null;
  }
}
