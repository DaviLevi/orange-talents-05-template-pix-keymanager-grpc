package br.com.zup.ot5.chave_pix.consulta_pix

import br.com.zup.ot5.chave_pix.ChavePix
import br.com.zup.ot5.chave_pix.TipoChave
import br.com.zup.ot5.compartilhado.model.Instituicoes
import br.com.zup.ot5.compartilhado.model.TipoConta
import br.com.zup.ot5.integracoes.sistema_pix_bcb.PixKeyDetailsResponse
import java.time.LocalDateTime

data class DetalheChavePix(
    val pixId: String? = null,
    val idTitular: String? = null,
    val tipoChave: TipoChave,
    val chave: String,
    val titular: DetalheTitularConta,
    val conta: DetalheConta,
    val criadaEm: LocalDateTime
) {

    constructor(chavePix: ChavePix) : this(
        pixId = chavePix.id.toString(),
        idTitular = chavePix.conta.titularConta.idTitular.toString(),
        tipoChave = chavePix.tipoChave,
        chave = chavePix.chave,
        titular = DetalheTitularConta(chavePix.conta.titularConta.nomeTitular, chavePix.conta.titularConta.cpfTitular),
        conta = DetalheConta(chavePix.conta.instituicao, agencia = chavePix.conta.agencia, numero = chavePix.conta.numero, tipoConta = chavePix.conta.tipoConta),
        criadaEm = chavePix.criadaEm
    )

    constructor(pixKeyDetailsResponse: PixKeyDetailsResponse) : this(
        tipoChave = pixKeyDetailsResponse.keyType.paraTipoChave(),
        chave = pixKeyDetailsResponse.key,
        titular = DetalheTitularConta(pixKeyDetailsResponse.owner.name, pixKeyDetailsResponse.owner.taxIdNumber),
        conta = DetalheConta(Instituicoes.instituicaoPorIspb(pixKeyDetailsResponse.bankAccount.participant), agencia = pixKeyDetailsResponse.bankAccount.branch,
        numero = pixKeyDetailsResponse.bankAccount.accountNumber, tipoConta = pixKeyDetailsResponse.bankAccount.accountType.paraTipoConta()),
        criadaEm = pixKeyDetailsResponse.createdAt
    )
}

data class DetalheTitularConta(
    val nome: String,
    val cpf: String
)

data class DetalheConta(
    val instituicao: String,
    val agencia: String,
    val numero: String,
    val tipoConta: TipoConta
)