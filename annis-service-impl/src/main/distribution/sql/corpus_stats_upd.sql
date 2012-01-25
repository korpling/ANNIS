UPDATE
  corpus_stats
SET 
  max_rank_post = (SELECT max(post) + 1 FROM facts),
  max_component_id = (SELECT max(component_id) + 1 FROM facts),
  max_node_id = (SELECT max(id) + 1  FROM facts)
;
