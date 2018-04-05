---
--- delete entries from source tables
---

-- corpus
DELETE FROM corpus child 
USING corpus toplevel 
WHERE toplevel.id IN ( :ids ) 
AND toplevel.pre <= child.pre AND toplevel.post >= child.pre;
