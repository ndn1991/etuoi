package com.xlot.admin.sms;

import com.mario.services.sms.SmsService;
import com.nhb.common.async.RPCFuture;

public interface SmsSender extends SmsService<RPCFuture<SendSMSResult>> {

}
