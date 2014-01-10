package ru.lehvolk.toolkit.security.http;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * SSL configuration for external interactors
 */
@XmlRootElement(name = "ssl-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SSLConfiguration implements Serializable {

	private static final long serialVersionUID = -6215141619368971092L;

	@XmlElement(name = "key-store-path", required = false)
	private String keyStorePath;

	@XmlElement(name = "key-store-password", required = false)
	private String keyStorePassword;

	@XmlElement(name = "trust-store-path", required = false)
	private String trustStorePath;

	@XmlElement(name = "trust-store-password", required = false)
	private String trustStorePassword;

	@XmlElement(name = "enabled", required = false)
	private Boolean enabled = false;

	@XmlElement(name = "verify-host", required = false)
	private Boolean verifyHost = false;

	@XmlElement(name = "check-host-trusted", required = false)
	private Boolean checkHostTrusted = false;

	@XmlElement(name = "forced-alias", required = false)
	private String forcedAlias;

	public Boolean getCheckHostTrusted() {
		return checkHostTrusted;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public String getForcedAlias() {
		return forcedAlias;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public String getTrustStorePath() {
		return trustStorePath;
	}

	public Boolean getVerifyHost() {
		return verifyHost;
	}

	public void setCheckHostTrusted(Boolean checkHostTrusted) {
		this.checkHostTrusted = checkHostTrusted;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setForcedAlias(String forcedAlias) {
		this.forcedAlias = forcedAlias;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public void setVerifyHost(Boolean verifyHost) {
		this.verifyHost = verifyHost;
	}
}
