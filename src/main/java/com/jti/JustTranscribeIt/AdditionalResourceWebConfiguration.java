package com.jti.JustTranscribeIt;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class AdditionalResourceWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        String uploadPathResource = getSystemDepPath();
        registry.addResourceHandler("/tmp/**")
                .addResourceLocations(uploadPathResource);
    }

    public String getSystemDepPath() {
        String os = System.getProperty("os.name");
        String result = Paths.get(System.getProperty("user.dir"), "src", "main", "resources",
                "static", "tmp").toString();
        if (os.startsWith("Win")) {
            result = result.replace("\\", "/");
            result = "file:///" + result + "/";
        }
        else {
            result = "file:" + result + "/";
        }
        return result;
    }
}