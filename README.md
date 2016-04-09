# djigger

djigger is a production-ready performance analysis and monitoring solution for java applications which relies primarily on an advanced round-the-clock sampler and an agent mode for instrumentation. The client contains all the means necessary to connect to a target JVM (standard JMX connection capability, process attach, a jstack output parser, etc). Store mode is a direct connection to the mongoDB instance used to persist the samples taken by our headless collector. TTL functionality at the threaddump collection level allows the user to monitor his java apps 24/7 without having to worry about running out of space.

Find out more at http://denkbar.io/tooling/djigger, and read ourgithub  wiki to get started !




[![StackShare](http://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](http://stackshare.io/denkbar-io/denkbar-io)
