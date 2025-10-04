package com.example.nasa.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

public class WebInit extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{HibernateConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return new Filter[]{characterEncodingFilter};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // Configure multipart for file upload
        String uploadLocation = System.getProperty("java.io.tmpdir");
        long maxFileSize = 5242880; // 5MB
        long maxRequestSize = 20971520; // 20MB
        int fileSizeThreshold = 1048576; // 1MB

        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                uploadLocation, maxFileSize, maxRequestSize, fileSizeThreshold
        );

        registration.setMultipartConfig(multipartConfig);
    }
}
