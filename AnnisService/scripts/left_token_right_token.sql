-- add columns left_token, right_token and copy values from token_index
ALTER TABLE _struct ADD left_token integer, ADD right_token integer;
UPDATE _struct SET left_token = token_index, right_token = token_index;

-- create a temporary table to get rid of joins between struct and rank
-- force an ANALYZE on the table, so Postgres won't use slow seq scan
CREATE TABLE tmp AS SELECT r.pre, r.post, s.id, s.left_token, s.right_token FROM _rank r, _struct s WHERE r.struct_ref = s.id;
ANALYZE tmp;

-- set left, right values for non-terminals
CREATE INDEX idx_tmp__pre_post ON tmp (pre, post);
UPDATE tmp SET left_token = (SELECT min(t2.left_token) FROM tmp t2 WHERE t2.pre >= tmp.pre AND t2.pre <= tmp.post);
UPDATE tmp SET right_token = (SELECT max(t2.right_token) FROM tmp t2 WHERE t2.pre >= tmp.pre AND t2.pre <= tmp.post);

-- copy left, right values for nodes that are a copy of another node without covering its children (pointing relation)
UPDATE tmp SET left_token = t.left_token FROM tmp t WHERE tmp.left_token IS NULL AND t.left_token IS NOT NULL AND tmp.id = t.id;
UPDATE tmp SET right_token = t.right_token FROM tmp t WHERE tmp.right_token IS NULL AND t.right_token IS NOT NULL AND tmp.id = t.id;

-- copy left, right values for everything 
CREATE INDEX idx_tmp__id ON tmp (id);
UPDATE _struct SET left_token = tmp.left_token, right_token = tmp.right_token FROM tmp WHERE _struct.id = tmp.id;

-- clean up
DROP TABLE tmp;
