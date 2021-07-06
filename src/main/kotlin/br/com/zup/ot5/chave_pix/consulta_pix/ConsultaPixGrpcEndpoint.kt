package br.com.zup.ot5.chave_pix.consulta_pix

import br.com.zup.ot5.ConsultaChavePixRequest
import br.com.zup.ot5.ConsultaChavePixResponse
import br.com.zup.ot5.KeyManagerConsultaServiceGrpc
import br.com.zup.ot5.TipoChave
import br.com.zup.ot5.chave_pix.ChavePixRepository
import br.com.zup.ot5.chave_pix.GerenciadorChavePix
import br.com.zup.ot5.compartilhado.interceptors.ErrorHandler
import br.com.zup.ot5.compartilhado.model.Instituicoes
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ConsultaPixGrpcEndpoint(
    private val chavePixRepository: ChavePixRepository,
    private val gerenciadorChavePix: GerenciadorChavePix
) : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceImplBase() {

    override fun consultaChavePix(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>?
    ) {

        val requestValidavel = request.paraConsultaChavePixRequestValidavel()

        val chaveEncontrada = gerenciadorChavePix.consultaPix(requestValidavel)

        responseObserver?.onNext(
            ConsultaChavePixResponse.getDefaultInstance().por(chaveEncontrada)
        )
        responseObserver?.onCompleted()

    }

    fun ConsultaChavePixRequest.paraConsultaChavePixRequestValidavel() : ConsultaChavePixRequestValidavel{
        return ConsultaChavePixRequestValidavel(
            idTitular = idTitular,
            pixId = pixId
        )
    }

    fun ConsultaChavePixResponse.por(detalheChavePix: DetalheChavePix) : ConsultaChavePixResponse{
        return ConsultaChavePixResponse.newBuilder()
            .setPixId(detalheChavePix.pixId)
            .setIdTitular(detalheChavePix.idTitular)
            .setTipoChave(TipoChave.valueOf(detalheChavePix.tipoChave.name))
            .setValorChave(detalheChavePix.chave)
            .setNomeTitular(detalheChavePix.titular.nome)
            .setCpfTitular(detalheChavePix.titular.cpf)
            .setInstituicaoConta(detalheChavePix.conta.instituicao)
            .setIspb(Instituicoes.ispbDe(detalheChavePix.conta.instituicao))
            .setAgencia(detalheChavePix.conta.agencia)
            .setNumero(detalheChavePix.conta.numero)
            .setTipoConta(br.com.zup.ot5.TipoConta.valueOf(detalheChavePix.conta.tipoConta.name))
            .setCriadaEm(detalheChavePix.criadaEm.toString())
            .build()
    }

}