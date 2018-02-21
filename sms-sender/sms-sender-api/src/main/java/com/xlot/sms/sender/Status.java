package com.xlot.sms.sender;

public interface Status {
	int SENT = -1;
	int TIMEOUT = -2;
	int OK = 0;
	int REQUEST_TOO_FAST = 1;
}
