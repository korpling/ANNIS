---
--- drop foreign key constraints on affected tables
--- (otherwise, calls to trigger functions dominate the execution time by a few magnitudes)
---

ALTER TABLE rank DROP CONSTRAINT "rank_component_ref_fkey";
ALTER TABLE rank DROP CONSTRAINT "rank_parent_fkey";
ALTER TABLE edge_annotation DROP CONSTRAINT "edge_annotation_rank_ref_fkey";

ALTER TABLE node DROP CONSTRAINT "node_text_ref_fkey";
ALTER TABLE rank DROP CONSTRAINT "rank_node_ref_fkey";
ALTER TABLE node_annotation DROP CONSTRAINT "node_annotation_node_ref_fkey";

ALTER TABLE corpus_annotation DROP CONSTRAINT "corpus_annotation_corpus_ref_fkey";
ALTER TABLE node DROP CONSTRAINT "node_corpus_ref_fkey";

---
--- delete table entries for this corpus for each table seperately
--- :ids is replaced by a list of IDs in code
--- XXX: SQL-Injection möglich, wenn IDs als String übergeben
---

--- 
--- delete entries from materialized tables
---

-- explain analyze
DELETE FROM facts
WHERE toplevel_corpus IN ( :ids );

---
--- delete entries from source tables
---

-- component
-- resolver_vis_map
DELETE FROM resolver_vis_map
USING corpus toplevel
WHERE resolver_vis_map.corpus IN ( SELECT toplevel.name WHERE toplevel.id IN (:ids))
AND (resolver_vis_map.version IN ( SELECT toplevel.version WHERE toplevel.id IN (:ids)) 
OR resolver_vis_map.version is NULL AND toplevel.version is NULL);

-- component
-- explain analyze
DELETE FROM component
USING node, rank
WHERE node.toplevel_corpus IN (:ids) 
AND rank.node_ref = node.id AND rank.component_ref = component.id;

-- edge_annotation
-- explain analyze
DELETE FROM edge_annotation
USING node, rank
WHERE node.toplevel_corpus IN (:ids)
AND rank.node_ref = node.id AND edge_annotation.rank_ref = rank.pre;

-- explain analyze
DELETE FROM rank
USING node
WHERE node.toplevel_corpus IN (:ids)
AND rank.node_ref = node.id;

-- node_annotation
-- explain analyze
DELETE FROM node_annotation
USING node
WHERE node.toplevel_corpus IN ( :ids ) 
AND node_annotation.node_ref = node.id;

-- text
-- explain analyze
DELETE FROM text 
USING node 
WHERE node.toplevel_corpus IN ( :ids ) 
AND node.text_ref = text.id;

-- node
-- explain analyze
DELETE FROM node
WHERE node.toplevel_corpus IN ( :ids ) ;

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
ALTER TABLE rank ADD CONSTRAINT "rank_node_ref_fkey" FOREIGN KEY (node_ref) REFERENCES node (id);
ALTER TABLE node_annotation ADD CONSTRAINT "node_annotation_node_ref_fkey" FOREIGN KEY (node_ref) REFERENCES node (id);

ALTER TABLE corpus_annotation ADD CONSTRAINT "corpus_annotation_corpus_ref_fkey" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
ALTER TABLE node ADD CONSTRAINT "node_corpus_ref_fkey" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
