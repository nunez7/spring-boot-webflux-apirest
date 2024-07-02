package com.vass.webflux.app.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.vass.webflux.app.models.documents.Categoria;

import reactor.core.publisher.Mono;


public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String>{
	
	public Mono<Categoria> findByNombre(String categoria);

}
