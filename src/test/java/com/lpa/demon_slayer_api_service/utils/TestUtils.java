package com.lpa.demon_slayer_api_service.utils;

import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {

    public static String loadJson(String path) throws IOException {
        return new String(Files.readAllBytes(
                Paths.get(ResourceUtils.getFile("classpath:" + path).toURI())
        ));
    }
}

