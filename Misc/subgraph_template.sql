
WITH
  matches AS
  (
    (
      (
      SELECT 1 AS n, 1 AS nodeNr, facts4.id AS id, facts4.text_ref AS text, facts4.left_token AS min, facts4.right_token AS max, facts4.corpus_ref AS corpus, facts4.node_name AS "name"
      FROM     facts_2010 AS facts4, corpus AS corpus4
      WHERE     corpus4.path_name = '{new.kreuterbuch.1543, Ridges_v2, Ridges_Herbology_Version_2.0}' AND
          facts4.corpus_ref = corpus4.id AND
          facts4.node_name = 'sTok4772' AND
          facts4.toplevel_corpus IN ( 2010) 
      LIMIT 1
      ) 
      UNION ALL
      (
      SELECT 1 AS n, 2 AS nodeNr, facts3.id AS id3, facts3.text_ref AS text3, facts3.left_token AS min3, facts3.right_token AS max3, facts3.corpus_ref AS corpus3, facts3.node_name AS name3
      FROM     facts_2010 AS facts3, corpus AS corpus3
      WHERE     corpus3.path_name = '{new.kreuterbuch.1543, Ridges_v2, Ridges_Herbology_Version_2.0}' AND
          facts3.corpus_ref = corpus3.id AND
          facts3.node_name = 'sTok4771' AND
          facts3.toplevel_corpus IN ( 2010) 
      LIMIT 1
      )
      UNION ALL
      (
      SELECT 1 AS n, 3 AS nodeNr, facts2.id AS id2, facts2.text_ref AS text2, facts2.left_token AS min2, facts2.right_token AS max2, facts2.corpus_ref AS corpus2, facts2.node_name AS name2
      FROM     facts_2010 AS facts2, corpus AS corpus2
      WHERE     corpus2.path_name = '{new.kreuterbuch.1543, Ridges_v2, Ridges_Herbology_Version_2.0}' AND
          facts2.corpus_ref = corpus2.id AND
          facts2.node_name = 'sSpan15122' AND
          facts2.toplevel_corpus IN ( 2010) 
      LIMIT 1
      )
      UNION ALL
      (
      SELECT 1 AS n, 4 AS nodeNr, facts1.id AS id1, facts1.text_ref AS text1, facts1.left_token AS min1, facts1.right_token AS max1, facts1.corpus_ref AS corpus1, facts1.node_name AS name1
      FROM     facts_2010 AS facts1, corpus AS corpus1
      WHERE     corpus1.path_name = '{new.kreuterbuch.1543, Ridges_v2, Ridges_Herbology_Version_2.0}' AND
          facts1.corpus_ref = corpus1.id AND
          facts1.node_name = 'sTok4769' AND
          facts1.toplevel_corpus IN ( 2010) 
      LIMIT 1
      )
    )
  ),
  keys AS (
    SELECT n, array_agg(id ORDER BY nodenr DESC) AS "key" FROM matches GROUP BY n
  ),
  nearestseg AS
  (
    SELECT
      matches.n, matches.nodeNr, matches.id AS id,
      facts.seg_index - 5 AS "min",
      facts.seg_index + 5 AS "max",
      facts.text_ref AS "text", 
      facts.corpus_ref AS "corpus", 
      row_number() OVER (PARTITION BY facts.corpus_ref, facts.text_ref, nodenr ORDER BY NULLIF(min - facts.left_token+ 1, -abs(min - facts.left_token + 1)) ASC) AS rank_left,
      row_number() OVER (PARTITION BY facts.corpus_ref, facts.text_ref, nodenr ORDER BY NULLIF(facts.right_token - max + 1, -abs(facts.right_token - max + 1)) ASC) AS rank_right
    FROM facts_2010 as facts, matches
    WHERE
      facts.toplevel_corpus IN (2010) AND
      facts.n_sample IS TRUE AND
      facts.seg_name = 'dipl' AND
      facts.text_ref = matches.text AND
      facts.corpus_ref = matches.corpus
  ),
  solutions AS
  (
    SELECT DISTINCT keys.key,
                    facts.left_token AS "min", facts.right_token AS "max", 
                    facts.text_ref AS "text", facts.corpus_ref AS "corpus"
    FROM nearestseg, facts_2010 AS facts, keys
    WHERE
      nearestseg.n = keys.n AND
      facts.toplevel_corpus IN (2010) AND
      facts.n_sample IS TRUE AND
      facts.seg_name = 'dipl' AND
      facts.text_ref = nearestseg."text" AND
      facts.corpus_ref = nearestseg."corpus" AND
      facts.seg_index <= nearestseg."max" AND
      facts.seg_index >= nearestseg."min" AND
      (nearestseg.rank_left = 1 OR nearestseg.rank_right = 1)
      GROUP BY "key", left_token, right_token, text_ref, corpus_ref
  )
  SELECT * FROM solutions
  ;
  
