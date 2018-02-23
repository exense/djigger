package io.djigger.collector.server.conf.mixin;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.CLASS)
public abstract class InstrumentSubscriptionMixin {

}
