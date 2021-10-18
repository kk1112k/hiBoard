/**
 * <pre>
 * 프로젝트명 : HiBoard
 * 패키지명   : com.icia.web.dao
 * 파일명     : UserDao.java
 * 작성일     : 2021. 1. 19.
 * 작성자     : daekk
 * </pre>
 */
package com.icia.web.dao;

import org.springframework.stereotype.Repository;

import com.icia.web.model.User;

/**
 * <pre>
 * 패키지명   : com.icia.web.dao
 * 파일명     : UserDao.java
 * 작성일     : 2021. 1. 19.
 * 작성자     : daekk
 * 설명       : 사용자 DAO
 * </pre>
 */
@Repository("userDao")
public interface UserDao
{
	/**
	 * <pre>
	 * 메소드명   : userInsert
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       : 사용자 등록
	 * </pre>
	 * @param user com.icia.web.model.User
	 * @return int
	 */
	public int userInsert(User user);
	
	/**
	 * <pre>
	 * 메소드명   : userUpdate
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       : 사용자 수정
	 * </pre>
	 * @param user com.icia.web.model.User
	 * @return int
	 */
	public int userUpdate(User user);
	
	/**
	 * <pre>
	 * 메소드명   : userStatusUpdate
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       : 사용자 상태 변경
	 * </pre>
	 * @param user com.icia.web.model.User
	 * @return int
	 */
	public int userStatusUpdate(User user);
	
	/**
	 * <pre>
	 * 메소드명   : userSelect
	 * 작성일     : 2021. 1. 12.
	 * 작성자     : daekk
	 * 설명       : 사용자 조회
	 * </pre>
	 * @param userId 사용자 아이디
	 * @return  com.icia.web.model.User
	 */
	public User userSelect(String userId);
}
