-- remove all unecessary span relations, thus the one covered by continuous spans
-- and without and edge annotations

DELETE FROM _rank AS child_rank
USING _node AS child_node, _component AS child_component
WHERE
child_node.id = child_rank.node_ref
AND child_rank.component_ref = child_component.id
AND child_node.token_index IS NOT NULL
AND child_component.type = 'c'
AND NOT EXISTS (
  SELECT 1 FROM _node AS parent_node, _rank AS parent_rank 
  WHERE 
	child_rank.parent = parent_rank.pre
	AND child_rank.component_ref = parent_rank.component_ref
	AND parent_rank.node_ref = parent_node.id
	AND child_component.id = child_rank.component_ref
	AND parent_node.continuous = false
) AND NOT EXISTS (
  SELECT 1 FROM _edge_annotation WHERE rank_ref = child_rank.pre
)
