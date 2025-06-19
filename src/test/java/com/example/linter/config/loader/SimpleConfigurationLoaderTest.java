package com.example.linter.config.loader;

import com.example.linter.config.*;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class SimpleConfigurationLoaderTest {
    
    @Test
    void testLoadConfigurationFromResources() {
        ConfigurationLoader loader = new ConfigurationLoader();
        
        InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream("test-config.yaml");
        
        assertNotNull(inputStream, "Test config file should exist");
        
        LinterConfiguration config = loader.loadConfiguration(inputStream);
        
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().metadata());
        assertEquals(2, config.document().metadata().attributes().size());
        
        var titleAttr = config.document().metadata().attributes().get(0);
        assertEquals("title", titleAttr.name());
        assertEquals(Severity.ERROR, titleAttr.severity());
    }
}