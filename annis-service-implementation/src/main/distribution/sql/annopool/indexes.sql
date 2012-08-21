BEGIN; -- transaction
-----------
-- FACTS --
-----------

CREATE INDEX idx__facts_component_id__:id
  ON facts_:id
  USING btree
  (component_id, corpus_ref);


CREATE INDEX idx__facts_edge_name__:id
  ON facts_:id
  USING btree
  (edge_name varchar_pattern_ops, corpus_ref);


CREATE INDEX idx__facts_edge_namespace__:id
  ON facts_:id
  USING btree
  (edge_namespace varchar_pattern_ops, corpus_ref);


CREATE INDEX idx__facts_edge_type__:id
  ON facts_:id
  USING btree
  (edge_type, corpus_ref);


CREATE INDEX idx__facts_id__:id
  ON facts_:id
  USING btree
  (id);

CREATE INDEX idx__facts_is_token__:id
  ON facts_:id
  USING btree
  (is_token, corpus_ref);

CREATE INDEX idx__facts_is_token2__:id
  ON facts_:id
  USING btree
  (is_token, text_ref);


CREATE INDEX idx__facts_left__:id
  ON facts_:id
  USING btree
  ("left", text_ref);


CREATE INDEX idx__facts_left_token__:id
  ON facts_:id
  USING btree
  (left_token, text_ref);


CREATE INDEX idx__facts_level__:id
  ON facts_:id
  USING btree
  ("level", corpus_ref);

CREATE INDEX idx__facts_node_anno__:id
  ON facts_:id
  USING btree
  (node_anno_ref, corpus_ref);

CREATE INDEX idx__facts_node_anno2__:id
  ON facts_:id
  USING btree
  (node_anno_ref, text_ref);

CREATE INDEX idx__facts_edge_annotation__:id
  ON facts_:id
  USING btree
  (edge_anno_ref, corpus_ref);

CREATE INDEX idx__facts_node_name__:id
  ON facts_:id
  USING btree
  (node_name varchar_pattern_ops, corpus_ref);

CREATE INDEX idx__facts_node_name2__:id
  ON facts_:id
  USING btree
  (node_name varchar_pattern_ops, text_ref);


CREATE INDEX idx__facts_node_namespace__:id
  ON facts_:id
  USING btree
  (node_namespace varchar_pattern_ops, corpus_ref);

CREATE INDEX idx__facts_node_namespace2__:id
  ON facts_:id
  USING btree
  (node_namespace varchar_pattern_ops, text_ref);


CREATE INDEX idx__facts_parent__:id
  ON facts_:id
  USING btree
  (parent, corpus_ref);


CREATE INDEX idx__facts_post__:id
  ON facts_:id
  USING btree
  (post, corpus_ref);


CREATE INDEX idx__facts_pre__:id
  ON facts_:id
  USING btree
  (pre, corpus_ref);

CREATE INDEX idx__facts_right__:id
  ON facts_:id
  USING btree
  ("right", text_ref);


CREATE INDEX idx__facts_right_token__:id
  ON facts_:id
  USING btree
  (right_token, text_ref);

CREATE INDEX idx__facts_root__:id
  ON facts_:id
  USING btree
  (root, corpus_ref);

CREATE INDEX idx__facts_seg_name_index__:id
  ON facts_:id
  USING btree
  (seg_name);

CREATE INDEX idx__facts_seg_left_index__:id
  ON facts_:id
  USING btree
  (seg_left);

CREATE INDEX idx__facts_seg_right_index__:id
  ON facts_:id
  USING btree
  (seg_right);

CREATE INDEX idx__facts_span__:id
  ON facts_:id
  USING btree
  (span varchar_pattern_ops, corpus_ref);

CREATE INDEX idx__facts_span2__:id
  ON facts_:id
  USING btree
  (span varchar_pattern_ops, text_ref);


CREATE INDEX idx__facts_token_index__:id
  ON facts_:id
  USING btree
  (token_index, text_ref);

CREATE INDEX idx__facts_corpus_ref_index__:id
  ON facts_:id
  USING btree
  (corpus_ref);

CREATE INDEX idx__facts_text_ref_index__:id
  ON facts_:id
  USING btree
  (text_ref);

----- 2nd query
CREATE INDEX idx__2nd_query_:id ON facts_:id (text_ref,left_token, right_token);

-- optimize the select distinct
CREATE INDEX idx_distinct_helper_:id ON facts_:id(id, text_ref, left_token, right_token);

-- allow simple searches (node, tok etc)
CREATE INDEX idx__sample_n__:id ON facts_:id(n_sample);
CREATE INDEX idx__sample_n_tok__:id ON facts_:id(n_sample) WHERE is_token = TRUE;
CREATE INDEX idx__sample_n_na__:id ON facts_:id(n_na_sample);
CREATE INDEX idx__sample_n_r_c__:id ON facts_:id(n_r_c_sample);
CREATE INDEX idx__sample_n_r_c_ea__:id ON facts_:id(n_r_c_ea_sample);
CREATE INDEX idx__sample_n_r_c_na__:id ON facts_:id(n_r_c_na_sample);

END; -- transaction
