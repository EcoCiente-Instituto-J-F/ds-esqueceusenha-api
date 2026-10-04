package br.com.ecociente.senha.dataprovaider.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import br.com.ecociente.senha.dataprovaider.entity.UsuarioEntity;
import jakarta.persistence.LockModeType;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity,Integer> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UsuarioEntity> findByEmailUsuarioIgnoreCase(String email);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UsuarioEntity> findById(Integer id);
}
