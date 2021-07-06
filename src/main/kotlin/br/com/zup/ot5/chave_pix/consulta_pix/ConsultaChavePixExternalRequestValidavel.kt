package br.com.zup.ot5.chave_pix.consulta_pix

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class ConsultaChavePixExternalRequestValidavel(
    @field:NotBlank @field:Size(max = 77) val chave: String
)