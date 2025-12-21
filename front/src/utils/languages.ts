export function toMonacoLanguage(raw?: string | null) {
  const value = (raw ?? "").trim().toLowerCase();
  switch (value) {
    case "typescript":
      return "typescript";
    case "javascript":
      return "javascript";
    case "python":
      return "python";
    case "java":
      return "java";
    case "kotlin":
      return "kotlin";
    case "go":
      return "go";
    case "rust":
      return "rust";
    case "cpp":
    case "c++":
      return "cpp";
    case "csharp":
    case "c#":
      return "csharp";
    case "sql":
      return "sql";
    case "bash":
    case "shell":
      return "shell";
    default:
      return "plaintext";
  }
}
