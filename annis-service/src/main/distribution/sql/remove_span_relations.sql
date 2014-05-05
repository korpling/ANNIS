-- remove all unecessary span relations, thus the one covered by continuous spans
-- and without and edge annotations

DELETE FROM _rank AS child_rank
WHERE
NOT EXISTS (
  SELECT 1 FROM _node AS parent_node, _rank AS parent_rank, _component AS child_component 
  WHERE 
	child_rank.parent = parent_rank.pre
	AND child_rank.component_ref = parent_rank.component_ref
	AND parent_rank.node_ref = parent_node.id
	AND child_component.id = child_rank.component_ref
	AND child_component.type = 'c'
	AND parent_node.continuous = true
) AND NOT EXISTS (
  SELECT 1 FROM _edge_annotation WHERE rank_ref = child_rank.id
)
