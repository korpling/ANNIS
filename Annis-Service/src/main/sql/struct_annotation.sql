CREATE TABLE struct_annotation AS SELECT
    node.*,
    annotation.namespace AS anno_namespace,
    annotation.name AS anno_name,
    annotation.value AS anno_value
FROM
    node LEFT OUTER JOIN node_annotation AS annotation ON (annotation.node_ref = node.id);
    
-- Problem: LEFT OUTER JOIN => Annotations-Spalten können alle NULL sein
-- ALTER TABLE struct_annotations ADD CONSTRAINT "PK_struct_annotations" PRIMARY KEY (id, ns, attribute);
ALTER TABLE struct_annotation ADD CONSTRAINT "FK_struct_annotation_2_text" FOREIGN KEY (text_ref) REFERENCES text (id) ON DELETE CASCADE;
