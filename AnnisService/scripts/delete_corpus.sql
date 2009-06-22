---
--- drop foreign key constraints on affected tables
--- (otherwise, calls to trigger functions dominate the execution time by a few magnitudes)
---

ALTER TABLE rank DROP CONSTRAINT "rank_component_ref_fkey";
ALTER TABLE rank DROP CONSTRAINT "rank_parent_fkey";
ALTER TABLE edge_annotation DROP CONSTRAINT "edge_annotation_rank_ref_fkey";

ALTER TABLE node DROP CONSTRAINT "node_text_ref_fkey";
ALTER TABLE struct_annotation DROP CONSTRAINT "FK_struct_annotation_2_text";
ALTER TABLE rank DROP CONSTRAINT "rank_node_ref_fkey";
ALTER TABLE node_annotation DROP CONSTRAINT "node_annotation_node_ref_fkey";

ALTER TABLE corpus_annotation DROP CONSTRAINT "corpus_annotation_corpus_ref_fkey";
ALTER TABLE node DROP CONSTRAINT "node_corpus_ref_fkey";
-- ALTER TABLE corpus_stats DROP CONSTRAINT "corpus_stats_id_fkey";
-- ALTER TABLE corp_2_viz DROP CONSTRAINT "FK_corp_2_viz_2_corpus";
-- ALTER TABLE xcorp_2_viz DROP CONSTRAINT "FK_xcorp_2_viz_2_corpus";

--- 
--- delete entries from materialized tables
---

-- edges
-- explain analyze
DELETE FROM edges
USING corpus toplevel, corpus child, node, rank
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND rank.node_ref = node.id AND edges.pre = rank.pre;

-- rank_annotations
-- explain analyze
DELETE FROM rank_annotations
USING corpus toplevel, corpus child, node, rank
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND rank.node_ref = node.id AND rank_annotations.pre = rank.pre;

-- rank_text_ref
-- explain analyze
DELETE FROM rank_text_ref
USING corpus toplevel, corpus child, node, rank
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND rank.node_ref = node.id AND rank_text_ref.pre = rank.pre;

-- struct_annotation
-- explain analyze
DELETE FROM struct_annotation
USING corpus toplevel, corpus child
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre < child.pre AND toplevel.post >= child.pre 
AND struct_annotation.corpus_ref = child.id;

---
--- delete table entries for this corpus for each table seperately
--- :ids is replaced by a list of IDs in code
--- XXX: SQL-Injection möglich, wenn IDs als String übergeben
---

-- component
-- explain analyze
DELETE FROM component
USING corpus toplevel, corpus child, node, rank
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND rank.node_ref = node.id AND rank.component_ref = component.id;

-- edge_annotation
-- explain analyze
DELETE FROM edge_annotation
USING corpus toplevel, corpus child, node, rank
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND rank.node_ref = node.id AND edge_annotation.rank_ref = rank.pre;

-- explain analyze
DELETE FROM rank
USING corpus toplevel, corpus child, node
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND rank.node_ref = node.id;

-- node_annotation
-- explain analyze
DELETE FROM node_annotation
USING corpus toplevel, corpus child, node
WHERE toplevel.id IN (:ids) AND toplevel.top_level = 'y'
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
AND node.corpus_ref = child.id AND node_annotation.node_ref = node.id;

-- text
-- explain analyze
DELETE FROM text 
USING corpus toplevel, corpus child, node 
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre < child.pre AND toplevel.post >= child.pre 
AND node.corpus_ref = child.id AND node.text_ref = text.id;

-- node
-- explain analyze
DELETE FROM node
USING corpus toplevel, corpus child
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre < child.pre AND toplevel.post >= child.pre 
AND node.corpus_ref = child.id;

-- corpus_annotation
-- explain analyze
DELETE FROM corpus_annotation
USING corpus toplevel, corpus child
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre < child.pre AND toplevel.post >= child.pre 
AND corpus_annotation.corpus_ref = child.id;

-- corpus_stats
-- explain analyze
DELETE FROM corpus_stats WHERE id IN ( :ids );

-- corpus
-- explain analyze
DELETE FROM corpus child 
USING corpus toplevel 
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre;

---
--- recreate foreign key constraints
---
ALTER TABLE rank ADD CONSTRAINT "rank_component_ref_fkey" FOREIGN KEY (component_ref) REFERENCES component (id);
ALTER TABLE rank ADD CONSTRAINT "rank_parent_fkey" FOREIGN KEY (parent) REFERENCES rank (pre);
ALTER TABLE edge_annotation ADD CONSTRAINT "edge_annotation_rank_ref_fkey" FOREIGN KEY (rank_ref) REFERENCES rank (pre);

ALTER TABLE node ADD CONSTRAINT "node_text_ref_fkey" FOREIGN KEY (text_ref) REFERENCES text (id);
ALTER TABLE struct_annotation ADD CONSTRAINT "FK_struct_annotation_2_text" FOREIGN KEY (text_ref) REFERENCES text (id);;
ALTER TABLE rank ADD CONSTRAINT "rank_node_ref_fkey" FOREIGN KEY (node_ref) REFERENCES node (id);
ALTER TABLE node_annotation ADD CONSTRAINT "node_annotation_node_ref_fkey" FOREIGN KEY (node_ref) REFERENCES node (id);

ALTER TABLE corpus_annotation ADD CONSTRAINT "corpus_annotation_corpus_ref_fkey" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
ALTER TABLE node ADD CONSTRAINT "node_corpus_ref_fkey" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
-- ALTER TABLE corpus_stats ADD CONSTRAINT "corpus_stats_id_fkey" FOREIGN KEY (id) REFERENCES corpus (id);
-- ALTER TABLE corp_2_viz ADD CONSTRAINT "FK_corp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
-- ALTER TABLE xcorp_2_viz ADD CONSTRAINT "FK_xcorp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
