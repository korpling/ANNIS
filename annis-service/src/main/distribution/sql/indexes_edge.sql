CREATE INDEX idx__facts_component_id__:id
  ON facts_:id
  USING btree
  (component_id, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_edge_name__:id
  ON facts_:id
  USING btree
  (edge_name varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_edge_namespace__:id
  ON facts_:id
  USING btree
  (edge_namespace varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_edge_type__:id
  ON facts_:id
  USING btree
  (edge_type, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_level__:id
  ON facts_:id
  USING btree
  ("level", corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_edge_annotext__:id
  ON facts_:id
  USING btree
  (edge_annotext varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts__edge_qannotext__:id
  ON facts_:id
  USING btree
  (edge_qannotext varchar_pattern_ops, corpus_ref)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_parent__:id
  ON facts_:id
  USING btree
  (parent, corpus_ref)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_post__:id
  ON facts_:id
  USING btree
  (post, corpus_ref)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_pre__:id
  ON facts_:id
  USING btree
  (pre, corpus_ref)
  WITH (FILLFACTOR=100);
