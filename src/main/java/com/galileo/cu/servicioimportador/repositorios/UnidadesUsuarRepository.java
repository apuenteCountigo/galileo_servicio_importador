package com.galileo.cu.servicioimportador.repositorios;

import com.galileo.cu.commons.models.UnidadesUsuarios;
import com.galileo.cu.commons.models.Usuarios;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(exported = false)
public interface UnidadesUsuarRepository extends CrudRepository<UnidadesUsuarios, Long> {

    UnidadesUsuarios findByUsuario(Usuarios usuario);
}
