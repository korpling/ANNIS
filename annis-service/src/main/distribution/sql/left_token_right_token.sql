ALTER TABLE _node ADD left_token integer, ADD right_token integer;

UPDATE _node AS parent SET 
left_token = (
  SELECT min(token_index) FROM _node AS child 
  WHERE 
    parent.left = child.left 
    AND parent.corpus_ref = child.corpus_ref 
    AND parent.text_ref = child.text_ref 
    AND child.token_index IS NOT NULL
), 
right_token = (
  SELECT max(token_index) FROM _node AS child 
  WHERE 
    parent.right = child.right 
    AND parent.corpus_ref = child.corpus_ref 
    AND parent.text_ref = child.text_ref 
    AND child.token_index IS NOT NULL
);