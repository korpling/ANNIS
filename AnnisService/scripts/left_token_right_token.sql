-- BEGIN;

-- FIXME: das ist nicht richtig!!!! Erzeugt Token, wo keine sind, z.B. wenn eine ZSHG-Komponente unterbrochen ist
-- set token_index for leafs where it is missing
-- UPDATE _node SET token_index = 0 FROM _rank WHERE token_index IS NULL AND _rank.node_ref = _node.id AND _rank.pre = _rank.post - 1; 

-- add columns left_token, right_token and copy values from token_index
ALTER TABLE _node ADD left_token integer, ADD right_token integer;
UPDATE _node SET left_token = token_index, right_token = token_index;

-- create a temporary table to get rid of joins between struct and rank
-- force an ANALYZE on the table, so Postgres won't use slow seq scan
CREATE TABLE _tmp_spanned_tokens AS 
SELECT r.pre, r.post, s.id, s.name, s.token_index, s.left_token, s.left_token AS old_left, s.right_token, s.right_token AS old_right 
FROM _rank r, _node s, _component c 
WHERE r.node_ref = s.id AND r.component_ref = c.id AND c.type IN ('c', 'd');
ANALYZE _tmp_spanned_tokens;

CREATE INDEX idx_tmp__pre_post ON _tmp_spanned_tokens (pre, post);
CREATE INDEX idx_tmp__id__non_terminals ON _tmp_spanned_tokens (id) WHERE token_index IS NOT NULL;

-- SAVEPOINT init;

SELECT compute_spanned_tokens();

-- SAVEPOINT computed;

-- copy left, right values for everything 
UPDATE _node SET left_token = tmp.left_token, right_token = tmp.right_token FROM _tmp_spanned_tokens AS tmp WHERE _node.id = tmp.id;

-- clean up
DROP TABLE _tmp_spanned_tokens;

-- SAVEPOINT stored;

-- COMMIT;