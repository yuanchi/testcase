package com.jerrylin.erp.initialize.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages={"com.jerrylin.erp.controller"})
public class WebConfig extends WebMvcConfigurerAdapter {
	private static final Charset UTF8 = Charset.forName("UTF-8");
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
	public MappingJackson2HttpMessageConverter jacksonMessageConverter(){
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> jsonTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
		jsonTypes.add(MediaType.TEXT_PLAIN);
		jsonConverter.setSupportedMediaTypes(jsonTypes);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		jsonConverter.setObjectMapper(mapper);
		
		return jsonConverter;
	}
	/**
	 * configure json converter charset to UTF-8
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
		converters.add(new StringHttpMessageConverter(UTF8));
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(jacksonMessageConverter());
		super.configureMessageConverters(converters);
	}
}
