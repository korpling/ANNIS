---
--- delete entries from source tables
---

-- corpus
DELETE FROM corpus child 
USING corpus toplevel 
WHERE toplevel."name" IN ( :names ) AND toplevel.top_level IS TRUE 
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre;
