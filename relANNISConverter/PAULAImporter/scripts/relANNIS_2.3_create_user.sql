-- **************************************************************************************************************
-- *****					SQL create script zur Erzeugung des relANNIS Benutzres					*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.2															*****
-- *****	Datum:		08.04.2008													*****
-- **************************************************************************************************************
DROP USER "relANNIS_user";

CREATE ROLE "relANNIS_user" LOGIN
  ENCRYPTED PASSWORD 'md50d49afc443f2e2a6423873e7085c413e'
  SUPERUSER INHERIT CREATEDB CREATEROLE;
UPDATE pg_authid SET rolcatupdate=true WHERE OID=16636::oid;
