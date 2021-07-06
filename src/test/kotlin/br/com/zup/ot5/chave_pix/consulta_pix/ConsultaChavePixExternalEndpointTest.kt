package br.com.zup.ot5.chave_pix.consulta_pix

import br.com.zup.ot5.ExternalServicesConsultaServiceGrpc
import br.com.zup.ot5.RegistraChavePixRequest
import br.com.zup.ot5.TipoConta
import br.com.zup.ot5.chave_pix.ChavePixRepository
import br.com.zup.ot5.chave_pix.cria_chave_pix.CriaChavePixEndpointTest
import br.com.zup.ot5.integracoes.sistema_erp_itau.ContaResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.InstituicaoResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.TipoContaResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.TitularResponse
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import javax.inject.Singleton

@MicronautTest(transactional = false)
class ConsultaChavePixExternalEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clientePixGrpc: ExternalServicesConsultaServiceGrpc.ExternalServicesConsultaServiceBlockingStub
){




    @Factory
    class Clients {

        @Singleton
        fun chavePixGrpcClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : ExternalServicesConsultaServiceGrpc.ExternalServicesConsultaServiceBlockingStub{
            return ExternalServicesConsultaServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePixValida() : RegistraChavePixRequest {
        return RegistraChavePixRequest.newBuilder()
            .setIdTitular(CriaChavePixEndpointTest.CLIENTE_ID.toString())
            .setValorChave(CriaChavePixEndpointTest.CPF_VALIDO)
            .setTipoChave(br.com.zup.ot5.TipoChave.CPF)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()
    }

    private fun chavePixTotalmenteInvalida() : RegistraChavePixRequest {
        return RegistraChavePixRequest.newBuilder().build()
    }

    private fun dadosDaContaAssociadaResponse() : ContaResponse {
        return ContaResponse(
            tipo = TipoContaResponse.CONTA_CORRENTE,
            instituicao = instituicaoContaResponse(),
            agencia = CriaChavePixEndpointTest.AGENCIA,
            numero = CriaChavePixEndpointTest.NUMERO,
            titular = titularContaResponse()
        )
    }

    private fun instituicaoContaResponse() : InstituicaoResponse {
        return InstituicaoResponse(
            nome = CriaChavePixEndpointTest.INSTITUICAO_VALIDA,
            ispb = CriaChavePixEndpointTest.ISBP
        )
    }

    private fun titularContaResponse() : TitularResponse {
        return TitularResponse(
            id = CriaChavePixEndpointTest.CLIENTE_ID,
            nome = CriaChavePixEndpointTest.NOME_TITULAR,
            cpf = CriaChavePixEndpointTest.CPF_VALIDO
        )
    }
}