package br.com.zup.ot5.compartilhado.model

class Instituicoes {

    companion object{
        private val ispbs = mapOf<String, String>(
            "ITAÚ UNIBANCO S.A." to "60701190"
        )

        private val instituicoes = mapOf<String, String>(
            "60701190" to "ITAÚ UNIBANCO S.A."
        )

        fun ispbDe(instituicao: String): String {
            return ispbs[instituicao] ?: throw IllegalStateException("Instituicao desconhecida")
        }

        fun instituicaoPorIspb(ispb: String): String {
            return instituicoes[ispb] ?: throw IllegalStateException("Ispb desconhecido")
        }
    }
}