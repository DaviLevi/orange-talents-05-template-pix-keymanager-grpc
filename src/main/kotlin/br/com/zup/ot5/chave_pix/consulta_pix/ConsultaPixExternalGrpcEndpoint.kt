package br.com.zup.ot5.chave_pix.consulta_pix

import br.com.zup.ot5.ConsultaChavePixExternalRequest
import br.com.zup.ot5.ConsultaChavePixExternalResponse
import br.com.zup.ot5.ExternalServicesConsultaServiceGrpc
import br.com.zup.ot5.TipoChave
import br.com.zup.ot5.chave_pix.ChavePixRepository
import br.com.zup.ot5.chave_pix.GerenciadorChavePix
import br.com.zup.ot5.compartilhado.interceptors.ErrorHandler
import br.com.zup.ot5.compartilhado.model.Instituicoes
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ConsultaPixExternalGrpcEndpoint(
    private val chavePixRepository: ChavePixRepository,
    private val gerenciadorChavePix: GerenciadorChavePix
) : ExternalServicesConsultaServiceGrpc.ExternalServicesConsultaServiceImplBase(){

    override fun consultaChavePix(
        request: ConsultaChavePixExternalRequest,
        responseObserver: StreamObserver<ConsultaChavePixExternalResponse>?
    ) {

        val requestValidavel = request.paraConsultaChavePixExternalRequestValidavel()

        val pixConsultada = gerenciadorChavePix.consultaPixExternal(requestValidavel)

        println(pixConsultada)

        responseObserver?.onNext(
            ConsultaChavePixExternalResponse.getDefaultInstance().por(pixConsultada)
        )
        responseObserver?.onCompleted()


    }

    fun ConsultaChavePixExternalRequest.paraConsultaChavePixExternalRequestValidavel() : ConsultaChavePixExternalRequestValidavel{
        return ConsultaChavePixExternalRequestValidavel(
            chave = this.valorChave
        )
    }

    fun ConsultaChavePixExternalResponse.por(detalheChavePix: DetalheChavePix) : ConsultaChavePixExternalResponse{
        return ConsultaChavePixExternalResponse.newBuilder()
            .setTipoChave(TipoChave.valueOf(detalheChavePix.tipoChave.name))
            .setValorChave(detalheChavePix.chave)
            .setNomeTitular(detalheChavePix.titular.nome)
            .setCpfTitular(detalheChavePix.titular.cpf)
            .setInstituicaoConta(detalheChavePix.conta.instituicao)
            .setIspb(Instituicoes.ispbDe(detalheChavePix.conta.instituicao))
            .setAgencia(detalheChavePix.conta.agencia)
            .setNumero(detalheChavePix.conta.numero)
            .setTipoConta(br.com.zup.ot5.TipoConta.valueOf(detalheChavePix.conta.tipoConta.name))
            .setCriadaEm(detalheChavePix.criadaEm.toString()).run {
                if(detalheChavePix.idTitular != null) this.setIdTitular(detalheChavePix.idTitular)
                if(detalheChavePix.pixId != null) this.setPixId(detalheChavePix.pixId)
                this.build()
            }

    }

}