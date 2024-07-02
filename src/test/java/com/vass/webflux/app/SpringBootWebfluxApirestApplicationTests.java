package com.vass.webflux.app;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

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
	

	@Test
	void listarTest() {
		client.get()
		.uri("/api/v2/productos")
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
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
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
		
		client.post().uri("/api/v2/productos")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Mesa Comedor");
	}
	
	@Test
	public void crearDosTest() {
		
		Categoria categoria = service.findCategoriaByNombre("Celulares").block();
		
		Producto producto = new Producto("Mesa Comedor", 20000.0, categoria);
		
		client.post().uri("/api/v2/productos")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response ->{
			Producto p = response.getResponseBody();
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
		
		client.put().uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
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
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody()
		.isEmpty();
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody()
		.isEmpty();
	}

}
