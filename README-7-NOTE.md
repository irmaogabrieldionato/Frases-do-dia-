# 🎵 7-Note Musical Game

Jogo musical em **português**, com **quatro modos**, sintetizador de áudio integrado, interface para celular e computador, progresso salvo e funcionamento offline após a primeira visita em HTTPS.

**Código:** branch [7-note-musical-game](https://github.com/irmaogabrieldionato/Frases-do-dia-/tree/7-note-musical-game), isolada dos outros projetos e da branch principal do repositório.

## Funcionalidades

### 1. Memória — jogo principal

Escute a sequência e reproduza a ordem pressionando uma das sete teclas (ou **1 a 7** no computador). Cada fase tem exatamente uma nota a mais que a anterior. O jogador só avança quando reproduz a fase inteira sem erros.

| Fase | Sequência |
| --- | --- |
| 1 | Mi |
| 2 | Mi, Dó |
| 3 | Mi, Dó, Sol |
| 4 | Mi, Dó, Sol, Dó |
| 5 | Mi, Dó, Sol, Dó, Dó |
| 6 | Mi, Dó, Sol, Dó, Dó, Ré |
| 7 | Mi, Dó, Sol, Dó, Dó, Ré, Si |

A partir da fase 8, acrescenta-se **uma nota no final**, entre Dó, Ré, Mi, Fá, Sol, Lá e Si. A sequência é determinística: não muda ao repetir a mesma fase. O limite técnico atual é a fase 10.000.

### 2. Ouvido — 10 perguntas por treino

Ouça uma nota sem saber qual tecla a produziu. Selecione a resposta no teclado e receba feedback. O modo guarda seu melhor resultado (0–10), estatísticas de acertos e pontos. Notas consecutivas nunca são iguais.

### 3. Livre — teclado musical

Explore as sete notas sem fase, tempo limite ou derrotas. Inclui demonstração da escala.

### 4. Compositor — até 64 notas

Toque notas para acrescentá-las a uma melodia, reproduza, desfaça a última nota ou apague a composição. As notas são salvas no navegador, sem precisar criar uma conta.

## Estatísticas e conquistas

- Fase atual, melhor fase e vitórias acumuladas;
- Derrotas, pontuação, vitórias consecutivas e melhor desempenho no treino de ouvido;
- Cinco conquistas desbloqueáveis.

## Publicidade: a cada duas derrotas

A regra é **uma tentativa de anúncio** na 2ª, 4ª, 6ª, 8ª... derrotas **acumuladas**. O contador não zera quando o jogador volta à fase 1. Errar uma resposta no treino de ouvido também conta como uma derrota. Os modos **Livre** e **Compositor** não geram derrotas.

O jogo possui um adaptador para integrar um provedor de intersticiais, porém **não inclui publicidade real, rastreamento, ad units ou SDK de anúncios**. Sem provedor configurado, aparece uma tela identificada como **DEMONSTRAÇÃO**, não um anúncio comercial.

Integração esperada:

~~~js
window.SevenNoteAds = {
  async showInterstitial({ placement, losses, level }) {
    // Integrar aqui o SDK do provedor. Retorne true somente
    // após um anúncio real ter sido exibido e encerrado.
    return false;
  }
};
~~~

Este adaptador deve ser carregado **antes** de app.js. A integração definitiva exige aprovação da rede, identificação das unidades, políticas de privacidade, controles por faixa etária e consentimento onde aplicável.

## Ajustes

- Volume de 0% a 100%;
- Velocidade lenta, normal e rápida;
- Sons suave, sino e retrô;
- Ajuda visual opcional com nome da nota durante a sequência;
- Limpeza completa de dados e reinício somente da fase atual (ações distintas).

## Instalação e offline

O aplicativo é uma **PWA** (Progressive Web App). Possui manifest, ícone e service worker. Em um navegador compatível, hospede em **HTTPS** ou localhost e use o comando de instalação disponibilizado pelo navegador. O jogo guarda os arquivos principais em cache e pode ser reaberto sem internet após a primeira visita concluída.

O **GitHub não fornece um endereço jogável automaticamente ao criar uma branch**. Para publicar sem mudar o código, use GitHub Pages, um serviço estático ou outro host HTTP(S). Se escolher GitHub Pages neste mesmo repositório, verifique antes se já existe outra página publicada para não substituí-la.

## Desenvolvimento e testes

Requer Node.js 18+ para testes de regras:

~~~sh
npm test
~~~

Para testar a interface no Chromium com Playwright:

~~~sh
npm install
npx playwright install chromium
npm run test:browser
~~~

GitHub Actions executa as duas suítes em cada push: regras de negócio e fluxo real no navegador, incluindo vitórias, anúncios, composição, configurações e offline.

### Arquivos

- index.html — interface em português e acessibilidade;
- styles.css — estilos responsivos e aparência das sete notas;
- game-core.js — motor de regras determinísticas;
- app.js — áudio Web Audio API, modos, armazenamento, conquistas e publicidade;
- manifest.webmanifest, icon.svg, sw.js — instalação e offline;
- tests/game.test.cjs, tests/browser-smoke.cjs — testes automatizados.

O jogo funciona sem cadastro, sem API externa, sem músicas ou bibliotecas de terceiros durante a execução. O armazenamento local pertence ao navegador do jogador; o progresso não é sincronizado entre aparelhos.
