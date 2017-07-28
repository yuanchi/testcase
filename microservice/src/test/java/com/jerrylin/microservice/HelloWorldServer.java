package com.jerrylin.microservice;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;

import java.nio.file.Paths;


public class HelloWorldServer {
	public static void main(String[]args){
		// examples https://github.com/undertow-io/undertow/tree/master/examples
		// api ref. http://undertow.io/javadoc/1.4.x/index.html
		// ref. https://stackoverflow.com/questions/39742014/routing-template-format-for-undertow
		// tutorial ref. https://www.stubbornjava.com/posts/url-routing-with-undertow-embedded-http-server
		PathHandler paths = Handlers.path()
			.addExactPath("/path1", exchange->{
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("This is PATH1");
			})
			.addExactPath("/path2", exchange->{
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("This is PATH2");
			})
			.addPrefixPath("/path3", exchange->{
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("This path is with PATH3 Prefix");
			});		
		Undertow server = Undertow.builder()
			.addHttpListener(8080, "localhost")
			.setHandler(exchange->{// simple example
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("Hello World");
			})
			.setHandler(paths)
			.setHandler(Handlers.path() // this would overwrite previous PathHandler
				.addPrefixPath("/api", Handlers.routing()
					.get("/customers", exchange->{
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
						exchange.getResponseSender().send("This is RESTful api/customers");
					})
					.delete("/customers/{customerId}", exchange->{
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
						exchange.getResponseSender().send("This is RESTful api/customers for deleting");
					})
					.setFallbackHandler(exchange->{
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
						exchange.getResponseSender().send("Anything else here");
					})
				)
				.addExactPath("/", Handlers.redirect("/static"))
				.addPrefixPath("/static", // static page resources
					new ResourceHandler(
						new PathResourceManager(Paths.get("./path/to/www/"), 100))
						.setWelcomeFiles("index.html")
				)
						
			)
			.build();
		server.start();
	}
}
