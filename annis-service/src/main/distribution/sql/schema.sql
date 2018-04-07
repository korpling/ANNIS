-- (modified) source tables

DROP TABLE IF EXISTS corpus CASCADE;
CREATE TABLE corpus
(
  id         integer PRIMARY KEY,
  name       varchar NOT NULL, -- UNIQUE,
  type       varchar NOT NULL,
  version    varchar COLLATE "C",
  pre        integer NOT NULL UNIQUE,
  post       integer NOT NULL UNIQUE,
  top_level  boolean NOT NULL,  -- true for roots of the corpus forest
  path_name  varchar[] COLLATE "C"
);
COMMENT ON COLUMN corpus.id IS 'primary key';
COMMENT ON COLUMN corpus.name IS 'name of the corpus';
COMMENT ON COLUMN corpus.pre IS 'pre-order value';
COMMENT ON COLUMN corpus.post IS 'post-order value';
COMMENT ON COLUMN corpus.path_name IS 'path of this corpus in the corpus tree (names)';

DROP TABLE IF EXISTS corpus_annotation CASCADE;
CREATE TABLE corpus_annotation
(
  corpus_ref  integer NOT NULL REFERENCES corpus (id) ON DELETE CASCADE,
  namespace   varchar COLLATE "C",
  name        varchar COLLATE "C" NOT NULL,
  value       varchar COLLATE "C",
  UNIQUE (corpus_ref, namespace, name)
);
COMMENT ON COLUMN corpus_annotation.corpus_ref IS 'foreign key to corpus.id';
COMMENT ON COLUMN corpus_annotation.namespace IS 'optional namespace of annotation key';
COMMENT ON COLUMN corpus_annotation.name IS 'annotation key';
COMMENT ON COLUMN corpus_annotation.value IS 'annotation value';


-- HACK: add a custom operator which is the same as "=" for integers but always
-- returns 0.995 as join selectivity. See the description
-- of "annis.hack_operator_same_span" in conf/develop.properties or the
-- comments in DefaultWhereClauseGenerator#addSameSpanConditions(...)
-- for details.
DROP OPERATOR IF EXISTS ^=^ (integer, integer);
CREATE OPERATOR ^=^ (
 PROCEDURE= int4eq,
 LEFTARG = integer,
 RIGHTARG = integer,
 JOIN = 'nlikejoinsel',
 MERGES
);