package br.com.zup.ot5.chave_pix

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import org.hibernate.annotations.Parameter
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID>{

    fun existsByChave(chave: String) : Boolean

    fun findByChave(chave: String) : Optional<ChavePix>

    @Query("SELECT c FROM ChavePix c WHERE c.conta.titularConta.idTitular = :idTitular")
    fun listarChavesPorTitular(idTitular: UUID) : List<ChavePix>
}