package hu.elte.txtuml.examples.producer_consumer;

import hu.elte.txtuml.api.deployment.Configuration;
import hu.elte.txtuml.api.deployment.Group;

import hu.elte.txtuml.examples.producer_consumer.model.Consumer;
import hu.elte.txtuml.examples.producer_consumer.model.Producer;
import hu.elte.txtuml.examples.producer_consumer.model.Storage;

@Group(contains = { Consumer.class })
@Group(contains = { Producer.class }, max = 10, gradient = 0.5)
@Group(contains = { Storage.class }, constant = 2)
public class ProducerConsumerConfiguration extends Configuration {

}