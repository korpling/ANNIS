UPDATE corpus SET path_name = 
(
  SELECT array_agg(ancestor_name)
  FROM
  (
    SELECT ancestor.name as ancestor_name, child.id as child_id
    FROM corpus as ancestor, corpus as child
    WHERE
      child.pre >= ancestor.pre AND child.post <= ancestor.post
      AND child.id = corpus.id
    ORDER BY ancestor.pre DESC
  ) AS sub
  GROUP BY child_id 
)
WHERE
  id IN
  (
    SELECT descendent.id
    FROM
      corpus as top, corpus as descendent
    WHERE
      descendent.pre >= top.pre AND descendent.post <= top.post
      AND top.id = :id
      AND top.top_level = true
  )