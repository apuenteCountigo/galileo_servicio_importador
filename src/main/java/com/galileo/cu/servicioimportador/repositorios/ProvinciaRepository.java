package com.galileo.cu.servicioimportador.repositorios;

import com.galileo.cu.commons.models.Provincias;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ProvinciaRepository extends CrudRepository<Provincias, Long> {

    Provincias findProvinciasByDescripcion(String descripcion);
}
