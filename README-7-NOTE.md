# 🎵 7-Note Musical Game

Jogo simples de **memória auditiva** que usa as sete notas naturais (**Dó, Ré, Mi, Fá, Sol, Lá e Si**) e funciona no navegador de celular ou computador. Não exige frameworks, instalação de pacotes ou arquivos de som.

## Como jogar

1. Abra [index.html](index.html) no navegador (ou publique esta branch no GitHub Pages).
2. Pressione **Ouvir sequência** para escutar as notas da fase.
3. Depois do áudio, repita a sequência usando os sete botões coloridos ou as teclas numéricas **1–7**.
4. Ao acertar todas as notas, uma nova fase é desbloqueada com mais uma nota.
5. Ao errar, a derrota é contabilizada e a mesma fase pode ser repetida.

O **Modo livre** permite explorar o teclado sem registrar derrota. O progresso e o recorde ficam salvos no armazenamento local do navegador; não há cadastro ou transmissão de dados pessoais.

## Progressão inicial obrigatória

| Fase | Sequência | Total |
| --- | --- | --- |
| 1 | Mi | 1 |
| 2 | Mi – Dó | 2 |
| 3 | Mi – Dó – Sol | 3 |
| 4 | Mi – Dó – Sol – Dó | 4 |
| 5 | Mi – Dó – Sol – Dó – Dó | 5 |
| 6 | Mi – Dó – Sol – Dó – Dó – Ré | 6 |
| 7 | Mi – Dó – Sol – Dó – Dó – Ré – Si | 7 |

**Da fase 8 em diante:** uma nova nota é adicionada ao final da sequência anterior, escolhida por um gerador determinístico entre as sete notas. O prefixo das sete primeiras notas nunca muda. Cada fase sempre tem um número de notas igual ao número da fase.

## Anúncios: a cada duas derrotas

O contador de derrotas é persistente (não zera quando o jogador volta à fase 1). Nas derrotas **2, 4, 6, 8, ...**, o jogo solicita um anúncio intersticial.

A estrutura para ligar uma rede de anúncios é:

~~~js
window.SevenNoteAds = {
  async showInterstitial({ placement, losses, level }) {
    // Chame aqui o SDK aprovado do provedor e aguarde o fim do anúncio.
    // Retorne true se um anúncio real foi exibido.
    // Retorne false se não houver anúncio disponível.
    return false;
  }
};
~~~

**Importante:** até haver provedor e identificadores publicitários configurados, o jogo mostra **somente uma tela identificada como demonstração**, e não um anúncio pago. A integração real precisa respeitar as regras de consentimento, idade, privacidade e frequência da plataforma/rede escolhida. O código não gera receita sem essa integração.

O adaptador precisa ser carregado em uma tag script **antes** de app.js. Nenhuma dependência de terceiros é exigida para testar a mecânica.

## Testes

Requer apenas Node.js 18 ou superior:

~~~bash
npm test
~~~

A suíte cobre: identidade das sete notas, fases 1–7, crescimento da sequência, repetibilidade, acertos, erros, progressão, persistência de dados inválidos e a regra de anúncios a cada duas derrotas.

## Publicar na web

Os arquivos estão na branch **7-note-musical-game** do repositório [Frases-do-dia-](https://github.com/irmaogabrieldionato/Frases-do-dia-/tree/7-note-musical-game).

Para publicar, abra **Settings → Pages** no repositório, selecione **Deploy from a branch**, escolha a branch **7-note-musical-game** e o diretório **/(root)** e salve. O endereço exato será disponibilizado pelo próprio GitHub quando a publicação estiver pronta.

### Tecnologias

HTML + CSS + JavaScript nativo + Web Audio API; testes em Node.js. Interface em português brasileiro. O projeto fica em uma branch isolada, sem modificar a versão principal do repositório.
