package br.com.zup.ot5.chave_pix.consulta_pix

import br.com.zup.ot5.ConsultaChavePixRequest
import br.com.zup.ot5.KeyManagerConsultaServiceGrpc
import br.com.zup.ot5.RegistraChavePixRequest
import br.com.zup.ot5.TipoConta
import br.com.zup.ot5.chave_pix.ChavePix
import br.com.zup.ot5.chave_pix.ChavePixRepository
import br.com.zup.ot5.chave_pix.TipoChave
import br.com.zup.ot5.integracoes.sistema_erp_itau.ContaResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.InstituicaoResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.TipoContaResponse
import br.com.zup.ot5.integracoes.sistema_erp_itau.TitularResponse
import br.com.zup.ot5.integracoes.sistema_pix_bcb.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class ConsultaChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clientePixGrpc: KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub
){

    @Inject
    lateinit var bcbClient: SistemaPixBcbClient

    companion object {
        val ID_TITULAR: UUID = UUID.randomUUID()
        val ID_TITULAR_DIVERGENTE = UUID.randomUUID()
        val PIX_ID_INEXISTENTE = UUID.randomUUID()
        const val CPF_VALIDO = "63657520325"
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
    fun `Deve consultar uma chave pix existente do mesmo titular`(){
        // cenario
        val chavePixSalva = chavePixRepository.save(
            ChavePix(
                tipoChave = TipoChave.CPF,
                chave = CPF_VALIDO,
                conta = dadosDaContaAssociadaResponse(ID_TITULAR).paraConta()
            )
        )

        Mockito.`when`(
            bcbClient.consultaPix(
                key = chavePixSalva.chave
            )
        ).thenReturn(
            HttpResponse.ok(
                PixKeyDetailsResponse(
                    keyType = chavePixSalva.tipoChave.paraTipoChaveBcb(),
                    key = chavePixSalva.chave,
                    bankAccount = ContaBcbResponse(
                        participant = chavePixSalva.conta.ispb,
                        branch = chavePixSalva.conta.agencia,
                        accountNumber = chavePixSalva.conta.numero,
                        accountType = chavePixSalva.conta.tipoConta.paraTipoContaBcb()
                    ),
                    owner = TitularBcbResponse(
                        name = chavePixSalva.conta.titularConta.nomeTitular,
                        taxIdNumber = chavePixSalva.conta.titularConta.cpfTitular,
                        type = TipoPessoaBcb.NATURAL_PERSON
                    ),
                    createdAt = chavePixSalva.criadaEm
        )))

        // exec
        val resposta = clientePixGrpc.consultaChavePix(
            ConsultaChavePixRequest.newBuilder()
                .setIdTitular(ID_TITULAR.toString())
                .setPixId(chavePixSalva.id.toString())
                .build()
        )

        Assertions.assertNotNull(resposta)
        Assertions.assertEquals(1, chavePixRepository.count())
    }

    @Test
    fun `Nao deve consultar uma chave pix existente de outro titular`(){
        // cenario
        val chavePixSalva = chavePixRepository.save(
            ChavePix(
                tipoChave = TipoChave.CPF,
                chave = CPF_VALIDO,
                conta = dadosDaContaAssociadaResponse(ID_TITULAR).paraConta()
            )
        )

        // exec
        val excecao = Assertions.assertThrows(StatusRuntimeException::class.java){
            clientePixGrpc.consultaChavePix(
                ConsultaChavePixRequest.newBuilder()
                    .setIdTitular(ID_TITULAR_DIVERGENTE.toString())
                    .setPixId(chavePixSalva.id.toString())
                    .build()
            )
        }

        Assertions.assertNotNull(excecao)
        Assertions.assertEquals(Status.PERMISSION_DENIED.code, excecao.status.code)
        Assertions.assertEquals(1, chavePixRepository.count())
    }

    @Test
    fun `Nao deve consultar uma chave pix inexistente`(){
        // cenario
        val chavePixSalva = chavePixRepository.save(
            ChavePix(
                tipoChave = TipoChave.CPF,
                chave = CPF_VALIDO,
                conta = dadosDaContaAssociadaResponse(ID_TITULAR).paraConta()
            )
        )

        // exec
        val excecao = Assertions.assertThrows(StatusRuntimeException::class.java){
            clientePixGrpc.consultaChavePix(
                ConsultaChavePixRequest.newBuilder()
                    .setIdTitular(ID_TITULAR.toString())
                    .setPixId(PIX_ID_INEXISTENTE.toString())
                    .build()
            )
        }

        Assertions.assertNotNull(excecao)
        Assertions.assertEquals(Status.NOT_FOUND.code, excecao.status.code)
        Assertions.assertEquals(1, chavePixRepository.count())
    }

    @Test
    fun `Nao deve consultar uma chave pix inexistente no BCB`(){
        // cenario
        val chavePixSalva = chavePixRepository.save(
            ChavePix(
                tipoChave = TipoChave.CPF,
                chave = CPF_VALIDO,
                conta = dadosDaContaAssociadaResponse(ID_TITULAR).paraConta()
            )
        )

        Mockito.`when`(
            bcbClient.consultaPix(
                key = chavePixSalva.chave
            )
        ).thenReturn(
            HttpResponse.notFound()
        )


        // exec
        val excecao = Assertions.assertThrows(StatusRuntimeException::class.java){
            clientePixGrpc.consultaChavePix(
                ConsultaChavePixRequest.newBuilder()
                    .setIdTitular(ID_TITULAR.toString())
                    .setPixId(chavePixSalva.id.toString())
                    .build()
            )
        }

        Assertions.assertNotNull(excecao)
        Assertions.assertEquals(Status.FAILED_PRECONDITION.code, excecao.status.code)
        Assertions.assertEquals(1, chavePixRepository.count())
    }

    @MockBean(SistemaPixBcbClient::class)
    fun sistemaPixBcbClient() : SistemaPixBcbClient{
        return Mockito.mock(SistemaPixBcbClient::class.java)
    }

    @Factory
    class Clients {

        @Singleton
        fun chavePixGrpcClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub{
            return KeyManagerConsultaServiceGrpc.newBlockingStub(channel)
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
            numero =NUMERO,
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