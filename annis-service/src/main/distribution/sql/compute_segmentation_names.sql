UPDATE corpus AS top SET seg_names = 
(
  SELECT array_agg(DISTINCT n.seg_name)
  FROM corpus AS child, _node AS n
  WHERE
    child.pre >= top.pre AND child.post <= top.post
    AND seg_name IS NOT NULL
)
WHERE
  top.top_level IS TRUE 
  AND top.id = :id
;
