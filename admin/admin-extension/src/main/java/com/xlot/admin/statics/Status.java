package com.xlot.admin.statics;

import lombok.Getter;

@Getter
public enum Status {
	SUCCESS(0, "ok"), 
	USER_NOT_FOUND(1, "Không tồn tại user"), 
	INVALID_PASSWORD_LENGTH(2, "mật khẩu phải từ 6 đến 32 kí tự"), 
	WRONG_PASSWORD(3, "sai mật khẩu"), 
	UNKNOWN_ERROR(4, "lỗi không xác định"), 
	INVALID_TOKEN(5, "invalid token"), 
	DUPLICATE_PERMISSION(6, "trùng permission name"), 
	DUPLICATE_ROLE(7, "trùng tên role"), 
	DUPLICATE_USER(8, "trùng tên user"), 
	USER_NOT_LOGGED_IN(9, "user chưa đăng nhập"), 
	NOT_HAVE_PERMISSION(10, "không có quyền"), 
	USERNAME_CONTAIN_PASSWORD(11, "username chứa password hoặc ngược lại"), 
	DUPLICATE_PASSWORD(12, "trùng mật khẩu cũ"),
	COMMAND_NOT_FOUND(13, "command not found"), 
	ASSET_NOT_FOUND(14, "không tìm thấy asset"), 
	AMS_LOG_NOT_FOUND(15, "amsLog not found"), 
	MISS_PRODUCT(16, "thiếu product id ref"), 
	MISS_CP(17, "thiếu cp id ref")
	;

	private int id;
	private String message;

	private Status(int id, String message) {
		this.id = id;
		this.message = message;
	}
}
