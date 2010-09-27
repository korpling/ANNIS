CREATE TABLE annotation AS SELECT
    anno.struct_ref,
    anno.anno_level AS ns,
    anno_attribute.attribute AS attribute,
    anno_attribute.value AS value
FROM
    anno, anno_attribute
WHERE
    anno_attribute.anno_ref = anno.id;
    
ALTER TABLE annotation ADD CONSTRAINT "PK_annotation" PRIMARY KEY (struct_ref, attribute);
ALTER TABLE annotation ADD CONSTRAINT "FK_annotation_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct (id);
