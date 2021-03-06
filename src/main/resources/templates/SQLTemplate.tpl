create or replace procedure %PROC_NAME%(
%ARGUMENT%
)
/**********************************************************************************
%PROC_COMMENTS%
**********************************************************************************/
as
/*************************************参数列表**************************************/
  version varchar2(5) := 'v1.0';
%PROC_VARIABLE%
begin

/*********************************************************************************
*                                       程序开始                                   *
**********************************************************************************/
%PROC_HEADER%


/*********************************************************************************
*                                       数据加载                                   *
**********************************************************************************/
insert into %TARGET_TABLE% (
%TARGET_COLUMNS%
) %WITH_VIEWS%
select
%EXPRESSION_COLUMNS%
from %MAIN_TABLE% %MAIN_TABLE_ALIAS% %SUB_TABLE_CONDITION% %WHERE_CONDITION%
;
commit;

/*********************************************************************************
*                                       程序尾部                                   *
**********************************************************************************/
%PROC_FOOTER%


/*********************************************************************************
*                                        异常部分                                  *
**********************************************************************************/
%PROC_EXCEPTION%

end;