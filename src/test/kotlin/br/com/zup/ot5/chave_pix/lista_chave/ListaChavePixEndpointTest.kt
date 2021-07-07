package br.com.zup.ot5.chave_pix.lista_chave

import br.com.zup.ot5.KeyManagerListaServiceGrpc
import br.com.zup.ot5.ListaChavePixRequest
import br.com.zup.ot5.RegistraChavePixRequest
import br.com.zup.ot5.TipoConta
import br.com.zup.ot5.chave_pix.ChavePix
import br.com.zup.ot5.chave_pix.ChavePixRepository
import br.com.zup.ot5.chave_pix.TipoChave
import br.com.zup.ot5.integracoes.sistema_erp_itau.ContaResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.InstituicaoResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.TipoContaResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.TitularResponse
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
class ListaChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clientePixGrpc: KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub
) {

    companion object {
        val ID_TITULAR: UUID = UUID.randomUUID()
        val ID_TITULAR_INVALIDO: String = "123ass456-asbfuf"
        val ID_TITULAR_VAZIO: String = "  "
        const val CPF_VALIDO = "63657520325"
        const val EMAIL_VALIDO = "rafael.ponte@zup.com.br"
        const val INSTITUICAO_VALIDA = "ITAÚ UNIBANCO S.A."
        const val ISBP = "60701190"
        const val NOME_TITULAR = "Rafael Ponte"
        const val AGENCIA = "1218"
        const val NUMERO = "291900"
    }

    @BeforeEach
    fun limpaSujeiraDoBanco(){
        chavePixRepository.deleteAll()
    }

    @Test
    fun `Deve consultar as chaves pertencentes ao mesmo usuario quando o payload for valido`(){
        // cenario
        val primeiraChavePixSalva = chavePixRepository.save(
            ChavePix(
                tipoChave = TipoChave.CPF,
                chave = CPF_VALIDO,
                conta = dadosDaContaAssociadaResponse(ID_TITULAR).paraConta()
            )
        )

        val segundaChavePixSalva = chavePixRepository.save(
            ChavePix(
                tipoChave = TipoChave.EMAIL,
                chave = EMAIL_VALIDO,
                conta = dadosDaContaAssociadaResponse(ID_TITULAR).paraConta()
            )
        )

        // exec
        val chaves = clientePixGrpc.listaChavePix(
            ListaChavePixRequest.newBuilder()
                .setIdTitular(ID_TITULAR.toString())
                .build()
        )

        // validacao
        Assertions.assertNotNull(chaves)
        Assertions.assertEquals(2, chaves.chavesList.size)
        Assertions.assertEquals(2, chavePixRepository.count())
    }

    @Test
    fun `Deve mostrar uma lista vazia quando o usuario nao possuir nenhuma chave`(){
        // cenario


        // exec
        val chaves = clientePixGrpc.listaChavePix(
            ListaChavePixRequest.newBuilder()
                .setIdTitular(ID_TITULAR.toString())
                .build()
        )

        // validacao
        Assertions.assertNotNull(chaves)
        Assertions.assertEquals(0, chaves.chavesList.size)
        Assertions.assertEquals(0, chavePixRepository.count())
    }

    @Test
    fun `Nao deve consultar as chaves quando o payload apresentar dados invalidos (INVALID ARGUMENT)`(){
        // cenario

        // exec
        val excecaoIdTitularVazio = Assertions.assertThrows(StatusRuntimeException::class.java) {
            val chaves = clientePixGrpc.listaChavePix(
                ListaChavePixRequest.newBuilder()
                    .setIdTitular(ID_TITULAR_VAZIO.toString())
                    .build()
            )
        }

        val excecaoIdTitularInvalido = Assertions.assertThrows(StatusRuntimeException::class.java) {
            val chaves = clientePixGrpc.listaChavePix(
                ListaChavePixRequest.newBuilder()
                    .setIdTitular(ID_TITULAR_INVALIDO.toString())
                    .build()
            )
        }


        // validacao
        Assertions.assertNotNull(excecaoIdTitularVazio)
        Assertions.assertNotNull(excecaoIdTitularInvalido)
    }


    @Factory
    class Clients {

        @Singleton
        fun chavePixGrpcClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub{
            return KeyManagerListaServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chavePixValida() : RegistraChavePixRequest {
        return RegistraChavePixRequest.newBuilder()
            .setIdTitular(ID_TITULAR.toString())
            .setValorChave(CPF_VALIDO)
            .setTipoChave(br.com.zup.ot5.TipoChave.CPF)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()
    }

    private fun chavePixTotalmenteInvalida() : RegistraChavePixRequest {
        return RegistraChavePixRequest.newBuilder().build()
    }

    private fun dadosDaContaAssociadaResponse(idTitular: UUID) : ContaResponse {
        return ContaResponse(
            tipo = TipoContaResponse.CONTA_CORRENTE,
            instituicao = instituicaoContaResponse(),
            agencia = AGENCIA,
            numero = NUMERO,
            titular = titularContaResponse(idTitular)
        )
    }

    private fun instituicaoContaResponse() : InstituicaoResponse {
        return InstituicaoResponse(
            nome = INSTITUICAO_VALIDA,
            ispb = ISBP
        )
    }

    private fun titularContaResponse(idTitular: UUID) : TitularResponse {
        return TitularResponse(
            id = idTitular,
            nome = NOME_TITULAR,
            cpf = CPF_VALIDO
        )
    }

}