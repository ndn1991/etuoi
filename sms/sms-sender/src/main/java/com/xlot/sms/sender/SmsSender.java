package com.xlot.sms.sender;

import com.mario.services.sms.SmsService;
import com.nhb.common.async.RPCFuture;

public interface SmsSender extends SmsService<RPCFuture<SendSMSResult>> {

}
