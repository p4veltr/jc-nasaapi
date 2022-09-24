package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final String NASA_API_KEY = "gkAehV3PeYAideqFEM2PQ1isQWvRZW38HrmEXYp3";
    private static final String NASA_URL = "https://api.nasa.gov/planetary/apod?api_key=" + NASA_API_KEY;

    public static void main(String[] args) {
        String jsonData = downloadFromURL(NASA_URL);

        ObjectMapper mapper = new ObjectMapper();
        NASADailyPost post;
        try {
            post = mapper.readValue(jsonData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        downloadFromURL(post.getUrl(), getFileNameFromURL(post.getUrl()));
    }

    public static String getFileNameFromURL(String url) {
        String[] arr = url.split("/");
        return arr[arr.length-1];
    }

    public static void downloadFromURL(String url, String fileName) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
        }
    }

    public static String downloadFromURL(String url) {
        HttpGet request = new HttpGet(url);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build()) {
            CloseableHttpResponse response = httpClient.execute(request);
            return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}