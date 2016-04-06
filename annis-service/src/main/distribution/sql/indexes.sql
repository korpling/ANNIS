-----------
-- FACTS --
-----------
CREATE INDEX idx__facts_id__:id
  ON facts_:id
  USING btree
  (id)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_is_token__:id
  ON facts_:id
  USING btree
  (is_token, corpus_ref, text_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_left__:id
  ON facts_:id
  USING btree
  ("left", corpus_ref, text_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_left_token__:id
  ON facts_:id
  USING btree
  (left_token, corpus_ref, text_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_node_annotext__:id
  ON facts_:id
  USING btree
  (node_annotext varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts__q_node_qannotext__:id
  ON facts_:id
  USING btree
  (node_qannotext varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_node_name__:id
  ON facts_:id
  USING btree
  (node_name varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_node_namespace__:id
  ON facts_:id
  USING btree
  (node_namespace varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_right__:id
  ON facts_:id
  USING btree
  ("right", corpus_ref, text_ref)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_right_token__:id
  ON facts_:id
  USING btree
  (right_token, corpus_ref, text_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_root__:id
  ON facts_:id
  USING btree
  (root, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_seg_name_index__:id
  ON facts_:id
  USING btree
  (seg_name)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_seg_index_index__:id
  ON facts_:id
  USING btree
  (seg_index)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_span__:id
  ON facts_:id
  USING btree
  (span varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_token_index__:id
  ON facts_:id
  USING btree
  (token_index, corpus_ref, text_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_corpus_ref_index__:id
  ON facts_:id
  USING btree
  (corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_text_ref_index__:id
  ON facts_:id
  USING btree
  (text_ref)
  WITH (FILLFACTOR=100);

----- 2nd query
CREATE INDEX idx__2nd_query_:id ON facts_:id (corpus_ref, text_ref,left_token, right_token)
  WITH (FILLFACTOR=100);

-- optimize the select distinct
CREATE INDEX idx_distinct_helper_:id ON facts_:id(id, corpus_ref, text_ref, left_token, right_token)
  WITH (FILLFACTOR=100);

-- allow simple searches (node, tok etc)
CREATE INDEX idx__sample_n__:id ON facts_:id(n_sample, corpus_ref)
  WITH (FILLFACTOR=100);
CREATE INDEX idx__sample_n_tok__:id ON facts_:id(n_sample, corpus_ref)
  WITH (FILLFACTOR=100) WHERE is_token = TRUE;
CREATE INDEX idx__sample_n_na__:id ON facts_:id(n_na_sample, corpus_ref)
  WITH (FILLFACTOR=100);
