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


-- attempt to fix invalid values
CREATE INDEX tmpidx__node__left_right_token ON _node (left_token, right_token);
CREATE INDEX tmpidx__node__right_left_token ON _node (right_token, left_token);

UPDATE _node SET left_token = right_token
WHERE right_token IS NOT NULL AND left_token IS NULL;

UPDATE _node SET right_token = left_token
WHERE right_token IS NULL AND left_token IS NOT NULL;

UPDATE _node SET left_token=-1, right_token=-1
WHERE right_token IS NULL AND left_token IS NULL;