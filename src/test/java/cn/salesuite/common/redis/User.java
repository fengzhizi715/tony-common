/**
 * 
 */
package cn.salesuite.common.redis;

import java.io.Serializable;

/**
 * @author Tony Shen
 * 
 */
public class User implements Serializable {

	private static final long serialVersionUID = 6087091965377201739L;

	private String userName;
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;

		User other = (User) obj;
		if (!this.getUserName().equals(other.getUserName())
				|| !this.getPassword().equals(other.getPassword()))
			return false;
		return true;
	}

}
