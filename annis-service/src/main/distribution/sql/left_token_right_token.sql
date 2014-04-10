ALTER TABLE _node ADD left_token integer, ADD right_token integer;

UPDATE _node AS parent SET 
left_token = (
  CASE
  WHEN parent.token_index IS NULL THEN (
    SELECT min(token_index) FROM _node AS child 
    WHERE 
      parent.left = child.left 
      AND parent.corpus_ref = child.corpus_ref 
      AND parent.text_ref = child.text_ref 
      AND child.token_index IS NOT NULL
  ) 
  ELSE parent.token_index
  END
), 
right_token = (
  CASE
    WHEN parent.token_index IS NULL THEN (
    SELECT max(token_index) FROM _node AS child 
    WHERE 
      parent.right = child.right 
      AND parent.corpus_ref = child.corpus_ref 
      AND parent.text_ref = child.text_ref 
      AND child.token_index IS NOT NULL
  )
  ELSE parent.token_index
  END
);