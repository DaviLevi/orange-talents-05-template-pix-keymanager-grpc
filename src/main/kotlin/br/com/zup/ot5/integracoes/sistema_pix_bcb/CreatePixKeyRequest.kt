package br.com.zup.ot5.integracoes.sistema_pix_bcb

import br.com.zup.ot5.chave_pix.ChavePix
import br.com.zup.ot5.chave_pix.TipoChave
import br.com.zup.ot5.compartilhado.model.Conta
import br.com.zup.ot5.compartilhado.model.Instituicoes
import br.com.zup.ot5.compartilhado.model.TipoConta

data class CreatePixKeyRequest(
    val keyType : TipoChaveBcb,
    val key: String,
    val bankAccount: ContaBcbRequest,
    val owner: TitularBcbRequest
){

    companion object {
        fun of(chavePix : ChavePix) : CreatePixKeyRequest{
            return CreatePixKeyRequest(
                keyType = chavePix.tipoChave.paraTipoChaveBcb(),
                key = chavePix.chave,
                bankAccount = ContaBcbRequest.of(chavePix.conta),
                owner = TitularBcbRequest.of(chavePix.conta)
            )
        }
    }
}

data class TitularBcbRequest(
    val type: TipoPessoaBcb,
    val name: String,
    val taxIdNumber: String
){
    companion object {
        fun of(conta : Conta) : TitularBcbRequest{
            return TitularBcbRequest(
                type = TipoPessoaBcb.NATURAL_PERSON,
                name = conta.titularConta.nomeTitular,
                taxIdNumber = conta.titularConta.cpfTitular
            )
        }
    }
}

data class ContaBcbRequest(
    val participant : String,
    val branch: String,
    val accountNumber: String,
    val accountType: TipoContaBcb
){
    companion object {
        fun of(conta : Conta) : ContaBcbRequest{
            return ContaBcbRequest(
                participant = Instituicoes.ispbDe(conta.instituicao).toString(),
                branch = conta.agencia,
                accountNumber = conta.numero,
                accountType = conta.tipoConta.paraTipoContaBcb()
            )
        }
    }
}

enum class TipoContaBcb{
    CACC {
        override fun paraTipoConta(): TipoConta {
            return TipoConta.CONTA_CORRENTE
        }
    }, SVGS {
        override fun paraTipoConta(): TipoConta {
            return TipoConta.CONTA_POUPANCA
        }
    };

    companion object{
        fun por(tipoConta: TipoConta): TipoContaBcb {
            return when(tipoConta){
                TipoConta.CONTA_CORRENTE -> CACC
                TipoConta.CONTA_POUPANCA -> SVGS
            }
        }
    }

    abstract fun paraTipoConta() : TipoConta
}

enum class TipoPessoaBcb{
    NATURAL_PERSON, LEGAL_PERSON
}

enum class TipoChaveBcb{
    CPF {
        override fun paraTipoChave(): TipoChave {
            return TipoChave.CPF
        }
    }, CNPJ {
        override fun paraTipoChave(): TipoChave {
            TODO("Not yet implemented")
        }
    }, PHONE {
        override fun paraTipoChave(): TipoChave {
            return TipoChave.TELEFONE_CELULAR
        }
    }, EMAIL {
        override fun paraTipoChave(): TipoChave {
            return TipoChave.EMAIL
        }
    }, RANDOM {
        override fun paraTipoChave(): TipoChave {
            return TipoChave.ALEATORIA
        }
    };

    abstract fun paraTipoChave() : TipoChave
}