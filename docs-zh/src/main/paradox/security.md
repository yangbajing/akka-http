# ! 安全声明 !
*! Security Announcements !*

## Receiving Security Advisories
**接收安全建议**

The best way to receive any and all security announcements is to subscribe to the [Akka security list](https://groups.google.com/forum/#!forum/akka-security).

接收任何/所有安全声明的最佳方式是订阅 [Akka 安全列表](https://groups.google.com/forum/#!forum/akka-security) 。

The mailing list is very low traffic, and receives notifications only after security reports have been managed by the core team and fixes are publicly available.

邮件列表的流量非常低，只有在核心团队管理了安全报告并公开了修复程序后才会接收通知。

## Reporting Vulnerabilities
**报告漏洞**

We strongly encourage people to report such problems to our private security mailing list first, before disclosing them in a public forum.

我们强烈建议大家先在我们的私有邮件列表里报告这些问题，然后再在公共论坛上披露。 

Following best-practice, we strongly encourage anyone to report potential security 
vulnerabilities to security@akka.io before disclosing them in a public forum like the mailing list or as a Github issue.

下列最佳实践，我们强烈建议任何人在公共论坛（如：邮件列表或 Github issue）上披露之前，报告潜在安全漏洞到 security@akka.io 。 

Reports to this email address will be handled by our security team, who will work together with you
to ensure that a fix can be provided without delay.

此邮件地址的报告将由我们的安全团队处理，他们将与你一起工作，确保可以及时提供修复。

## Fixed Security Vulnerabilities
**已修复的安全漏洞**

### Fixed in Akka HTTP 10.1.5 & 10.0.14

* @ref:[Denial of Service via unlimited decoding with decodeRequest directive ("zip bomb")](security/2018-09-05-denial-of-service-via-decodeRequest.md)

### Fixed in Akka HTTP 10.0.6 & 2.4.11.2

* @ref:[Illegal Media Range in Accept Header Causes StackOverflowError Leading to Denial of Service](security/2017-05-03-illegal-media-range-in-accept-header-causes-stackoverflowerror.md)

### Fixed in Akka HTTP 10.0.2 & 2.4.11.1

* @ref:[Denial-of-Service by stream leak on unconsumed closed connections](security/2017-01-23-denial-of-service-via-leak-on-unconsumed-closed-connections.md)

### Fixed in Akka HTTP 2.4.11

* @ref:[Directory Traversal Vulnerability Announcement](security/2016-09-30-windows-directory-traversal.md)


@@@ index

 * [2018](security/2018.md)
 * [2017](security/2017.md)
 * [2016](security/2016.md)

@@@