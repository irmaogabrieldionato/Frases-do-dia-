package com.exemplo.frases

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val frases = listOf(
        "Acredite no seu potencial!",
        "Cada dia é uma nova oportunidade.",
        "Comece pequeno, sonhe grande.",
        "A persistência transforma sonhos.",
        "Você é capaz de conquistar mais!"
    )
    private var indice = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tela = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(245, 242, 255))
        }
        val titulo = TextView(this).apply {
            text = "Frases do Dia"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(90, 45, 170))
        }
        val frase = TextView(this).apply {
            text = frases[indice]
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 65, 0, 65)
        }
        val proxima = Button(this).apply {
            text = "Nova frase"
            setOnClickListener {
                indice = (indice + 1) % frases.size
                frase.text = frases[indice]
            }
        }
        val compartilhar = Button(this).apply {
            text = "Compartilhar"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, frases[indice])
                }
                startActivity(Intent.createChooser(intent, "Compartilhar frase"))
            }
        }
        tela.addView(titulo)
        tela.addView(frase)
        tela.addView(proxima)
        tela.addView(compartilhar)
        setContentView(tela)
    }
}
