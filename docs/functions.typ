#let lines(type  , reqs, start: 0) = [
  #for i in range(reqs.len()){
    "•" + text(type + str(i + start), weight: "bold")  + reqs.at(i) + "\n"
  }

]

#let uc_table(
  name: none,
  id: none,
  brief: none,
  actor: none,
  pre: none,
  main: none,
  post: none,
  altA: [],
  altB: [],
) = table(
  columns: (auto, auto),
  inset: 10pt,
  align: horizon,

  "Прецедент", name,
  "ID", id,
  "Краткое описание", brief,
  "Главный актер", actor,
  "Предусловия", pre,
  "Основной поток", main,
  "Альтернативный поток", altA,
  "Альтернативный поток B", altB,
  "Постусловия", post,
)
