/**
 * <pre>
 * 프로젝트명 : BasicBoard
 * 패키지명   : com.icia.web.model
 * 파일명     : User.java
 * 작성일     : 2021. 1. 12.
 * 작성자     : daekk
 * </pre>
 */
package com.icia.web.model;

import java.io.Serializable;

/**
 * <pre>
 * 패키지명   : com.icia.web.model
 * 파일명     : User.java
 * 작성일     : 2021. 1. 12.
 * 작성자     : daekk
 * 설명       : 사용자 모델
 * </pre>
 */
public class User implements Serializable
{
	private static final long serialVersionUID = 8638989512396268543L;
	
	private String userId;    // 사용자 아이디
	private String userPwd;   // 비밀번호
	private String userName;  // 사용자 명
	private String userEmail; // 사용자 이메일 
	private String status;    // 상태 ("Y":사용, "N":정지)
	private String regDate;   // 등록일
	
	/**
	 * 생성자 
	 */
	public User()
	{
		userId = "";
		userPwd = "";
		userName = "";
		userEmail = "";
		status = "";
		regDate = "";
	}

	/**
	 * <pre>
	 * 메소드명   : getUserId
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @return 
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * <pre>
	 * 메소드명   : setUserId
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @param userId
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * <pre>
	 * 메소드명   : getUserPwd
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @return 
	 */
	public String getUserPwd()
	{
		return userPwd;
	}

	/**
	 * <pre>
	 * 메소드명   : setUserPwd
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @param userPwd
	 */
	public void setUserPwd(String userPwd)
	{
		this.userPwd = userPwd;
	}

	/**
	 * <pre>
	 * 메소드명   : getUserName
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @return 
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * <pre>
	 * 메소드명   : setUserName
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @param userName
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	/**
	 * <pre>
	 * 메소드명   : getUserEmail
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @return 
	 */
	public String getUserEmail()
	{
		return userEmail;
	}

	/**
	 * <pre>
	 * 메소드명   : setUserEmail
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @param userEmail
	 */
	public void setUserEmail(String userEmail)
	{
		this.userEmail = userEmail;
	}

	/**
	 * <pre>
	 * 메소드명   : getStatus
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @return 
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * <pre>
	 * 메소드명   : setStatus
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @param status
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * <pre>
	 * 메소드명   : getRegDate
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @return 
	 */
	public String getRegDate()
	{
		return regDate;
	}

	/**
	 * <pre>
	 * 메소드명   : setRegDate
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       :
	 * </pre>
	 * @param regDate
	 */
	public void setRegDate(String regDate)
	{
		this.regDate = regDate;
	}
}
