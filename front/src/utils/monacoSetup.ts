import type * as monaco from "monaco-editor";

let configured = false;

const KOTLIN_KEYWORDS = [
  "package",
  "import",
  "class",
  "interface",
  "object",
  "data",
  "sealed",
  "enum",
  "fun",
  "val",
  "var",
  "override",
  "public",
  "private",
  "protected",
  "internal",
  "open",
  "abstract",
  "final",
  "if",
  "else",
  "when",
  "for",
  "while",
  "do",
  "return",
  "break",
  "continue",
  "try",
  "catch",
  "finally",
  "throw",
  "null",
  "true",
  "false",
  "as",
  "is",
  "in",
  "this",
  "super",
];

function registerKotlinProviders(monacoApi: typeof monaco) {
  try {
    monacoApi.languages.registerCompletionItemProvider("kotlin", {
      triggerCharacters: [".", "(", " "],
      provideCompletionItems: (model, position) => {
        const word = model.getWordUntilPosition(position);
        const range = new monacoApi.Range(
          position.lineNumber,
          word.startColumn,
          position.lineNumber,
          word.endColumn,
        );
        const suggestions: monaco.languages.CompletionItem[] = [
          ...KOTLIN_KEYWORDS.map((keyword) => ({
            label: keyword,
            kind: monacoApi.languages.CompletionItemKind.Keyword,
            insertText: keyword,
            range,
          })),
          {
            label: "fun main()",
            kind: monacoApi.languages.CompletionItemKind.Snippet,
            insertText: ["fun main() {", "\t$0", "}"].join("\n"),
            insertTextRules: monacoApi.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range,
          },
          {
            label: "data class",
            kind: monacoApi.languages.CompletionItemKind.Snippet,
            insertText: "data class ${1:Name}(${2:val id: Int})",
            insertTextRules: monacoApi.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range,
          },
          {
            label: "println",
            kind: monacoApi.languages.CompletionItemKind.Function,
            insertText: "println($1)",
            insertTextRules: monacoApi.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            range,
          },
        ];
        return { suggestions };
      },
    });
  } catch {
    // Ignore: language might be unavailable in current Monaco build
  }
}

export function setupMonaco(monacoApi: typeof monaco) {
  if (configured) return;
  configured = true;
  registerKotlinProviders(monacoApi);
}
