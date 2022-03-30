package com.example.hzwatch.service;

import com.example.hzwatch.domain.Product;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ParseHtmlServiceTest {

    private ParseHtmlService parseHtmlService;

    @Before
    public void setUp() {
        parseHtmlService = new ParseHtmlService();
    }

    @Test
    public void parse() throws IOException {
        List<Product> products = parseHtmlService.parse(loadHzwatchHtml());
        Assert.assertNotNull(products);
        Assert.assertEquals(9, products.size());
    }

    private String loadHzwatchHtml() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("hzwatch.html");

        StringBuilder sb = new StringBuilder();
        String line;

        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();

        return sb.toString();
    }
}