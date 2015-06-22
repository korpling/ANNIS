UPDATE corpus AS child SET path_name = 
(
  SELECT array_agg(ancestor_name)
  FROM
  (
    SELECT ancestor.name as ancestor_name, child.id as child_id
    FROM corpus as ancestor
    WHERE
      child.pre >= ancestor.pre AND child.post <= ancestor.post
      AND top_corpus_pre(:id) <= ancestor.pre AND ancestor.post <= top_corpus_post(:id)
    ORDER BY ancestor.pre DESC
  ) AS sub
  GROUP BY child_id 
)
WHERE
pre >= top_corpus_pre(:id) AND post <= top_corpus_post(:id);