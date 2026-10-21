package com.ruoyi.signing.provider.esign;

import java.io.IOException;
import java.net.*;
import javax.net.ssl.SSLSocketFactory;

/** SOCKS 连接传递原始域名，由代理端解析；TLS 仍使用 JDK 默认信任链及 HTTPS 主机名校验。 */
final class SocksRemoteDnsSocketFactory extends SSLSocketFactory {
  private final SSLSocketFactory delegate = (SSLSocketFactory) SSLSocketFactory.getDefault();
  private final Proxy proxy;
  private final String targetHost;
  private final int targetPort;
  private final int connectTimeout;
  private final int readTimeout;

  /** 绑定单个 HTTPS 目标，避免本机虚拟 DNS 地址被发送给远端 SOCKS 服务。 */
  SocksRemoteDnsSocketFactory(Proxy proxy, URL target, int connectTimeout, int readTimeout) {
    this.proxy = proxy;
    this.targetHost = target.getHost();
    this.targetPort = target.getPort() == -1 ? target.getDefaultPort() : target.getPort();
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  /** 创建待连接的代理套接字；HttpsURLConnection 随后通过带主机名的重载包装 TLS。 */
  @Override
  public Socket createSocket() throws IOException {
    Socket socket =
        new Socket(proxy) {
          @Override
          public void connect(SocketAddress endpoint, int timeout) throws IOException {
            super.connect(InetSocketAddress.createUnresolved(targetHost, targetPort), timeout);
          }
        };
    socket.setSoTimeout(readTimeout);
    return socket;
  }

  /** 使用目标域名包装 TLS，保留 SNI、证书链及 HTTPS 主机名检查。 */
  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
      throws IOException {
    return delegate.createSocket(socket, host, port, autoClose);
  }

  /** 经远端 DNS 连接并包装 TLS。 */
  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return connected(null, 0);
  }

  /** 经远端 DNS 连接，可绑定本地地址。 */
  @Override
  public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return connected(localAddress, localPort);
  }

  /** 忽略本机预解析的目标地址，始终使用本次请求的原始域名。 */
  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return connected(null, 0);
  }

  /** 保留本地绑定，目标域名仍交给代理解析。 */
  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return connected(localAddress, localPort);
  }

  /** 沿用 JDK 默认密码套件。 */
  @Override
  public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  /** 沿用 JDK 支持的密码套件。 */
  @Override
  public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
  }

  /** 创建连接失败时关闭底层套接字，防止代理资源泄漏。 */
  private Socket connected(InetAddress localAddress, int localPort) throws IOException {
    Socket socket = createSocket();
    try {
      if (localAddress != null || localPort != 0) {
        socket.bind(new InetSocketAddress(localAddress, localPort));
      }
      socket.connect(InetSocketAddress.createUnresolved(targetHost, targetPort), connectTimeout);
      return delegate.createSocket(socket, targetHost, targetPort, true);
    } catch (IOException | RuntimeException error) {
      try {
        socket.close();
      } catch (IOException closeError) {
        error.addSuppressed(closeError);
      }
      throw error;
    }
  }
}
