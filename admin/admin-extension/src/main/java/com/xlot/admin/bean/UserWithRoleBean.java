package com.xlot.admin.bean;

import java.util.List;

import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserWithRoleBean extends UserBean {
	private List<String> roleNames;
	
	@Override
	public PuObject toPuObject() {
		PuObject puo = super.toPuObject();
		puo.setPuArray("roleNames", PuArrayList.fromObject(roleNames));
		return puo;
	}
}
