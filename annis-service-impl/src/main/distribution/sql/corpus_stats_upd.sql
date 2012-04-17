UPDATE
  corpus_stats
SET 
  max_component_id = (SELECT max(id) + 1 FROM _component),
  max_node_id = (SELECT max(id) + 1  FROM _node)
WHERE
  id  = :id
;
