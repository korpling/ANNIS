BEGIN; -- transaction
-----------
-- FACTS --
-----------

----- facts_node

CREATE INDEX idx__facts_id__:id
  ON facts_node_:id
  USING btree
  (id);

CREATE INDEX idx__facts_is_token__:id
  ON facts_node_:id
  USING btree
  (is_token);

CREATE INDEX idx__facts_left__:id
  ON facts_node_:id
  USING btree
  ("left");


CREATE INDEX idx__facts_left_token__:id
  ON facts_node_:id
  USING btree
  (left_token);

CREATE INDEX idx__facts_seg_name_index__:id
  ON facts_node_:id
  USING btree
  (seg_name);

CREATE INDEX idx__facts_seg_index_index__:id
  ON facts_node_:id
  USING btree
  (seg_index);

CREATE INDEX idx__facts_node_anno__:id
  ON facts_node_:id
  USING btree
  (node_anno_ref);

CREATE INDEX idx__facts_node_name__:id
  ON facts_node_:id
  USING btree
  (node_name varchar_pattern_ops);


CREATE INDEX idx__facts_node_namespace__:id
  ON facts_node_:id
  USING btree
  (node_namespace varchar_pattern_ops);

CREATE INDEX idx__facts_right__:id
  ON facts_node_:id
  USING btree
  ("right");


CREATE INDEX idx__facts_right_token__:id
  ON facts_node_:id
  USING btree
  (right_token);

CREATE INDEX idx__facts_span__:id
  ON facts_node_:id
  USING btree
  (span varchar_pattern_ops);


CREATE INDEX idx__facts_token_index__:id
  ON facts_node_:id
  USING btree
  (token_index);

CREATE INDEX idx__facts_corpus_ref_index__:id
  ON facts_node_:id
  USING btree
  (corpus_ref);

CREATE INDEX idx__facts_text_ref_index__:id
  ON facts_node_:id
  USING btree
  (text_ref);

----- facts_edge

CREATE INDEX idx__facts_component_id__:id
  ON facts_edge_:id
  USING btree
  (component_id);


CREATE INDEX idx__facts_edge_name__:id
  ON facts_edge_:id
  USING btree
  (edge_name varchar_pattern_ops);


CREATE INDEX idx__facts_edge_namespace__:id
  ON facts_edge_:id
  USING btree
  (edge_namespace varchar_pattern_ops);


CREATE INDEX idx__facts_edge_type__:id
  ON facts_edge_:id
  USING btree
  (edge_type);

CREATE INDEX idx__facts_level__:id
  ON facts_edge_:id
  USING btree
  ("level");

CREATE INDEX idx__facts_edge_annotation__:id
  ON facts_edge_:id
  USING btree
  (edge_anno_ref);

CREATE INDEX idx__facts_egde_node_ref__:id
  ON facts_edge_:id
  USING btree
  (node_ref);


CREATE INDEX idx__facts_parent__:id
  ON facts_edge_:id
  USING btree
  (parent);


CREATE INDEX idx__facts_post__:id
  ON facts_edge_:id
  USING btree
  (post);


CREATE INDEX idx__facts_pre__:id
  ON facts_edge_:id
  USING btree
  (pre);

CREATE INDEX idx__facts_root__:id
  ON facts_edge_:id
  USING btree
  (root);


----- 2nd query
CREATE INDEX idx__2nd_query_:id ON facts_node_:id (text_ref,left_token, right_token);

-- allow simple searches (node, tok etc)
CREATE INDEX idx__sample_n__:id ON facts_node_:id(n_sample);
CREATE INDEX idx__sample_n_tok__:id ON facts_node_:id(n_sample) WHERE is_token = TRUE;
CREATE INDEX idx__sample_n_r_c__:id ON facts_edge_:id(r_c_sample);



END; -- transaction
