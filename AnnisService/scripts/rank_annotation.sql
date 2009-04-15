-- Create left outer join of rank table with its annoations
CREATE TABLE rank_annotation AS SELECT 
    pre, 
    post, 
    struct_ref, 
    parent, 
    level, 
    edge_type,
    name as edge_name, 
    edge, 
    value as edge_value
FROM
     rank LEFT JOIN rank_anno ON (rank.pre = rank_anno.rank_ref);
