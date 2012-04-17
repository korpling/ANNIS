SELECT 
  count(*)
FROM
  (
    SELECT DISTINCT
      facts_node1.id AS id1, facts_node2.id AS id2, facts_node3.id AS id3, facts_node1.toplevel_corpus
    FROM
      facts_node AS facts_node1, facts_edge AS facts_edge1,
      facts_node AS facts_node2, facts_edge AS facts_edge2,
      facts_node AS facts_node3, facts_edge AS facts_edge3
    WHERE
      -- annotations can always only be inside a subcorpus/document AND
      facts_edge1.component_id = facts_edge2.component_id AND
      facts_edge1.node_ref = facts_node1.id AND
      facts_edge1.pre < facts_edge2.pre AND
      facts_edge2.component_id = facts_edge3.component_id AND
      facts_edge2.edge_name IS NULL AND
      facts_edge2.edge_type = 'd' AND
      facts_edge2.node_ref = facts_node2.id AND
      facts_edge2.pre < facts_edge1.post AND
      facts_edge2.pre < facts_edge3.pre AND
      facts_edge3.edge_name IS NULL AND
      facts_edge3.edge_type = 'd' AND
      facts_edge3.node_ref = facts_node3.id AND
      facts_edge3.pre < facts_edge2.post AND
      facts_node1.corpus_ref = facts_node2.corpus_ref AND
      facts_node1.corpus_ref = facts_node3.corpus_ref AND
      facts_node1.node_anno_ref= ANY(getAnnoByNameVal('cat', 'S', ARRAY[1974], 'node')) AND
      facts_node1.toplevel_corpus IN (1974) AND
      facts_edge1.toplevel_corpus IN (1974) AND
      facts_node2.corpus_ref = facts_node3.corpus_ref AND
      facts_node2.node_anno_ref= ANY(getAnnoByNameVal('cat', 'NP', ARRAY[1974], 'node')) AND
      facts_node2.toplevel_corpus IN (1974) AND
      facts_edge2.toplevel_corpus IN (1974) AND
      facts_node3.span ~ '^H.*$' AND
      facts_node3.toplevel_corpus IN (1974) AND
      facts_edge3.toplevel_corpus IN (1974)
  ) AS solutions

