package com.xlot.admin.model;

import java.util.List;

import com.xlot.admin.bean.CreditBean;
import com.xlot.admin.vo.CreditQuery;

public interface CreditModel {
	int createCredit(CreditBean bean);

	int editCredit(CreditBean bean);

	int changeStatus(byte[] id, int status);

	List<CreditBean> search(CreditQuery query, int from, int size);

	int SearchCount(CreditQuery query);
}
