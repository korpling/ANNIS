---
--- delete entries from source tables
---

-- resolver_vis_map
DELETE FROM resolver_vis_map
USING corpus toplevel
WHERE resolver_vis_map.corpus IN ( SELECT toplevel.name WHERE toplevel.id IN (:ids))
AND (resolver_vis_map.version IN ( SELECT toplevel.version WHERE toplevel.id IN (:ids)) 
OR resolver_vis_map.version is NULL AND toplevel.version is NULL);

-- corpus
DELETE FROM corpus child 
USING corpus toplevel 
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre;
