create or replace procedure proc_app_ledger(
	p_as_of_date in date
	,ret_flag out varchar2
	,ret_msg out varchar2
)
/**********************************************************************************

	作者名称：	zhanwei_huang
	电子邮箱：	zhanwei_huang@vprisk.com
	创建日期：	2018-01-18
	功能描述：	将总账业务数据加载到FTP系统总账接口表中
	修改历史：	无
	其他备注：	无

**********************************************************************************/
as
/*************************************参数列表**************************************/
  version varchar2(5) := 'v1.0';

begin

/*********************************************************************************
*                                       程序开始                                   *
**********************************************************************************/
execute immediate 'truncate table FTP_PD_GL';


/*********************************************************************************
*                                       数据加载                                   *
**********************************************************************************/
insert into FTP_PD_GL (
	UUID                                                        	--UUID
	,data_date                                                   	--数据日期
	,ORGAN_CODE                                                  	--机构编号
	,SUBJECT_CODE                                                	--科目编号
	,CURRENCY_CODE                                               	--币种编号
	,gl_bal                                                      	--总账余额
)
select
	sys_guid()                                                  	 as UUID	--
	,t.as_of_date                                                	 as data_date	--
	,t.org_unit_id                                               	 as ORGAN_CODE	--
	,t.gl_account_id                                             	 as SUBJECT_CODE	--
	,t.iso_currency_cd                                           	 as CURRENCY_CODE	--
	,case substr(t.gl_account_id,0,1) 
	     when '1'  then t.debit_balance - t.creadit_balance
	     when '2'  then t.creadit_balance - t.debit_balance 
	 end
	                                                            	 as gl_bal	--资产余额在借方，负债余额在贷方。默认情况下，只取资产负债总账数据到产品
from mas_fin_investment_info t
	
where t.as_of_date = p_as_of_date  and  substr(t.gl_account_id,0,1) in ('1','2')
;
commit;

/*********************************************************************************
*                                       程序尾部                                   *
**********************************************************************************/



/*********************************************************************************
*                                        异常部分                                  *
**********************************************************************************/
-- no exception handle

end;