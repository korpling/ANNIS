CREATE INDEX idx__facts_component_id__:id
  ON facts_:id
  USING btree
  (component_id, edge_name)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_edge_name__:id
  ON facts_:id
  USING btree
  (edge_name varchar_pattern_ops, component_id)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_edge_namespace__:id
  ON facts_:id
  USING btree
  (edge_namespace varchar_pattern_ops)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_level__:id
  ON facts_:id
  USING btree
  ("level", component_id)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_edge_annotext__:id
  ON facts_:id
  USING btree
  (edge_annotext varchar_pattern_ops)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts__edge_qannotext__:id
  ON facts_:id
  USING btree
  (edge_qannotext varchar_pattern_ops)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_parent__:id
  ON facts_:id
  USING btree
  (parent)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_rankid__:id
  ON facts_:id
  USING btree
  (rank_id)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_post__:id
  ON facts_:id
  USING btree
  (post, component_id)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_pre__:id
  ON facts_:id
  USING btree
  (pre, component_id)
  WITH (FILLFACTOR=100);


CREATE INDEX idx__facts_prepost__:id
  ON facts_:id
  USING btree
  (component_id, pre, post)
  WITH (FILLFACTOR=100);

CREATE INDEX idx__facts_edge_type__:id
  ON facts_:id
  USING btree
  (edge_type, component_id);
