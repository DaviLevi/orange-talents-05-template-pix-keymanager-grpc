package br.com.zup.ot5.chave_pix.lista_chave

import br.com.zup.ot5.*
import br.com.zup.ot5.chave_pix.ChavePix
import br.com.zup.ot5.chave_pix.GerenciadorChavePix
import br.com.zup.ot5.compartilhado.interceptors.ErrorHandler
import io.grpc.stub.StreamObserver
import java.util.stream.Collectors
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListaPixGrpcEndpoint(
    private val gerenciadorChavePix: GerenciadorChavePix
) : KeyManagerListaServiceGrpc.KeyManagerListaServiceImplBase(){


    override fun listaChavePix(
        request: ListaChavePixRequest?,
        responseObserver: StreamObserver<ListaChavePixResponse>?
    ) {
        val requestValidavel = request?.paraListaChavePixRequestValidavel()

        val chaves = gerenciadorChavePix.listarPix(requestValidavel!!)

        responseObserver?.onNext(
            ListaChavePixResponse.newBuilder().addAllChaves(
                chaves.stream().map { c -> ResumoChavePix.getDefaultInstance().de(c) }.collect(Collectors.toList())
            ).build()
        )
        responseObserver?.onCompleted()
    }


    internal fun ListaChavePixRequest.paraListaChavePixRequestValidavel() : ListaChavePixRequestValidavel{
        return ListaChavePixRequestValidavel(this.idTitular)
    }

    internal fun ResumoChavePix.de(chavePix: ChavePix) : ResumoChavePix{
        return ResumoChavePix.newBuilder()
            .setPixId(chavePix.id.toString())
            .setIdTitular(chavePix.conta.titularConta.idTitular.toString())
            .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
            .setValorChave(chavePix.chave)
            .setTipoConta(TipoConta.valueOf(chavePix.conta.tipoConta.name))
            .setCriadaEm(chavePix.criadaEm.toString())
            .build()
    }
}