package br.com.zup.ot5.integracoes.sistema_pix_bcb

import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType : TipoChaveBcb,
    val key: String,
    val bankAccount: ContaBcbResponse,
    val owner: TitularBcbResponse,
    val createdAt: LocalDateTime
)