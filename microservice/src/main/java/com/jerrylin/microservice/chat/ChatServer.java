package com.jerrylin.microservice.chat;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

public class ChatServer {
	public static Undertow startup(){
		Undertow server = Undertow.builder()
			.addHttpListener(8080, "localhost")
			.setHandler(path()
					.addPrefixPath("/myapp", websocket((exchange, channel)->{
							channel.getReceiveSetter().set(new AbstractReceiveListener(){
								@Override
								protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message){
									final String messageData = message.getData();
									for(WebSocketChannel session : channel.getPeerConnections()){
										WebSockets.sendText(messageData, session, null);
									}
								}
							});
							channel.resumeReceives();
						})
					)
					.addPrefixPath("/", resource(new ClassPathResourceManager(ChatServer.class.getClassLoader(), ChatServer.class.getPackage()))
							.addWelcomeFiles("index.html")))
			.build();
		server.start();
		return server;
	}
}
