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
execute immediate 'truncate table mas_fin_payment_current ';


/*********************************************************************************
*                                       数据加载                                   *
**********************************************************************************/
insert into mas_fin_payment_current  (
	account_number                                              	--账号
	,next_payment_date                                           	--下次支付日期
	,caldays                                                     	--计息天数
	,calrate                                                     	--执行利率
	,cur_payment                                                 	--支付本息
	,cur_payment_principal                                       	--支付本金
	,cur_payment_interest                                        	--支付利息
	,cur_book_bal                                                	--剩余本金
) 
with vt_ext_abcd as (
  select
	1                                                           	 as account_number	--放款编码
	,2                                                           	 as next_payment_date	--还款日期
	,3                                                           	 as caldays	--计息天数
	,4                                                           	 as calrate	--执行利率
	,5                                                           	 as cur_payment	--支付本息
	,6                                                           	 as cur_payment_principal	--支付本金
	,7                                                           	 as cur_payment_interest	--支付利息
	,8                                                           	 as cur_book_bal	--剩余本金
	,9                                                           	 as latest_payment_date 	--数据日期最近还款日
  from mas_fin_payment_schedule t
  where  to_date(t.payplandate,'YYYY-MM-DD') > p_as_of_date
), vt_ext_fin_payment_schedule as (
  select
	regitid                                                     	 as regitid	--放款编码
	,payplandate                                                 	 as payplandate	--还款日期
	,caldays                                                     	 as caldays	--计息天数
	,calrate                                                     	 as calrate	--执行利率
	,planprinintersum                                            	 as planprinintersum   	--支付本息
	,planprincipal                                               	 as planprincipal	--支付本金
	,planinterest                                                	 as planinterest 	--支付利息
	,planprincipal                                               	 as planprincipal	--剩余本金
	,payplandate                                                 	 as payplandate	--数据日期最近还款日
  from ext_fin_payment_schedule t
	inner join  vt_ext_abcd  s
		on t.regitid = s.regitid
  where  to_date(t.payplandate,'YYYY-MM-DD') > p_as_of_date
), vt_etl_mas_fin_payment_schedule as (
  select
	regitid                                                     	 as account_number	--放款编码
	,to_date(t.payplandate ,'YYYY-MM-DD')                        	 as next_payment_date	--还款日期
	,caldays                                                     	 as caldays	--计息天数
	,calrate                                                     	 as calrate	--执行利率
	,t.planprinintersum                                          	 as cur_payment	--支付本息
	,planprincipal                                               	 as cur_payment_principal	--支付本金
	,t.planinterest                                              	 as cur_payment_interest	--支付利息
	,sum(t.planprincipal)  
	     over(partition by t.regitid order by regitid desc)     	 as cur_book_bal	--剩余本金
	,min(to_date(t.payplandate,'YYYY-MM-DD'))  over(    
	     partition by  t.regitid 
	     order by to_date(t.payplandate,'YYYY-MM-DD') 
	     asc    
	 )                                                          	 as latest_payment_date 	--数据日期最近还款日
  from vt_ext_fin_payment_schedule t
  where  to_date(t.payplandate,'YYYY-MM-DD') > p_as_of_date
)
select
	account_number                                              	 as account_number	--账号
	,next_payment_date                                           	 as next_payment_date	--下次支付日期
	,caldays                                                     	 as caldays	--计息天数
	,calrate                                                     	 as calrate	--执行利率
	,cur_payment                                                 	 as cur_payment	--支付本息
	,cur_payment_principal                                       	 as cur_payment_principal	--支付本金
	,cur_payment_interest                                        	 as cur_payment_interest	--支付利息
	,cur_book_bal                                                	 as cur_book_bal	--剩余本金
from vt_etl_mas_fin_payment_schedule t  
where  t.next_payment_date = t.latest_payment_date
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