-- count pos="ART" & pos="NN" & pos="VAPP" & #1 . #2 & #2 .1,30 #3

SELECT 
  count(*)
FROM
  (
    SELECT
      facts1.id AS id1, facts2.id AS id2, facts3.id AS id3, facts1.toplevel_corpus
    FROM
      s_facts AS facts1,
      s_facts AS facts2,
      s_facts AS facts3
    WHERE
      (facts1.sample & B'01000') = B'01000' AND
      (facts2.sample & B'01000') = B'01000' AND
      (facts3.sample & B'01000') = B'01000' AND
      -- annotations can always only be inside a subcorpus/document AND
      -- artificial node-node_annotation subview AND
      facts1.corpus_ref = facts3.corpus_ref AND
      facts1.node_anno = ANY(getNodeAnno('pos', 'ART', ARRAY[2015])) AND
      facts1.right_token = facts2.left_token - 1 AND
      facts1.text_ref = facts2.text_ref AND
      facts1.toplevel_corpus IN (2015) AND
      facts2.node_anno = ANY(getNodeAnno('pos', 'NN', ARRAY[2015])) AND
      facts2.right_token BETWEEN SYMMETRIC facts3.left_token - 1 AND facts3.left_token - 30 AND
      facts2.text_ref = facts3.text_ref AND
      facts2.toplevel_corpus IN (2015) AND
      facts3.node_anno = ANY(getNodeAnno('pos', 'VAPP', ARRAY[2015])) AND
      facts3.toplevel_corpus IN (2015)
  ) AS solutions
;

-- count pos="ART" & pos="NN" & pos="VAPP" & cat="NP" &  #1 . #2 & #2 .1,30 #3 & #4 >* #3
SELECT 
  count(*)
FROM
  (
    SELECT DISTINCT
      facts1.id AS id1, facts2.id AS id2, facts3.id AS id3,
      facts4.id AS id4, 
      facts1.toplevel_corpus
    FROM
      s_facts AS facts1,
      s_facts AS facts2,
      s_facts AS facts3,
      s_facts AS facts4
    WHERE
      (facts1.sample & B'01000') = B'01000' AND
      (facts2.sample & B'01000') = B'01000' AND
      (facts3.sample & B'00001') = B'00001' AND
      (facts4.sample & B'00001') = B'00001' AND
      -- annotations can always only be inside a subcorpus/document AND
      -- artificial node-node_annotation subview AND
      -- artificial node-rank-component-node_annotation subview AND
      facts1.corpus_ref = facts3.corpus_ref AND
      facts1.corpus_ref = facts4.corpus_ref AND
      facts1.node_anno = ANY(getNodeAnno('pos', 'ART', ARRAY[2015])) AND
      facts1.right_token = facts2.left_token - 1 AND
      facts1.text_ref = facts2.text_ref AND
      facts1.toplevel_corpus IN (2015) AND
      facts2.corpus_ref = facts4.corpus_ref AND
      facts2.node_anno = ANY(getNodeAnno('pos', 'NN', ARRAY[2015])) AND
      facts2.right_token BETWEEN SYMMETRIC facts3.left_token - 1 AND facts3.left_token - 30 AND
      facts2.text_ref = facts3.text_ref AND
      facts2.toplevel_corpus IN (2015) AND
      facts3.corpus_ref = facts4.corpus_ref AND
      facts3.edge_name IS NULL AND
      facts3.edge_type = 'd' AND
      facts3.node_anno = ANY(getNodeAnno('pos', 'VAPP', ARRAY[2015])) AND
      facts3.pre < facts4.post AND
      facts3.toplevel_corpus IN (2015) AND
      facts4.node_anno = ANY(getNodeAnno('cat', 'NP', ARRAY[2015])) AND
      facts4.pre < facts3.pre AND
      facts4.toplevel_corpus IN (2015)
  ) AS solutions
;
--  select * from annotations where "type"='node' and toplevel_corpus='2014' and "name"='cat' order by occurences;
