create temporary table __delete_doc_2_corp as
    select distinct doc_2_corp.doc_id as id from doc_2_corp, __delete_corpus where doc_2_corp.corpus_ref = __delete_corpus.id;
create index idx__delete_doc_2_corp on __delete_doc_2_corp(id);

create temporary table __delete_struct as
    select distinct struct.id as id from struct, __delete_doc_2_corp where struct.doc_ref = __delete_doc_2_corp.id;
create index idx__delete_struct on __delete_struct(id);

create temporary table __delete_text as
    select distinct struct.text_ref as id from struct, __delete_struct where struct.id = __delete_struct.id;
create index dix__delete_text on __delete_text(id);

create temporary table __delete_rank as
    select distinct rank.pre as id from rank, __delete_struct where rank.struct_ref = __delete_struct.id;
create index idx__delete_rank on __delete_rank(id);

create temporary table __delete_anno as
    select distinct anno.id as id from anno, __delete_struct where anno.struct_ref = __delete_struct.id;
create index idx__delete_anno on __delete_anno(id);

create temporary table __delete_extdata as
    select distinct anno_attribute.value as id from __delete_anno, anno_attribute where anno_attribute.anno_ref = __delete_anno.id and lower(anno_attribute.attribute) = 'AUDIO';
create index idx__delete_extdata on __delete_extdata(id);

create temporary table __delete_collection as
    select distinct collection.id from collection where collection.id in ( 
        select col_ref from struct, __delete_struct where struct.id = __delete_struct.id union
        select col_ref from anno, __delete_anno where anno.id = __delete_anno.id union
        select col_ref from text, __delete_text where text.id = __delete_text.id 
    );
create index idx__delete_collection on __delete_collection(id);
    

-- delete entries from tables
DELETE FROM rank_anno USING __delete_rank WHERE rank_anno.rank_ref = __delete_rank.id;
DELETE FROM rank USING __delete_rank WHERE pre = __delete_rank.id;
delete from rank_annotation using __delete_rank where pre = __delete_rank.id;
delete from rank_text_ref using __delete_text where text_ref = __delete_text.id;
DELETE FROM anno_attribute using __delete_anno where anno_ref = __delete_anno.id;
delete from anno using __delete_anno where anno.id = __delete_anno.id;
delete from annotation using __delete_struct where annotation.struct_ref = __delete_struct.id;
delete from struct using __delete_struct where struct.id = __delete_struct.id;
delete from struct_annotation using __delete_struct where struct_annotation.id = __delete_struct.id;
delete from text using __delete_text where text.id = __delete_text.id;
delete from col_rank using __delete_collection where col_rank.col_ref = __delete_collection.id;
delete from meta_attribute using __delete_collection where meta_attribute.col_ref = __delete_collection.id;
delete from collection using __delete_collection where collection.id = __delete_collection.id;
delete from doc_2_corp using __delete_corpus where doc_2_corp.corpus_ref = __delete_corpus.id;
delete from document using __delete_doc_2_corp where document.id = __delete_doc_2_corp.id;
delete from corp_2_viz using __delete_corpus where corp_2_viz.corpus_ref = __delete_corpus.id;
delete from xcorp_2_viz using __delete_corpus where xcorp_2_viz.corpus_ref = __delete_corpus.id;
delete from corpus_stats using __delete_corpus where corpus_stats.corpus_ref = __delete_corpus.id;
delete from corpus using __delete_corpus where corpus.id = __delete_corpus.id;

-- FIXME: delete external file
delete from extdata using __delete_extdata where extdata.id = __delete_extdata.id::numeric;

-- drop tables
drop table __delete_extdata;
drop table __delete_corpus; 
drop table __delete_doc_2_corp; 
drop table __delete_collection; 
drop table __delete_text;
drop table __delete_struct; 
drop table __delete_rank; 
drop table __delete_anno; 

