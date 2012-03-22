SELECT count(*)
  FROM
  (
  WITH n1 AS (
    SELECT * FROM facts_node_1974
    WHERE
      node_anno_ref= ANY(getAnnoByNameVal('cat', 'S', ARRAY[1974], 'node'))
  ), n2 AS (
    SELECT * FROM facts_node_1974
    WHERE
      node_anno_ref= ANY(getAnnoByNameVal('cat', 'NP', ARRAY[1974], 'node'))
  ), n3 AS (
    SELECT * FROM facts_node_1974
    WHERE
      span ~ '^H.*$'
  )
  SELECT DISTINCT n1.id as id1, n2.id as id2, n3.id as id3 
  FROM facts_edge_1974 e1, facts_edge_1974 e2, facts_edge_1974 e3, facts_edge_1974 e4, n1, n2, n3
  WHERE
    -- #1 >* #2
    e1.component_id = e2.component_id AND
    e1.pre < e2.pre AND
    e2.pre < e1.post AND
    e1.node_ref = n1.id AND
    e2.node_ref = n2.id AND

    -- #3 >* #3
    e3.component_id = e4.component_id AND
    e3.pre < e4.pre AND
    e4.pre < e3.post AND
    e3.node_ref = n2.id AND
    e4.node_ref = n3.id
    
) as counter;


