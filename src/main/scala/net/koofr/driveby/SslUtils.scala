package net.koofr.driveby

import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.net.ssl.KeyManager

object SslUtils {

  object BlindFaithX509TrustManager extends X509TrustManager {
    def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()
    def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()
    def getAcceptedIssuers = Array[X509Certificate]()
  }

  def sslContext(verify: Boolean): SSLContext = {
    if (verify) {
      SSLContext.getDefault()
    } else {
      val context = SSLContext.getInstance("TLS")
      context.init(Array[KeyManager](), Array(BlindFaithX509TrustManager), null)
      context
    }
  }

}
