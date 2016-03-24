package com.jerrylin.erp.initialize.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages={"com.jerrylin.erp.controller"})
public class WebConfig extends WebMvcConfigurerAdapter {
	@Bean
	public ViewResolver viewResolver(ContentNegotiationManagerFactoryBean contentManager){
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		resolver.setExposeContextBeansAsAttributes(true);
		resolver.setViewClass(JstlView.class);
		
//		TilesViewResolver resolver = new TilesViewResolver();
		
		// priority: file extension->Accept header
//		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
//		resolver.setContentNegotiationManager(contentManager.getObject());
		return resolver;
	}
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer){
		configurer.enable();
	}
	
	@Bean
	public ContentNegotiationManagerFactoryBean contentManager(){
		ContentNegotiationManagerFactoryBean cnmfb = new ContentNegotiationManagerFactoryBean();
		cnmfb.setFavorPathExtension(true);
		cnmfb.setIgnoreAcceptHeader(false);
		cnmfb.setDefaultContentType(MediaType.TEXT_HTML);
		cnmfb.setUseJaf(false);
		
		Properties props = new Properties();
		props.setProperty("html", "text/html");
		props.setProperty("json", "application/json");
		props.setProperty("xml", "application/xml");
		cnmfb.setMediaTypes(props);
		
		return cnmfb;
	}
}
