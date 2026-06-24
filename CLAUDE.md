# TargetCompare — Guia para o Claude Code

## Encoding

- **Todos os arquivos Java devem ser salvos em UTF-8.**
- Use caracteres portugueses diretamente (ex.: `ã`, `ç`, `é`, `ô`). **Nunca use escapes Unicode** (`ã`, `ç` etc.) em strings ou comentários.
- O projeto Eclipse deve ter "Text file encoding" configurado como **UTF-8** em Project → Properties → Resource.
- Ao compilar via linha de comando, use sempre a flag `-encoding UTF-8`.

## Idioma

- Comentários, mensagens de UI e Javadoc: **Português do Brasil** com acentuação correta.
- Identificadores (variáveis, métodos, classes): **inglês** ou manter os já existentes em português para não quebrar compatibilidade.

## Estilo de código

- Java 8+; sem raw types (use `JComboBox<String>`, `List<Gene>` etc.).
- `StringBuilder` em vez de concatenação em loops.
- `try-with-resources` para qualquer recurso `Closeable`.
- Campos de instância nunca devem ser `static` por conveniência.
- Algoritmos de busca/comparação: preferir estruturas de dados adequadas (`Map`, `Set`) em vez de loops aninhados.
