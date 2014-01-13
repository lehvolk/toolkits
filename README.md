toolkits
========

This project accumulates various usefull toolkits

toolkit.security
========

This is cryptographic toolkit.

First point is class Crypto. static methods provides access for generating 
* hash (md5, sha1, sha256)
* encrypting/decrypting data using working key
* tools for base64 and hex representation of data

Second point is class SSL. Methods of this class provides ability to create custom SSLSocketFactory based on specific SSL configuration, which can be stored in xml or plain POJO bean. 

toolkit.ws
========

This is toolkit for working with soap web services. Toolkit provides ability to create soap clients (supports only cxf and jaxws clients) for endpoint which uses ssl with server/clients certificates, host verification. 

Toolkit provides ability to pool configured client (as JDBCConnections in DataSource). This is usefull if configuring soap client is huge operation. Sometimes (but not always) pooling configured ws client shows much better performance then not creating new client on each request. 
