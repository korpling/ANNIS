---
--- delete entries from source tables
---

-- resolver_vis_map
DELETE FROM resolver_vis_map
USING corpus toplevel
WHERE resolver_vis_map.corpus IN ( SELECT toplevel.name WHERE toplevel.id IN (:ids))
AND (resolver_vis_map.version IN ( SELECT toplevel.version WHERE toplevel.id IN (:ids)) 
OR resolver_vis_map.version is NULL AND toplevel.version is NULL);

-- text
-- explain analyze
DELETE FROM text 
WHERE toplevel_corpus IN ( :ids );

-- corpus_annotation
-- explain analyze
DELETE FROM corpus_annotation
USING corpus toplevel, corpus child
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre
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
