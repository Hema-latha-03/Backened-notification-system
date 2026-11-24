package com.notifyengine.sender;

import com.notifyengine.model.Notification;
import com.notifyengine.model.VendorSubscription;

/** One implementation per vendor provider_code. Throw an exception to signal a failed attempt. */
public interface VendorSender {
    boolean supports(String providerCode);

    /** @return vendor response / message id */
    String send(Notification notification, VendorSubscription subscription) throws Exception;
}
