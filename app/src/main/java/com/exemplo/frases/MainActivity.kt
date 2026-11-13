package com.exemplo.frases

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

data class Citacao(val texto: String, val filosofo: String, val obra: String)

class MainActivity : Activity() {
    private val citacoes = listOf(
        Citacao("As pessoas não são perturbadas pelas coisas, mas pelas opiniões que têm sobre elas.", "Epicteto", "Manual, 5"),
        Citacao("Uma vida sem exame não vale a pena ser vivida.", "Sócrates", "Em Platão, Apologia de Sócrates, 38a"),
        Citacao("Penso, logo existo.", "René Descartes", "Discurso do Método, parte IV"),
        Citacao("A natureza não faz nada em vão.", "Aristóteles", "Política, livro I"),
        Citacao("Não é que tenhamos pouco tempo, mas perdemos muito dele.", "Sêneca", "Sobre a Brevidade da Vida, 1"),
        Citacao("O homem nasce livre, e por toda parte encontra-se acorrentado.", "Jean-Jacques Rousseau", "Do Contrato Social, livro I, capítulo 1"),
        Citacao("O coração tem razões que a razão não conhece.", "Blaise Pascal", "Pensamentos"),
        Citacao("O universo é mudança; a vida é opinião.", "Marco Aurélio", "Meditações, livro IV"),
        Citacao("Estudar sem refletir é inútil; refletir sem estudar é perigoso.", "Confúcio", "Analectos, 2.15"),
        Citacao("Nunca se é jovem demais nem velho demais para filosofar.", "Epicuro", "Carta a Meneceu")
    )

    private lateinit var textoFrase: TextView
    private lateinit var textoAutor: TextView
    private lateinit var textoObra: TextView
    private var indiceAtual = -1

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fundo = Color.rgb(249, 247, 242)
        val tinta = Color.rgb(46, 48, 58)
        val destaque = Color.rgb(75, 59, 134)

        val coluna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(26), dp(32), dp(26), dp(32))
            setBackgroundColor(fundo)
        }

        val titulo = TextView(this).apply {
            text = "Frase de Filósofos"
            textSize = 29f
            setTypeface(null, Typeface.BOLD)
            setTextColor(destaque)
            gravity = Gravity.CENTER
        }

        val subtitulo = TextView(this).apply {
            text = "Uma reflexão diferente a cada abertura"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(35))
        }

        textoFrase = TextView(this).apply {
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(tinta)
            setPadding(0, dp(15), 0, dp(22))
        }

        textoAutor = TextView(this).apply {
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(destaque)
            setPadding(0, 0, 0, dp(6))
        }

        textoObra = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, dp(30))
        }

        val botaoOutra = Button(this).apply {
            text = "Outra frase"
            setOnClickListener { mostrarNovaFrase() }
        }

        val botaoCompartilhar = Button(this).apply {
            text = "Compartilhar"
            setOnClickListener {
                if (indiceAtual >= 0) {
                    val citacao = citacoes[indiceAtual]
                    val mensagem = "“${citacao.texto}”\n— ${citacao.filosofo}\n${citacao.obra}"
                    val envio = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, mensagem)
                    }
                    startActivity(Intent.createChooser(envio, "Compartilhar citação"))
                }
            }
        }

        val aviso = TextView(this).apply {
            text = "Textos em tradução livre de obras clássicas."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(0, dp(26), 0, 0)
        }

        coluna.addView(titulo)
        coluna.addView(subtitulo)
        coluna.addView(textoFrase)
        coluna.addView(textoAutor)
        coluna.addView(textoObra)
        coluna.addView(botaoOutra)
        coluna.addView(botaoCompartilhar)
        coluna.addView(aviso)

        val rolagem = ScrollView(this).apply {
            isFillViewport = true
            addView(coluna, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
        setContentView(rolagem)
        mostrarNovaFrase()
    }

    private fun mostrarNovaFrase() {
        val preferencias = getSharedPreferences("frases_filosofos", MODE_PRIVATE)
        val indiceAnterior = preferencias.getInt("ultima_frase", -1)
        val opcoes = citacoes.indices.filter { it != indiceAnterior }
        val novoIndice = opcoes.random()

        indiceAtual = novoIndice
        val citacao = citacoes[novoIndice]
        textoFrase.text = "“${citacao.texto}”"
        textoAutor.text = "— ${citacao.filosofo}"
        textoObra.text = citacao.obra

        preferencias.edit().putInt("ultima_frase", novoIndice).apply()
    }
}
