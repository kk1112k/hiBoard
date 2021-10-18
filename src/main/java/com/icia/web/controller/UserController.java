package com.icia.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.icia.common.util.StringUtil;
import com.icia.web.model.Response;
import com.icia.web.model.User;
import com.icia.web.service.UserService;
import com.icia.web.util.CookieUtil;
import com.icia.web.util.HttpUtil;
import com.icia.web.util.JsonUtil;

@Controller("userController")
public class UserController {
	private static Logger logger = LoggerFactory.getLogger(IndexController.class);
	
	//쿠키명
	@Value("#{env['auth.cookie.name']}")
	private String AUTH_COOKIE_NAME;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/user/login", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> login(HttpServletRequest request, HttpServletResponse response)
	{
		String userId = HttpUtil.get(request, "userId");
		String userPwd = HttpUtil.get(request, "userPwd");
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userPwd))
		{
			User user = userService.userSelect(userId);
			
			if(user != null)
			{
				if(StringUtil.equals(user.getUserPwd(), userPwd))
				{
					CookieUtil.addCookie(response, "/", -1, AUTH_COOKIE_NAME, CookieUtil.stringToHex(userId));
					
					ajaxResponse.setResponse(0, "Success");
				}
				else
				{
					ajaxResponse.setResponse(-1, "비밀번호 불일치");
				}
			}
			else
			{
				ajaxResponse.setResponse(404, "Not Found");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		logger.debug("[UserController] /user/login response \n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	//회원가입 폼
	@RequestMapping(value="/user/regForm", method=RequestMethod.GET)
	public String regForm(HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		if(!StringUtil.isEmpty(cookieUserId))
		{
			CookieUtil.deleteCookie(request, response, AUTH_COOKIE_NAME);
			
			return "redirect:/";
		}
		else
		{
			return "/user/regForm";
		}
	}
	
	//아이디 중복체크
	@RequestMapping(value="/user/idCheck", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> idCheck(HttpServletRequest request, HttpServletResponse response)
	{
		String userId = HttpUtil.get(request, "userId");
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(!StringUtil.isEmpty(userId))
		{
			if(userService.userSelect(userId) == null)
			{
				//사용 가능 아이디인 경우
				ajaxResponse.setResponse(0, "Success");
			}
			else
			{
				ajaxResponse.setResponse(100, "Duplicate ID");
			}
				
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		logger.debug("[UserController] /user/idCheck response \n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	// 회원 등록
	@RequestMapping(value="/user/regProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> regProc(HttpServletRequest request, HttpServletResponse response)
	{
		String userId = HttpUtil.get(request, "userId");
		String userPwd = HttpUtil.get(request, "userPwd");
		String userName = HttpUtil.get(request, "userName");
		String userEmail = HttpUtil.get(request, "userEmail");
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userPwd) &&
				!StringUtil.isEmpty(userName) && !StringUtil.isEmpty(userEmail))
		{
			if(userService.userSelect(userId) == null)
			{
				User user = new User();
				
				user.setUserId(userId);
				user.setUserName(userName);
				user.setUserPwd(userPwd);
				user.setUserEmail(userEmail);
				user.setStatus("Y");
				
				if(userService.userInsert(user) > 0)
				{
					ajaxResponse.setResponse(0, "Success");
				}
				else
				{
					ajaxResponse.setResponse(500, "Internal Server Error");
				}
			}
			else
			{
				ajaxResponse.setResponse(100, "Duplicate ID");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		logger.debug("[UserController] /user/regProc response \n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	// 로그아웃 !! 
	@RequestMapping(value="/user/loginOut", method=RequestMethod.GET)
	public String loginOut(HttpServletRequest request, HttpServletResponse response)
	{
		if(CookieUtil.getCookie(request, AUTH_COOKIE_NAME) != null)
		{
			CookieUtil.deleteCookie(request, response, "/", AUTH_COOKIE_NAME);
		}
		
		return "redirect:/";
	}
	
	// 회원정보 수정
	@RequestMapping(value="/user/updateForm", method=RequestMethod.GET)
	public String updateForm(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		String CookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		
		User user = userService.userSelect(CookieUserId);
		model.addAttribute("user", user);
		
		return "/user/updateForm";
	}
	
	//회원정보 수정 반영
	@RequestMapping(value="/user/updateProc", method = RequestMethod.POST)
	@ResponseBody
	public Response<Object> updateProc(HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		String userPwd = HttpUtil.get(request, "userPwd");
		String userName = HttpUtil.get(request, "userName");
		String userEmail = HttpUtil.get(request, "userEmail");
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(!StringUtil.isEmpty(cookieUserId))
		{
			User user = userService.userSelect(cookieUserId);
			
			if(user != null)
			{
				if(StringUtil.equals(user.getStatus(), "Y"))
				{
					if(!StringUtil.isEmpty(userPwd) && !StringUtil.isEmpty(userName) && !StringUtil.isEmpty(userEmail))
					{
						user.setUserPwd(userPwd);
						user.setUserName(userName);
						user.setUserEmail(userEmail);
						
						if(userService.userUpdate(user) > 0)
						{
							ajaxResponse.setResponse(0, "success");
						}
						else
						{
							ajaxResponse.setResponse(500, "Internal server Error");
						}
					}
				}
				else
				{
					CookieUtil.deleteCookie(request, response, AUTH_COOKIE_NAME);
					ajaxResponse.setResponse(400, "Bad Request");
				}
			}
			else
			{
				CookieUtil.deleteCookie(request, response, AUTH_COOKIE_NAME);
				ajaxResponse.setResponse(404, "Bad Request");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		return ajaxResponse;
	}
}
