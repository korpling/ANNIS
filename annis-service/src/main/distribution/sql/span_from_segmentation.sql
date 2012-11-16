UPDATE _node AS n SET span =
(
  SELECT "value"
  FROM _node_annotation AS na
  WHERE
    na.node_ref = n.id
    AND na.name = n.seg_name
  LIMIT 1
)
WHERE
  seg_name IS NOT NULL
  AND span IS NULL
;
