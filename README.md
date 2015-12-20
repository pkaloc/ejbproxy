# EJBProxy

*EJBProxy* is a small library which makes your *EJB*s act as a *reverse proxy*.
As it's generally more of an anti-pattern you can employ this approach if you
(for some reason) cannot load-balance client requests nor refactor the application
layer, and still desperately need to at least little moderate load on the servers
where your enterprise apps lay.

To get it working is rather straightforward. Just grab your existing *ear* archive,
*jar* artifacts of the *runtime* sub-modules, add configuration tailored to your landscape,
and eventually put it into a specific structure and run the *create-ear* goal of the
*maven-ejbproxy-plugin* maven plugin. This will 'transform' your original app into a
reverse proxy one. You will then just swap your currently deployed app with this one,
and the original app will get deployed on as many other servers as it'll be needed.
Best if you take a look at the accompanying *ejbproxy-test* project for how-tos.

In case you won't forget to specify those servers in the ejbproxy configuration file,
the runtime will ensure logic within your EJBs won't get executed on the frontline server,
but actually on those 'backing' servers instead.

It's possible to enable proxying globally or per-ejb basis. It's also possible to set different
proxy strategies (failover, round-robin load balancing, single endpoint) also either globally or per-ejb.
You can switch on logging, too. Last but not least, it's possible to adjust many of these settings
at runtime. Either via JMX, or on a simple configuration webpage which, by default, gets embedded
by the *create-ear* goal and is accessible at */ejbproxywc* url.

*Caveat: I came up with this tiny library while working on one enterprise app with extremely convoluted back-end
side. JBoss AS 4 was in use and there wasn't any other solution available how to get this done.
Although the codebase as such is generic, it works only for jndi-bound remote beans. Moreover, it currently assumes
it's running on JBoss AS 4 as there's only JBoss4JndiGlobalNameComposer implementation of JndiGlobalNameComposer
available. Please take a look at this interface for more info.*