package de.bruss.demontoo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({ResourceProperties.class})
public class MvcConfig extends WebMvcConfigurerAdapter {

    private ResourceProperties resourceProperties;

    @Autowired
    public MvcConfig(ResourceProperties resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

    @Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/").setViewName("forward:/index.html");
		super.addViewControllers(registry);
	}

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Duration cachePeriod = resourceProperties.getCache().getPeriod();

        final String[] staticLocations = resourceProperties.getStaticLocations();
        registry.addResourceHandler(
                "/**/*.css",
                "/**/*.html",
                "/**/*.js",
                "/**/*.json",
                "/**/*.bmp",
                "/**/*.map",
                "/**/*.gif",
                "/**/*.jpeg",
                "/**/*.jpg",
                "/**/*.png",
                "/**/*.ttf",
                "/**/*.eot",
                "/**/*.svg",
                "/**/*.woff",
                "/**/*.woff2",
                "/**/*.exe",
                "/**/*.msi",
                "/**/*.pkg",
                "/**/*.deb",
                "/**/*.cab",
                "/**/*.rpm",
                "/**/*.zip"
        ).addResourceLocations(staticLocations);

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/index.html")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) {
                        return location.exists() && location.isReadable() ? location : null;
                    }
                });

        super.addResourceHandlers(registry);
    }
}
