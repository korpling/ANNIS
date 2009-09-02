-- Create left outer join of rank table with its annoations
CREATE TABLE rank_annotations AS SELECT 
    pre, 
    post, 
	node_ref, 
    parent, 
    level, 
    zshg,
    edge_type,
    edges.name as edge_name, 
    rank_annotation.namespace AS anno_namespace,
    rank_annotation.name AS anno_name,
    value as anno_value
FROM
     edges LEFT JOIN edge_annotation as rank_annotation ON (edges.pre = rank_annotation.rank_ref);
