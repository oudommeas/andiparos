# Why didn't you integrate your changes into Paros Proxy? #

We asked Chinotec to integrate our changes into Paros Proxy, but the request was rejected. Therefore, we created a fork of the software.


# Is there any documentation? #

We are sorry, there is no documentation of Andiparos yet.


# Does Andiparos offer full smartcard support? #

To be honest, smartcard support has only been tested on Windows systems, yet. Smartcard support on different systems than Windows was not a requirement when the feature was developed. We would enjoy getting feedback about your testing results.

# Why is PKCS11 support not working on 64bit Windows? #

At the Java does not support PKCS11 on 64bit Windows systems due to the lack of suitable PKCS11 libraries (See also [Java PKCS#11 Reference Guide](http://download-llnw.oracle.com/javase/6/docs/technotes/guides/security/p11guide.html#Requirements)). Therefore, Andiparos is not able to handle PKCS11 on 64bit Windows systems. You might want to run Andiparos on Linux, Mac OS X or in a 32-bit Windows virtual machine.