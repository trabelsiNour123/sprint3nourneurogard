package com.neuroguard.reservationservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = "";
        try {
            if (response.body() != null) {
                body = new String(response.body().asInputStream().readAllBytes());
            }
        } catch (IOException e) {
            // Ignore
        }

        if (response.status() == 400) {
            return new IllegalArgumentException("Bad Request: " + body);
        } else if (response.status() == 401) {
            return new IllegalArgumentException("Unauthorized: " + body);
        } else if (response.status() == 403) {
            return new IllegalArgumentException("Forbidden: " + body);
        } else if (response.status() == 404) {
            return new IllegalArgumentException("Not Found: " + body);
        } else if (response.status() >= 500) {
            return new RuntimeException("Server error (" + response.status() + "): " + body);
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}
