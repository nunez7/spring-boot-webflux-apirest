package com.vass.webflux.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.vass.webflux.app.handler.ProductoHandler;


import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {
		
	@Bean
	public RouterFunction<ServerResponse> router(ProductoHandler handler){
		return route(GET("/api/v2/productos"), request -> handler.listar(request));
	}

}
