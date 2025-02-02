package br.com.zup.ot5.integracoes.sistema_erp_itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${integracoes.sistemaErpItau.url}")
interface SistemaERPItauClient {

    @Get(uri = "/api/v1/clientes/{clienteId}/contas")
    fun buscaContaPorTipo(@PathVariable clienteId: String, @QueryValue tipo: String) : HttpResponse<ContaResponse>
}