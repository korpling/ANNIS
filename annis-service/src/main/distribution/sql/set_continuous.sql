-- This will set the "continous" property of the spans correctly

UPDATE _node AS parent_node SET continuous = false
WHERE
EXISTS
(
	SELECT parent_node.id AS parent_id, generate_series(parent_node.left_token, parent_node.right_token) AS s
	FROM  _component AS c, _rank AS r
	WHERE 
		parent_node.id = r.node_ref
		AND parent_node.token_index IS NULL
		AND r.component_ref = c.id
		AND c.type='c'
		
	EXCEPT

	SELECT parent_node.id AS parent_id, c_n.token_index AS s
	FROM _node AS c_n, _rank AS p_r, _rank AS c_r, _component AS c_c
	WHERE 
		p_r.pre = c_r.parent
		AND p_r.component_ref = c_r.component_ref
		AND parent_node.id = p_r.node_ref
		AND c_n.id = c_r.node_ref
		AND c_c.id = c_r.component_ref
		AND c_c.type = 'c'
)
