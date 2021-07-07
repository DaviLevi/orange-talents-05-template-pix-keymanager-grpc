package br.com.zup.ot5.chave_pix.lista_chave

import br.com.zup.ot5.compartilhado.validadores.UUIDValido
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ListaChavePixRequestValidavel(
    @field:NotBlank @field:UUIDValido val idTitular: String
)