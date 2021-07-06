package br.com.zup.ot5.chave_pix.consulta_pix

import br.com.zup.ot5.compartilhado.validadores.UUIDValido
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ConsultaChavePixRequestValidavel(
    @field:NotBlank @field:UUIDValido val idTitular: String,
    @field:NotBlank @field:UUIDValido val pixId: String
)