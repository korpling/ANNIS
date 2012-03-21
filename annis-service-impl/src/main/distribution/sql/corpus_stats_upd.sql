UPDATE
  corpus_stats
SET 
  max_rank_post = (SELECT max(post) + 1 FROM _rank),
  max_component_id = (SELECT max(id) + 1 FROM _component),
  max_node_id = (SELECT max(id) + 1  FROM _node)
WHERE
  id  = :id
;
