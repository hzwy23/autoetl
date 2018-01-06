create or replace procedure %PROC_NAME%(
%ARGUMENT%
)
/***************************************************
%PROC_COMMENTS%
****************************************************/
as
version varchar2(100) := 'v1.0';
begin
insert into %TARGET_TABLE% (
%TARGET_COLUMNS%
)
select
%EXPRESSION_COLUMNS%
from %MAIN_TABLE% %MAIN_TABLE_ALIAS%
%SUB_TABLE_CONDITION%
%WHERE_CONDITION%
;
commit;
Exception
    When others then
       Rollback;
end;