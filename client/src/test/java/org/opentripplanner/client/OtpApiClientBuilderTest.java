package org.opentripplanner.client;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class OtpApiClientBuilderTest {

    @Test
    public void buildWithDefaultParams() throws IOException {
        final var client = OtpApiClient.builder()
                .baseUri("https://otp2debug.dev.entur.org/")
                .timeZone(ZoneId.of("Europe/Oslo"))
                .build();
        assertTrue(client.agencies().stream().anyMatch(agency -> agency.name().equals("Flytoget")));
    }


    @Test
    public void buildWithExtraParams() throws IOException {
        final var customURI = "https://otp2debug.dev.entur.org/non-extisting-endpoint";
        final AtomicBoolean customHttpClientWasUsed = new AtomicBoolean(false);
        final var httpClient = HttpClientBuilder.create()
                .addRequestInterceptorLast(
                        (httpRequest, entityDetails, httpContext) -> {
                            customHttpClientWasUsed.set(true);
                            try {
                                assertEquals(customURI, httpRequest.getUri().toString());
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .build();
        var client = OtpApiClient.builder()
                .graphQLUri(customURI)
                .timeZone(ZoneId.of("Europe/Oslo"))
                .httpClient(httpClient)
                .build();

        assertThrows(IOException.class, client::agencies, "We expect a 404 error");
        assertTrue(customHttpClientWasUsed.get(), "Custom http client was not used");
    }
}
