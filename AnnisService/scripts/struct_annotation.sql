CREATE TABLE struct_annotation AS SELECT
    struct.*,
    annotation.ns,
    annotation.attribute,
    annotation.value
FROM
    struct LEFT OUTER JOIN annotation ON (annotation.struct_ref = struct.id);
    
-- Problem: LEFT OUTER JOIN => Annotations-Spalten können alle NULL sein
-- ALTER TABLE struct_annotations ADD CONSTRAINT "PK_struct_annotations" PRIMARY KEY (id, ns, attribute);
ALTER TABLE struct_annotation ADD CONSTRAINT "FK_struct_annotation_2_text" FOREIGN KEY (text_ref) REFERENCES text (id);
