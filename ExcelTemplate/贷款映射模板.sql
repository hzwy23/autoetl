create or replace procedure proc_etl_test123(
	p_as_of_date date
	,ret_msg varchar2
)
/**********************************************************************************

	功能描述：	测试模板
	作者名称：	zhanwei_huang
	创建日期：	42736
	其他备注：	无
	修改历史：	无
	电子邮箱：	hzwy23@163.com

**********************************************************************************/
as
/*************************************参数列表**************************************/
  version varchar2(5) := 'v1.0';
	imd varchar2(100) := '123';
	
begin

/*********************************************************************************
*                                       程序开始                                   *
**********************************************************************************/
execute immediate 'truncate table atomic.fsi_d_load_contracts';


/*********************************************************************************
*                                       数据加载                                   *
**********************************************************************************/
insert into atomic.fsi_d_term_deposits (
	AS_OF_DATE                                                  	--数据日期
	,ACCOUNT_NUMBER                                              	--账号
	,ORG_UNIT_ID                                                 	--机构号
	,GL_ACCOUNT_ID                                               	--科目号
	,ISO_CURRENCY_CD                                             	--币种
	,CUR_BOOK_BAL                                                	--当前余额
	,ORG_PAR_BAL                                                 	--票面余额
	,ORIGINATION_DATE                                            	--起息日期
	,MATURITY_DATE                                               	--到期日期
	,CUR_PAYMENT                                                 	--当期支付金额
	,CUR_PAR_BAL                                                 	--当前票面余额
	,LAST_PAYMENT_DATE                                           	--上次支付日
	,NEXT_PAYMENT_DATE                                           	--下次支付日
	,PMT_FREQ                                                    	--支付频率
	,PMT_FEEQ_MULT                                               	--支付频率单位
	,AMRT_TYPE_CD                                                	--支付方式
	,ORG_TERM                                                    	--原始期限
	,ORG_TERM_MULT                                               	--原始期限单位
	,AMRT_TERM                                                   	--摊还期限
	,AMRT_TERM_MULT                                              	--摊还期限单位
	,ADJUSTABLE_TYPE_CD                                          	--利率调节方式
	,REPRICE_FREQ                                                	--重定价频率
	,REPRICE_FREQ_MULT                                           	--重定价频率单位
	,LAST_REPRICE_DATE                                           	--上次重定价日
	,NEXT_REPRICE_DATE                                           	--下次重定价日
	,CUR_NET_RATE                                                	--执行利率
	,LRD_BALANCE                                                 	--上次重定余额
	,ACCRUAL_BASIC_CD                                            	--计息基础
	,PRODUCT_ID                                                  	--定价单元
	,COMMON_COA_ID                                               	--账户册编码
	,MARGIN                                                      	--利差
	,REMAIN_NO_PMTS_C                                            	--剩余支付次数
	,REMAIN_TERM_C                                               	--剩余期限
	,REMAIN_TERM_MULT_C                                          	--剩余期限单位
	,DEFERRED_CUR_BAL                                            	--当前递延余额
	,ID_NUMBER                                                   	--ID号
	,IDENTITY_VCODE                                              	--标示代码
	,COMPOUND_BASIS_CD                                           	--复利方式
	,INSTRUMENT_TYPE_CD                                          	--产品类型
	,ORG_PAYMENT_AMT                                             	--原始支付金额
)
select
	t.data_dt                                                   	 as AS_OF_DATE	--批次日期
	,t.acct_no                                                   	 as ACCOUNT_NUMBER	--借据号
	,t.enty_org_cd                                               	 as ORG_UNIT_ID	--机构编码
	,t.prim_sbjt_cd                                              	 as GL_ACCOUNT_ID	--填写科目号
	,t.ccy_cd                                                    	 as ISO_CURRENCY_CD	--填写币种编码
	,t.cur_bal                                                   	 as CUR_BOOK_BAL	--当前余额
	,t.cur_bal                                                   	 as ORG_PAR_BAL	--ofsa计算固定利率业务，现金流的起始金额，对于定期存款如果不考虑利息现金流和整存零取与零存整取的现金流，则直接填cur_book_bal即可
	,t.start_dt                                                  	 as ORIGINATION_DATE	--开户日期可能与起息日起不一样，一定要填写起息日起
	,t.matu_dt                                                   	 as MATURITY_DATE	--定期存款到期日期
	,case 
	      when t.rate_adjst_type = '0' then 0
	      when t.rate_adjust_type = '1' then 1
	 end                                                        	 as CUR_PAYMENT	--针对分期支付的业务，计算现金流时使用，如果定期存款不考虑利息现金流和整存零取与零存整取现金流，则填写01.支付方式100，400，500，600，710，800时填写当期应还本金+利息2. 当付款方式时820时，填写当期应还本金3.当还款700，802，单利时，填写0
	,t.cur_bal                                                   	 as CUR_PAR_BAL	--
	,t.start_dt                                                  	 as LAST_PAYMENT_DATE	--如果不考虑定期存款的现金流时，上次支付日与起息日相同
	,t.next_pmt_intst_dt                                         	 as NEXT_PAYMENT_DATE	--如果不考虑定期存款的现金流时，下次支付日填写到期日
	,t.instmt_pmt_intst_freq                                     	 as PMT_FREQ	--如果不考虑定期存款现金流，则支付频率用到期日减去起息日
	,t.instmt_pmt_intst_freq_mult                                	 as PMT_FEEQ_MULT	--
	,700                                                         	 as AMRT_TYPE_CD	--定期存款业务，如果不考虑利息现金流，如果不考虑整存零取和零存整取本金，统一填写700，如果需要对计算定期存款利息现金流和整存零取现金流与零存整取现金流，则酌情填写相对应的还款方式
	,t.term                                                      	 as ORG_TERM	--到期日减去起息日，ofsa固定利率现金流计算时使用
	,t.term_mult                                                 	 as ORG_TERM_MULT	--
	,t.term                                                      	 as AMRT_TERM	--如果摊还方式不是气球式，则amrt_term = org_term
	,t.term_mult                                                 	 as AMRT_TERM_MULT	--
	,t.rate_adjst_tp                                             	 as ADJUSTABLE_TYPE_CD	--资产负债使用这个字段来表示业务利率是否为固定和浮动，对于FTP而言，不使用这个字段来表示利率是否可以变化。
	,t.repricing_freq                                            	 as REPRICE_FREQ	--如果不考虑定期存款利率浮动，则重定价频率填0，FTP使用这个字段来表示业务利率是否可以变化，如果是固定利率，一定要填写0，浮动利率，填写相应的浮动频率
	,t.repricing_freq_mult                                       	 as REPRICE_FREQ_MULT	--
	,t.start_dt                                                  	 as LAST_REPRICE_DATE	--上次重定价日期，如果不考虑定期存款利率重定价，则上次重定价日填写起息日
	,t.next_repricing_date                                       	 as NEXT_REPRICE_DATE	--下次重定价日期，如果不考虑定期存款重定价，则下次重定价日期写到期日
	,t.exec_rate                                                 	 as CUR_NET_RATE	--
	,t.cur_bal                                                   	 as LRD_BALANCE	--如果不考虑定期存款利率浮动，则上次重定价日余额填写当前余额即可，FTP使用这个字段来计算浮动利率现金流计算起始金额，对于资产负债模块，不实用这个字段
	,t.acru_intst_bas                                            	 as ACCRUAL_BASIC_CD	--1:30/3602:实际/3603:实际/实际4:30/3655:30/实际6:实际/365
	,null                                                        	 as PRODUCT_ID	--根据业务规则，匹配定价单元
	,null                                                        	 as COMMON_COA_ID	--维度信息，根据业务需求填写，通常不需要
	,t.exec_rate - t.bas_rate                                    	 as MARGIN	--差值等于执行利率与基准利差的差额，FTP通常不使用这个字段进行定价
	,1                                                           	 as REMAIN_NO_PMTS_C	--计算现金流时必须填写这个字段，如果这个字段确实，可能将会导致现金流计算时出现遗漏，如果这个字段值为1，表示是最后一期的现金流
	,t.matu_dt - t.data_dt                                       	 as REMAIN_TERM_C	--到期日减去数据日期
	,D                                                           	 as REMAIN_TERM_MULT_C	--
	,0                                                           	 as DEFERRED_CUR_BAL	--cur_book_bal - cur_par_bal（差额即为递延）
	,22222                                                       	 as ID_NUMBER	--两个字段能够保证表中数据唯一性即可
	,11100                                                       	 as IDENTITY_VCODE	--
	,160                                                         	 as COMPOUND_BASIS_CD	--定期存款不考虑复利
	,210                                                         	 as INSTRUMENT_TYPE_CD	--110:商业贷款120:消费贷款130:房屋贷款140:投资141:MBS150:信用卡210:存款220:批发融资
	,0                                                           	 as ORG_PAYMENT_AMT	--如果没有使用反向摊还方式，这个字段填0即可
from app_loan_info t
	
where t.as_of_date = t1.as_of_date
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