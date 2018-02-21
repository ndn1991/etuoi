package com.xlot.admin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nhb.common.vo.ByteArrayWrapper;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.model.UserModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserCache {
	private final UserModel userModel;
	private final Cache<ByteArrayWrapper, String> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

	public String getHashKey(byte[] userId) {
		ByteArrayWrapper userIdWrapper = new ByteArrayWrapper(userId);
		String rs = cache.getIfPresent(userIdWrapper);
		if (rs != null) {
			return rs;
		}
		UserBean user = userModel.loadById(userId);
		if (user != null) {
			cache.put(userIdWrapper, user.getHashKey());
			return user.getHashKey();
		}
		return null;
	}
	
	public void invalidate(byte[] userId) {
		cache.invalidate(new ByteArrayWrapper(userId));
	}
}
