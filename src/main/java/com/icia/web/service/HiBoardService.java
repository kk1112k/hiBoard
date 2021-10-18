package com.icia.web.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.icia.common.util.FileUtil;
import com.icia.web.dao.HiBoardDao;
import com.icia.web.model.HiBoard;
import com.icia.web.model.HiBoardFile;

@Service("hiBoardService")
public class HiBoardService  
{
	private static Logger logger = LoggerFactory.getLogger(HiBoardService.class);
	
	//파일 저장 디렉토리
	@Value("#{env['upload.save.dir']}")
	private String UPLOAD_SAVE_DIR;
	
	@Autowired
	private HiBoardDao hiBoardDao;
	
	//게시물 등록
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int boardInsert(HiBoard hiBoard) throws Exception
	{
		int count = hiBoardDao.boardInsert(hiBoard);
		
		//게시물 정상 등록 되고, 첨부파일이 있다면 첨부파일 등로오오옥
		if(count > 0 && hiBoard.getHiBoardFile() != null)
		{
			HiBoardFile hiBoardFile = hiBoard.getHiBoardFile();
			
			hiBoardFile.setHiBbsSeq(hiBoard.getHiBbsSeq());
			hiBoardFile.setFileSeq((short)1);
			
			hiBoardDao.boardFileInsert(hiBoard.getHiBoardFile());
			
		}
		
		return count;
	}
	
	//총 게시물 수 
	public long boardListCount(HiBoard hiBoard)
	{
		
		long count = 0;
		
		try
		{
			count = hiBoardDao.boardListCount(hiBoard);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardListCount Exception", e);
		}
		
		return count;
	}
	
	//게시물 리스트
	public List<HiBoard> boardList(HiBoard hiBoard)
	{
		List<HiBoard> list = null;
		
		try
		{
			list = hiBoardDao.boardList(hiBoard);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardList Exception", e);
		}
		
		return list;
	}
	
	//게시물 보기
	public HiBoard boardView(long hiBbsSeq)
	{
		HiBoard hiBoard = null;
		
		try
		{
			hiBoard = hiBoardDao.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				//조회수 증가
				hiBoardDao.boardReadCntPlus(hiBbsSeq);
				
				//첨부파일 조회
				HiBoardFile hiBoardFile = hiBoardDao.boardFileSelect(hiBbsSeq);
				
				if(hiBoardFile != null)
				{
					hiBoard.setHiBoardFile(hiBoardFile);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardView Exception", e);
		}
		
		return hiBoard;
	}
	
	//게시물 조회
	public HiBoard boardSelect(long hiBbsSeq)
	{
		HiBoard hiBoard = null;
		
		try
		{
			hiBoard = hiBoardDao.boardSelect(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardSelect Exception", e);
		}
		
		return hiBoard;
	}
	
	//게시물 삭제시 답변글 체크
	public int boardAnswersCount(long hiBbsSeq)
	{
		int count = 0;
		
		try
		{
			count = hiBoardDao.boardAnswersCount(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardAnswersCount Exception", e);
		}
		
		return count;
	}
	
	//게시물 삭제(파일이 있으면 같이 삭제)
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int boardDelete(long hiBbsSeq) throws Exception
	{
		int count = 0;
		
		HiBoard hiBoard = hiBoardDao.boardSelect(hiBbsSeq);
		
		if(hiBoard != null)
		{
			count = hiBoardDao.boardDelete(hiBbsSeq);
			
			if(count > 0)
			{
				HiBoardFile hiBoardFile = hiBoardDao.boardFileSelect(hiBbsSeq);
				
				if(hiBoardFile != null)
				{
					//테이블 삭제, 파일 삭제 동시에 !!
					if(hiBoardDao.boardFileDelete(hiBbsSeq) > 0)
					{
						FileUtil.deleteFile(UPLOAD_SAVE_DIR + FileUtil.getFileSeparator() + hiBoardFile.getFileName());
					}
				}
			}
		}
		
		return count;
	}
	
	//게시물 수정
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)	
	public int boardUpdate(HiBoard hiBoard) throws Exception	
	{
		int count = hiBoardDao.boardUpdate(hiBoard);
		
		if(count > 0 && hiBoard.getHiBoardFile() != null)
		{
			HiBoardFile delHiBoardFile = hiBoardDao.boardFileSelect(hiBoard.getHiBbsSeq());
			
			//기존파일이 있으면 삭제
			if(delHiBoardFile != null)
			{
				FileUtil.deleteFile(UPLOAD_SAVE_DIR + FileUtil.getFileSeparator() + delHiBoardFile.getFileName());
				
				hiBoardDao.boardFileDelete(hiBoard.getHiBbsSeq());
			}
			
			HiBoardFile hiBoardFile = hiBoard.getHiBoardFile();
			
			hiBoardFile.setHiBbsSeq(hiBoard.getHiBbsSeq());
			hiBoardFile.setFileSeq((short)1);
			
			hiBoardDao.boardFileInsert(hiBoardFile);
		}
		
		return count;
	}
	
	//답글 등록, 같은 그룹내 순서 업데이트
	//트랜잭셔널 어노테이션으로 한 것은 인서트 업데이트 딜리트 등은 여러 DB 움직임 이기 때문에 ??? 움직임이 아니라 딜리트 셀렉트 등 여러가지로 구성되어 있기 때문에 트랜잭셔널로 묶어줘야 한다고 한다. 
	//위으 내용 다시 알아봐야 할듯 .. ? ( 쉽게 생각하면 하나라도 실패하면 그동안 성공했던 부분 롤백해주는 개념 .. ?)
	//예를 들어 SELECT와 UPDATE 가 동시에 있을때 셀렉트는 성공하고 업데이트는 실패했을 때,  셀렉트를 다시 롤백시켜주는게 Transactional 어노테이션의 기능이다. 
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int boardReplyInsert(HiBoard hiBoard) throws Exception
	{
		hiBoardDao.boardGroupOrderUpdate(hiBoard);
		
		int count = hiBoardDao.boardReplyInsert(hiBoard);
		
		if(count > 0 && hiBoard.getHiBoardFile() != null)
		{
			HiBoardFile hiBoardFile = hiBoard.getHiBoardFile();
			
			hiBoardFile.setHiBbsSeq(hiBoard.getHiBbsSeq());
			hiBoardFile.setFileSeq((short)1);
			
			hiBoardDao.boardFileInsert(hiBoard.getHiBoardFile());;
		}
		
		return count;
	}
	
	//첨부파일조회
	public HiBoardFile boardFileSelect(long hiBbsSeq)
	{
		HiBoardFile hiBoardFile = null;
		
		try
		{
			hiBoardFile = hiBoardDao.boardFileSelect(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardFileSelect Exception", e);
		}
		
		return hiBoardFile;
	}
}






