toolkits
========

This project accumulates various usefull toolkits

toolkit.security
========

This is cryptographic toolkit.

Class Crypto with static methods provides access for: 
* hash (md5, sha1, sha256)
* encrypting/decrypting data using working key
* tools for base64 and hex representation of data

Class SSL provides ability to create custom SSLSocketFactory based on specific SSL configuration, which can be stored in xml or plain POJO bean. 

Last one is PGP util.

toolkit.ws
========

This is toolkit for working with soap web services. Toolkit provides ability to create soap clients (only cxf and jaxws clients). Toolkit supports functionality to use ssl with server/clients certificates and host verification. 

Toolkit provides ability to pool configured client (as JDBCConnections in DataSource). This is usefull if configuring ws client is huge operation. WS client's is not thread safe so application can't use one instance of client for all callers. Sometimes (but not always) pooling configured client shows much better performance then creating new client per request.
