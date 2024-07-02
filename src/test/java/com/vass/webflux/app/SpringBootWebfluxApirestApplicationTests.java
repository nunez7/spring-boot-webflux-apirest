package com.vass.webflux.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vass.webflux.app.models.documents.Categoria;
import com.vass.webflux.app.models.documents.Producto;
import com.vass.webflux.app.models.service.ProductoService;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductoService service;
	
	@Value("${config.base.endpoint}")
	private String URL_BASE;

	@Test
	void listarTest() {
		client.get()
		.uri(URL_BASE)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class)
		.consumeWith( response -> {
			List<Producto> productos = response.getResponseBody();
			productos.forEach(p -> System.out.println(p.getNombre()));

			Assertions.assertThat(productos.size()>0 ).isTrue();
		});
		//.hasSize(10);
		
	}
	
	@Test
	void verTest() {
		Producto producto = service.findByNombre("Impresora Epson").block();
		client.get()
		.uri(URL_BASE+"/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Impresora Epson");
	}
	
	@Test
	public void crearTest() {
		
		Categoria categoria = service.findCategoriaByNombre("Celulares").block();
		
		Producto producto = new Producto("Mesa Comedor", 20000.0, categoria);
		
		client.post().uri(URL_BASE)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.producto.id").isNotEmpty()
		.jsonPath("$.producto.nombre").isEqualTo("Mesa Comedor");
	}
	
	@Test
	public void crearDosTest() {
		
		Categoria categoria = service.findCategoriaByNombre("Celulares").block();
		
		Producto producto = new Producto("Mesa Comedor", 20000.0, categoria);
		
		client.post().uri(URL_BASE)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		.consumeWith(response ->{
			//Se obtiene el producto del map
			Object o = response.getResponseBody().get("producto");
			//Se convierte el object a product
			Producto p = new ObjectMapper().convertValue(o, Producto.class);
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getNombre()).isEqualTo("Mesa Comedor");
			Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Celulares");
		});
	}
	
	@Test
	public void editarTest() {
		Producto producto = service.findByNombre("Impresora Epson").block();
		
		Categoria categoria = service.findCategoriaByNombre("Computacion").block();
		
		Producto productoEditado = new Producto("Impresora Epson", 8000.0, categoria);
		productoEditado.setId(producto.getId());
		
		client.put().uri(URL_BASE+"/{id}", Collections.singletonMap("id", producto.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(productoEditado), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Impresora Epson");
	}
	
	@Test
	public void eliminar() {
		Producto producto = service.findByNombre("Pantalla NOC").block();
		
		client.delete()
		.uri(URL_BASE+"/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody()
		.isEmpty();
		
		client.get()
		.uri(URL_BASE+"/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody()
		.isEmpty();
	}

}
