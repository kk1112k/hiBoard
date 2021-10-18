package com.icia.web.controller;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.icia.common.model.FileData;
import com.icia.common.util.FileUtil;
import com.icia.common.util.StringUtil;
import com.icia.web.model.HiBoard;
import com.icia.web.model.HiBoardFile;
import com.icia.web.model.Paging;
import com.icia.web.model.Response;
import com.icia.web.model.User;
import com.icia.web.service.HiBoardService;
import com.icia.web.service.UserService;
import com.icia.web.util.CookieUtil;
import com.icia.web.util.HttpUtil;
import com.icia.web.util.JsonUtil;


@Controller("hiBoardController")
public class HiBoardController 
{	
	private static Logger logger = LoggerFactory.getLogger(HiBoardController.class);
	
	@Value("#{env['auth.cookie.name']}")
	private String AUTH_COOKIE_NAME;
	
	@Value("#{env['upload.save.dir']}")
	private String UPLOAD_SAVE_DIR;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private HiBoardService hiBoardService;
	
	private static final int LIST_COUNT = 5;		//한 페이지의 게시물 수 
	private static final int PAGE_COUNT = 5;		//페이징 수
	
	@RequestMapping(value="/board/list")
	public String list(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		//조회 항목(1:작성자조회, 2:제목조회, 3:내용조회)
		String searchType = HttpUtil.get(request, "searchType");
		//조회 값
		String searchValue = HttpUtil.get(request, "searchValue", "");
		//현재 페이지 
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		//총 게시물 수
		long totalCount = 0;
		//게시물 리스트
		List<HiBoard> list = null;
		//조회 객체
		HiBoard search = new HiBoard();
		//페이징 객체
		Paging paging = null;	
		
		if(!StringUtil.isEmpty(searchType) && !StringUtil.isEmpty(searchType))
		{
			search.setSearchType(searchType);
			search.setSearchValue(searchValue);
		}
		else
		{
			searchType = "";
			searchValue = "";
		}
		
		totalCount = hiBoardService.boardListCount(search);
		
		logger.debug("totalCount : " + totalCount);
		
		if(totalCount > 0)
		{
			paging = new Paging("/board/list", totalCount, LIST_COUNT, PAGE_COUNT, curPage, "curPage");
			
			paging.addParam("searchType", searchType);
			paging.addParam("searchValue", searchValue);
			paging.addParam("curPage", curPage);
			
			search.setStartRow(paging.getStartRow());
			search.setEndRow(paging.getEndRow());
			
			list = hiBoardService.boardList(search);
		}
		
		model.addAttribute("list", list);
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		model.addAttribute("paging", paging);
		
		return "/board/list";
	}
	
	//게시물 등록 폼
	@RequestMapping(value="/board/writeForm")
	public String writeForm(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		//쿠키 값
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		//조회 항목(1:작성자 조회, 2:제목 조회, 3:내용 조회)
		String searchType = HttpUtil.get(request, "searchType", "");
		//조회 값 
		String searchValue = HttpUtil.get(request, "searchValue", "");
		//현재 페이지
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		//사용자정보 조회
		User user = userService.userSelect(cookieUserId);
		
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		model.addAttribute("user", user);
		
		return "/board/writeForm";
	}
	
	//게시물 등록 시작한다잉
	@RequestMapping(value="/board/writeProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> writeProc(MultipartHttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle", "");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent", "");
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(!StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent))
		{
			HiBoard hiBoard = new HiBoard();
			
			hiBoard.setUserId(cookieUserId);
			hiBoard.setHiBbsTitle(hiBbsTitle);
			hiBoard.setHiBbsContent(hiBbsContent);
			
			if(fileData != null && fileData.getFileSize() > 0)
			{
				HiBoardFile hiBoardFile = new HiBoardFile();
				
				hiBoardFile.setFileName(fileData.getFileName());
				hiBoardFile.setFileOrgName(fileData.getFileOrgName());
				hiBoardFile.setFileExt(fileData.getFileExt());
				hiBoardFile.setFileSize(fileData.getFileSize());
				
				hiBoard.setHiBoardFile(hiBoardFile);
			}
			
			try
			{
				if(hiBoardService.boardInsert(hiBoard) > 0)
				{
					ajaxResponse.setResponse(0, "Success");
				}
				else
				{
					ajaxResponse.setResponse(500, "Internal Server Error");
				}
			}
			catch(Exception e)
			{
				logger.error("[HiBoardController]/board/writeProc Exception", e);
				ajaxResponse.setResponse(500, "Internal Server Error");
			}
			
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		logger.debug("[HiBoardController] /board/writeProc response\n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	//게에시이무울 조오회
	@RequestMapping(value="/board/view")
	public String view(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		//쿠키 값
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		//게시물 번호
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		//조회 항목(1:작성자, 2:제목, 3:내용)
		String searchType = HttpUtil.get(request, "searchType");
		//조회값	
		String searchValue = HttpUtil.get(request, "searchValue", "");
		//현재페이지
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		//본인 글 여부
		String boardMe = "N";
		
		HiBoard hiBoard = null;
		
		if(hiBbsSeq > 0)
		{
			hiBoard = hiBoardService.boardView(hiBbsSeq);
			
			if(hiBoard != null && StringUtil.equals(hiBoard.getUserId(), cookieUserId))
			{
				boardMe = "Y";  //본인 글
			}
		}
		
		model.addAttribute("hiBbsSeq", hiBbsSeq);
		model.addAttribute("hiBoard", hiBoard);
		model.addAttribute("boardMe", boardMe);
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		
		return "/board/view";
	}
	
	//게시이물 사악제
	@RequestMapping(value="/board/delete", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> delete(HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(hiBbsSeq > 0)
		{
			HiBoard hiBoard = hiBoardService.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				if(StringUtil.equals(hiBoard.getUserId(), cookieUserId))
				{
					try
					{
						
						if(hiBoardService.boardAnswersCount(hiBoard.getHiBbsSeq()) > 0)
						{
							ajaxResponse.setResponse(-999, "Answers exist and cannot be deleted");
						}
						else
						{
							if(hiBoardService.boardDelete(hiBoard.getHiBbsSeq()) > 0)
							{
								ajaxResponse.setResponse(0, "success");
							}
							else
							{
								ajaxResponse.setResponse(500, "internal Server Error");
							}
						}
					}
					catch(Exception e)
					{
						logger.error("[HiBoardController] /board/delete Exception", e);
						ajaxResponse.setResponse(500, "internal Server Error");
					}
				}
				else
				{
					ajaxResponse.setResponse(404, "Not Found");
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
		logger.debug("[HiBoardController] /board/delete response\n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	//게시물 수정 화면
	@RequestMapping(value="/board/updateForm")
	public String updateForm(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		//쿠키값 읽어오는거 
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		//게시물 번호 읽어오기 
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		//조회항목 가져오기 (1:작성자, 2:제목, 3:내용)
		String searchType = HttpUtil.get(request, "searchType", "");
		
		//조회 값 가져오기
		String searchValue = HttpUtil.get(request, "searchValue", "");
		
		//현재 페이지 가져오기
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		HiBoard hiBoard = null;
		User user = null;
		
		if(hiBbsSeq > 0)
		{
			hiBoard = hiBoardService.boardView(hiBbsSeq);
			
			if(hiBoard != null)
			{
				if(StringUtil.equals(hiBoard.getUserId(), cookieUserId))
				{
					user = userService.userSelect(cookieUserId);
				}
				else
				{
					hiBoard = null;
				}
			}
		}
		
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		model.addAttribute("hiBoard", hiBoard);
		model.addAttribute("user", user);
		
		return "/board/updateForm";
	}
	
	//게시물 수정
	@RequestMapping(value="/board/updateProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> updateProc(MultipartHttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle", "");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent", "");
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(hiBbsSeq > 0 && !StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent))
		{
			HiBoard hiBoard = hiBoardService.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				if(StringUtil.equals(hiBoard.getUserId(), cookieUserId))
				{
					hiBoard.setHiBbsSeq(hiBbsSeq);
					hiBoard.setHiBbsTitle(hiBbsTitle);
					hiBoard.setHiBbsContent(hiBbsContent);
					
					if(fileData != null && fileData.getFileSize() > 0)
					{
						HiBoardFile hiBoardFile = new HiBoardFile();
						
						hiBoardFile.setFileName(fileData.getFileName());
						hiBoardFile.setFileOrgName(fileData.getFileOrgName());
						hiBoardFile.setFileExt(fileData.getFileExt());
						hiBoardFile.setFileSize(fileData.getFileSize());
						
						hiBoard.setHiBoardFile(hiBoardFile);
					}
				}
				
				try
				{
					if(hiBoardService.boardUpdate(hiBoard) > 0)
					{
						ajaxResponse.setResponse(0, "Success"); 
					}
					else
					{
						ajaxResponse.setResponse(500, "Internal Server Error");
					}
				}
				catch(Exception e)
				{
					logger.error("[HiBoardController] /board/updateProc Exception", e);
					ajaxResponse.setResponse(500, "Internal Server Error"); 
				}
			}		
			else
			{
				ajaxResponse.setResponse(404, "Not Found");  //본인 게시물이 아닙니다.
			}
		
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		logger.debug("[HiBoardController] /board/updateProc response\n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	//게시물 답변 폼
	@RequestMapping(value="/board/replyForm")
	public String replyForm(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		String searchType = HttpUtil.get(request, "searchType");
		String searchValue = HttpUtil.get(request, "searchValue", "");
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		HiBoard hiBoard = null;
		User user = null;
		
		if(hiBbsSeq > 0)
		{
			hiBoard = hiBoardService.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				user = userService.userSelect(cookieUserId);
			}
		}
		
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		model.addAttribute("hiBoard", hiBoard);
		model.addAttribute("user", user);
		
		return "/board/replyForm";
	}
	
	
	//게시물 답변
	@RequestMapping(value="/board/replyProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> replyProc(MultipartHttpServletRequest request, HttpServletResponse response)  //multipart는 파일첨부가 있어서라고 한다. 
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle", "");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent", "");
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		
		Response<Object> ajaxResponse = new Response<Object>();
		
		if(hiBbsSeq > 0 && !StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent))
		{
			HiBoard parentHiBoard = hiBoardService.boardSelect(hiBbsSeq);
			
			if(parentHiBoard != null)
			{
				HiBoard hiBoard = new HiBoard();
				
				hiBoard.setUserId(cookieUserId);
				hiBoard.setHiBbsTitle(hiBbsTitle);
				hiBoard.setHiBbsContent(hiBbsContent);
				hiBoard.setHiBbsGroup(parentHiBoard.getHiBbsGroup());
				hiBoard.setHiBbsOrder(parentHiBoard.getHiBbsOrder() + 1);
				hiBoard.setHiBbsIndent(parentHiBoard.getHiBbsIndent() + 1);
				hiBoard.setHiBbsParent(hiBbsSeq);
				
				if(fileData != null && fileData.getFileSize() > 0)
				{
					HiBoardFile hiBoardFile = new HiBoardFile();
					
					hiBoardFile.setFileName(fileData.getFileName());
					hiBoardFile.setFileOrgName(fileData.getFileOrgName());
					hiBoardFile.setFileExt(fileData.getFileExt());
					hiBoardFile.setFileSize(fileData.getFileSize());
					
					hiBoard.setHiBoardFile(hiBoardFile);
				}
				
				try
				{
					//try catch 구문 안에 둘러쌓여져 있는 메소드는 반드시 예외처리가 구성되어있어야 한다 .. ? 
					//그니까 바로밑에 boardReplyInsert 메소드에 들어가보면 throw로 Exception 을 던지고 있는걸 확인할 수 있다. 
					if(hiBoardService.boardReplyInsert(hiBoard) > 0)
					{
						ajaxResponse.setResponse(0, "Success");
					}
					else
					{
						ajaxResponse.setResponse(500, "Internal Server Error");
					}
				}
				catch(Exception e)
				{
					logger.error("[HiBoardController] /board/replyProc Exception", e);
					ajaxResponse.setResponse(500, "Internal Server Error"); 
				}
			}
			else
			{
				//부모글이 없을때애
				ajaxResponse.setResponse(404, "Not Found");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		logger.debug("[HiBoardController] /board/replyProc response\n" + JsonUtil.toJsonPretty(ajaxResponse));
		
		return ajaxResponse;
	}
	
	//첨부파일 다운로드
	@RequestMapping(value="/board/download")
	public ModelAndView download(HttpServletRequest request, HttpServletResponse response)
	{
		ModelAndView modelAndView = null;
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		if(hiBbsSeq > 0)
		{
			HiBoardFile hiBoardFile = hiBoardService.boardFileSelect(hiBbsSeq);
			if(hiBoardFile != null)
			{
				File file = new File(UPLOAD_SAVE_DIR + FileUtil.getFileSeparator() + hiBoardFile.getFileName());
				
				logger.debug("UPLOAD_SAVE_DIR : " + UPLOAD_SAVE_DIR);
				logger.debug("FileUtil.getFileSeparator() : " + FileUtil.getFileSeparator());
				logger.debug("hiBoardFile.getFileName() : " + hiBoardFile.getFileName());
				
				if(FileUtil.isFile(file))
				{
					modelAndView = new ModelAndView();
					
					//servlet-context.xml에 정의한 ID내용 
					modelAndView.setViewName("fileDownloadView");
					modelAndView.addObject(file);
					modelAndView.addObject("fileName", hiBoardFile.getFileOrgName());
					
					return modelAndView;
				}
			}
		}
		
		
		return modelAndView;
	}
}		








