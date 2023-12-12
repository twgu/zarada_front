package com.dai.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dai.common.util.Aria;

import com.dai.common.CommonMessageSource;
import com.dai.common.Constants;
import com.dai.service.GrowtropinService;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.TransferManager;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;
import com.microsoft.rest.v2.RestException;

import egovframework.rte.fdl.property.EgovPropertyService;
import io.reactivex.Single;

@Controller
public class GrowtropinController {
	private final Logger logger = LogManager.getLogger(this.getClass());

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	/** EgovMessageSource */
	@Resource(name = "cmmnMessageSource")
	CommonMessageSource messageSource;

	/* Service */
	@Resource(name = "GrowtropinService")
	GrowtropinService GrowtropinService;

	String zip;
	String addr;
	String userPhone;
	String userPhoneVal;
	String userName;
	String randomResult;

	// 나이스 정보 API 배열 전역번수
	List<Map<String, Object>> nice = new ArrayList<>();
	// 다음주소 API 배열 전역번수
	List<Map<String, Object>> daum = new ArrayList<>();

	// 회원정보 관리 함수
	@RequestMapping(value = "Join_User_C", method = { RequestMethod.POST })
	@ResponseBody
	public String insertUser(
			HttpServletRequest request,
			@RequestBody Map<String, Object> args)
			throws Exception {

		logger.debug(args);

		logger.debug("mode : " + args.get("mode"));

		// 결과 변수 선언
		Map<String, Object> result = null;

		// 유저코드가 없는 경우
		if (args.get("USER_CO") == null || args.get("USER_CO") == "") {

			// key값 조회
			Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
			// key값 대입
			String Key = selectkey.get("encryptkey").toString();

			// key 값으로 암복호화 모듈 생성
			Aria aria = new Aria(Key);
			// 암호화 하여 변수에 추가
			String EnuserPhone = aria.Encrypt((String) args.get("userPhone"));
			args.put("EnuserPhone", EnuserPhone);

			String EnuserAddress = aria.Encrypt((String) args.get("userAddress"));
			args.put("EnuserAddress", EnuserAddress);

			// 회원가입의 경우만 PW드 암호화
			if (args.get("mode").equals("signup")) {
				String Enpassword = aria.Encrypt((String) args.get("password"));
				args.put("Enpassword", Enpassword);
			}

			// 회원가입구분 signup 최초회원가입, idcreate ID생성, tmpagree 임시저장 , agree 개인정보동의
			// 회원가입이 아닌경우는 관리관호사 세션정보생성
			if (!args.get("mode").equals("signup")) {

				if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
					logger.debug("insertUser user_co : " + args.get("user_co"));
				} else {
					HttpSession session = request.getSession(true);
					args.put("EDU_NRS", session.getAttribute(Constants.SESSION_USERCO));
				}

				if (args.get("EDU_NRS") == null || args.get("EDU_NRS") == "") {

					System.out.println("세션값이 없습니다.");
					return "실패";
				}

				logger.debug("agree : " + args);

			}

			// 유저 코드 생성
			result = GrowtropinService.MAX_USER_CO();

			logger.debug("USER_CO : " + result.get("userCo".toString()));

			args.put("USER_CO", result.get("userCo".toString()));

			// USER 정보 입력
			GrowtropinService.insertUser(args);

			result = null;
			// 자년 코드 생성
			result = GrowtropinService.MAX_CHL_CO(args);

			logger.debug("result : " + result);

			logger.debug("CHL_CO : " + result.get("chlCo".toString()));

			args.put("CHL_CO", result.get("chlCo".toString()));

			// 자년 정보 입력
			GrowtropinService.insertUser_ch(args);

			result = null;
			// 임시저장의 경우 개인정보 동의 내역 저장하지 않으며 최종 개인정보 동의 시 생성
			if (!args.get("mode").equals("tmpagree")) {
				// 개인정보동의 코드 생성
				result = GrowtropinService.MAX_PINFO_CO(args);

				logger.debug("result : " + result);

				logger.debug("PINFO_CO : " + result.get("pinfoCo".toString()));

				args.put("PINFO_CO", result.get("pinfoCo".toString()));

				// 개인정보동의 정보 입력
				GrowtropinService.insertUser_pinfo(args);

				System.out.println("args : " + args);

				// crm인터페이스정보 입력
				GrowtropinService.crm_interface(args);
			}

		}
		// 생성된 유저코드 리턴
		return args.get("USER_CO").toString();
	}

	// 임시저장 후 최종 개인정보 동의 함수
	@RequestMapping(value = "Join_User_A", method = { RequestMethod.POST })
	@ResponseBody
	public String Join_User_A(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		// 결과변수
		Map<String, Object> result = null;

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화
		String EnuserPhone = aria.Encrypt((String) args.get("userPhone"));
		args.put("EnuserPhone", EnuserPhone);

		String EnuserAddress = aria.Encrypt((String) args.get("userAddress"));
		args.put("EnuserAddress", EnuserAddress);

		args.put("USER_CO", args.get("tmpselectuser"));

		logger.debug(args);

		// 개인정보동의내용의거 user정보 수정
		GrowtropinService.AgreeUser_U(args);

		// 개인정보동의 코드 생성
		result = GrowtropinService.MAX_PINFO_CO(args);

		logger.debug("result : " + result);

		logger.debug("PINFO_CO : " + result.get("pinfoCo".toString()));

		args.put("PINFO_CO", result.get("pinfoCo".toString()));

		// 개인정보동의 정보 입력
		GrowtropinService.insertUser_pinfo(args);

		System.out.println("args : " + args);

		// crm인터페이스정보 입력
		GrowtropinService.crm_interface(args);

		// 유저코드 결과 반환
		return args.get("USER_CO").toString();

	}

	// 자녀추가 함수
	@RequestMapping(value = "addChildren", method = { RequestMethod.POST })
	@ResponseBody
	public void addChildren(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		// 자녀코드 생성을 위한 변수
		Map<String, Object> result = null;

		// 자녀코드 MAX+1 값 조회
		result = GrowtropinService.MAX_CHL_CO(args);

		logger.debug("result : " + result);

		logger.debug("CHL_CO : " + result.get("chlCo".toString()));

		// 자녀코드 변수에 추가
		args.put("CHL_CO", result.get("chlCo".toString()));

		// 자년 정보 입력
		GrowtropinService.insertUser_ch(args);

	}

	// 임시저장 회원 정보 수정
	@RequestMapping(value = "Tmpuser_U", method = { RequestMethod.POST })
	@ResponseBody
	public String Tmpuser_U(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화
		String EnuserPhone = aria.Encrypt((String) args.get("userPhone"));
		args.put("EnuserPhone", EnuserPhone);

		String EnuserAddress = aria.Encrypt((String) args.get("userAddress"));
		args.put("EnuserAddress", EnuserAddress);

		// 임시저장 유저코드를 유저코드로 변수에 저장
		args.put("USER_CO", args.get("tmpselectuser"));

		logger.debug(args);

		// 개인정보동의내용의거 user정보 수정
		GrowtropinService.Tmpuser_U(args);

		// 유저코드 리턴
		return args.get("USER_CO").toString();

	}

	// 회원정보 수정
	@RequestMapping(value = "memberModify", method = { RequestMethod.POST })
	@ResponseBody
	public void memberModify(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화
		String EnuserPhone = aria.Encrypt((String) args.get("userPhone"));
		args.put("EnuserPhone", EnuserPhone);

		String EnuserAddress = aria.Encrypt((String) args.get("userAddress"));
		args.put("EnuserAddress", EnuserAddress);

		logger.debug(args);

		// 부모정보 수정
		GrowtropinService.memberModify(args);

		// 자녀정보 LSIT 변수 생성
		List<Map<String, Object>> childs = (List<Map<String, Object>>) args.get("childs");

		// 자녀가 1명일경우
		if (childs.size() == 1) {
			logger.debug("childs :" + childs.get(0));
			// 자녀정보 수정
			GrowtropinService.memberModify_Ch(childs.get(0));
			// 다자녀의 경우
		} else {
			for (int i = 0, n = childs.size(); i < n; i++) {
				logger.debug("childs :" + childs.get(i));
				// 자녀별 정보수정
				GrowtropinService.memberModify_Ch(childs.get(i));
			}
		}

	}

	// 확인 환자 취소 함수
	@RequestMapping(value = "memberCancel", method = { RequestMethod.POST })
	@ResponseBody
	public void memberCancel(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		logger.debug(args);
		// 확인 환자 상태를 미확인 생태로 변경 홤수
		GrowtropinService.memberCancel(args);

	}

	// 개인정보동의 후 -> ID생성 시 함수
	@RequestMapping(value = "Join_Idcreate", method = { RequestMethod.POST })
	@ResponseBody
	public void Join_Idcreate(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		logger.debug(args);
		logger.debug("mode : " + args.get("mode"));

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String EnuserPhone = aria.Encrypt((String) args.get("userPhone"));
		args.put("EnuserPhone", EnuserPhone);

		String EnuserAddress = aria.Encrypt((String) args.get("userAddress"));
		args.put("EnuserAddress", EnuserAddress);

		String Enpassword = aria.Encrypt((String) args.get("password"));
		args.put("Enpassword", Enpassword);

		// 회원정보ID UPDATE
		GrowtropinService.Join_Idcreate(args);

	}

	@RequestMapping(value = "INJ_MSG", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> INJ_MSG(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("INJ_MSG user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		Map<String, Object> result = GrowtropinService.INJ_MSG_SELECT(args);

		logger.debug(args);
		return GrowtropinService.INJ_MSG_SELECT(args);
		// return result;
	}

	@RequestMapping(value = "CHL_INJ_HIS", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> CHL_INJ_HIS(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("CHL_INJ_HIS user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.CHL_INJ_HIS_Select(args);
	}

	@RequestMapping(value = "Insert_INJ_Save", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> Insert_INJ_Save(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("Insert_INJ_Save user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		// args.put("user_co",
		// Float.parseFloat(args.get("user_co").toString()));
		// args.put("chl_co", Float.parseFloat(args.get("chl_co").toString()));
		// args.put("ymd", Float.parseFloat(args.get("ymd").toString()));
		// args.put("inj_co", Float.parseFloat(args.get("inj_co").toString()));
		// args.put("stat", Float.parseFloat(args.get("stat").toString()));

		logger.debug(args);
		GrowtropinService.Insert_inj(args);
		// GrowtropinService.INJ_MSG_SELECT_YMD(args);

		return GrowtropinService.INJ_MSG_SELECT_YMD(args);

	}

	// 자동로그인,아이디저장 처리 함수
	@RequestMapping(value = "autoFlagmethod", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> autoFlagmethod(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		List<Map<String, Object>> result = GrowtropinService.autoFlagme(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		Map<String, Object> mapResult;
		for (int i = 0; i < result.size(); i++) {
			mapResult = result.get(i);

			mapResult.put("pw", aria.Decrypt(mapResult.get("pw").toString()));

			result.set(i, mapResult);
		}

		return result;

	}

	// 로그인 처리 함수
	@RequestMapping(value = "Login", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> Login(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		// 세션 사용
		HttpSession session = request.getSession(true);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String Enpassword = aria.Encrypt((String) args.get("password"));
		args.put("Enpassword", Enpassword);

		// ID PW로 해당회원조회
		Map<String, Object> result = GrowtropinService.LoginSelect(args);

		// ID/PW가 맞지않는 경우
		if (result == null) {

			// ID만가지고 회원정보가 있는지 검색
			return GrowtropinService.LoginSelectId(args);

			// 회원정보가 일치하는경우
		} else {

			if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
				args.put("user_co", result.get("userCo"));
				args.put("user_stat", result.get("userStat")); // 승인

			} else {
				// 세션정보생성
				session.setAttribute(Constants.SESSION_USERCO, result.get("userCo"));
				session.setAttribute(Constants.SESSION_USERID, result.get("id"));
				session.setAttribute(Constants.SESSION_USERNA, result.get("userNa"));
				session.setAttribute(Constants.SESSION_USERCL, result.get("userCl"));
				session.setAttribute(Constants.SESSION_USERST, result.get("userStat"));

				// 세션 로그를 위한 변수 추가
				args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
				args.put("user_stat", session.getAttribute(Constants.SESSION_USERST)); // 승인
																						// 받은
																						// 사람만
																						// loginInfo에
																						// 저장하기
																						// 위해
			}

			String adminF = "A";

			// 세션 로그 입력 함수 생성
			if (args.get("user_stat").toString().equals(adminF)) {
				GrowtropinService.insertLoginInfo(args); // login info 추가
				GrowtropinService.updateRecentConnect(args); // login 시 최근 접속 시간
																// 추가
			}

			GrowtropinService.insertAutoFlag(args); // 아이디저장,자동로그인 플레그값 저장

			return result;
		}

	}

	@RequestMapping(value = "FindId", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> FindId(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String hp = aria.Encrypt((String) args.get("hp"));
		args.put("hp", hp);

		Map<String, Object> result = GrowtropinService.FindIdSelect(args);

		logger.debug(args);
		//
		if (result == null) {
			return result;

		} else {

			if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
				logger.debug("FindId user_co : " + args.get("user_co"));
			} else {
				HttpSession session = request.getSession(true);
				session.setAttribute(Constants.SESSION_USERID, result.get("id"));
			}

			// return GrowtropinService.FindIdSelect(args);
			return result;
		}

	}

	@RequestMapping(value = "findinjectiondata", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> FindInjection(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		// logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findinjectiondata user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		Map<String, Object> result = GrowtropinService.findinjectiondata(args);

		// logger.debug(args);
		//
		if (result == null) {
			return result;

		} else {
			GrowtropinService.removeinjectiondata(args);
			return result;
		}

	}

	@RequestMapping(value = "growSave", method = { RequestMethod.POST })
	@ResponseBody
	public int growSave(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("growSave user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		args.put("stat", Float.parseFloat(args.get("stat").toString()));
		args.put("wt", Float.parseFloat(args.get("wt").toString()));
		args.put("bmi", String.format("%.2f", Float.parseFloat(args.get("bmi").toString())));

		logger.debug(args.get("flag").toString());

		if (args.get("flag").toString().equals("I")) {
			GrowtropinService.insertGrow(args);
		} else {
			GrowtropinService.updateGrow(args);
		}

		return 0;
	}

	@RequestMapping(value = "selectDelMedong", method = { RequestMethod.POST })
	@ResponseBody
	public int selectDelMedong(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);
		HttpSession session = request.getSession(true);

		GrowtropinService.DelMedong(args);
		GrowtropinService.DelMedongChl(args);

		return 0;
	}

	@RequestMapping(value = "selectDelChadan", method = { RequestMethod.POST })
	@ResponseBody
	public int selectDelChadan(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);
		HttpSession session = request.getSession(true);

		GrowtropinService.DelChadan(args);

		return 0;
	}

	@RequestMapping(value = "findUser", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> findUser(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findUser user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		Map<String, Object> result = GrowtropinService.UserSelect(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		result.put("hp", aria.Decrypt(result.get("hp").toString()));

		if (!result.get("zipaddr").toString().equals("")) {
			result.put("zipaddr", aria.Decrypt(result.get("zipaddr").toString()));
		}

		logger.debug("psw:" + result);

		return result;
	}

	// 유저정보검색
	@RequestMapping(value = "SelectUser", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> SelectUser(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		args.put("user_co", args.get("selectuser"));

		// 유저정보검색 함수
		Map<String, Object> result = GrowtropinService.UserSelect(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		// 암호화 부분 복호화
		result.put("hp", aria.Decrypt(result.get("hp").toString()));
		result.put("zipaddr", aria.Decrypt(result.get("zipaddr").toString()));

		logger.debug("result:" + result);

		// 결과감 리턴
		return result;
	}

	// 유저 자녀정보 검색
	@RequestMapping(value = "UserchildSelect", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> UserchildSelect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		args.put("user_co", args.get("selectuser"));

		// 유저 자녀정보검색ㄴ
		List<Map<String, Object>> result = GrowtropinService.ChlListSelect(args);

		logger.debug("result:" + result);

		// 결과값 리턴
		return result;
	}

	@RequestMapping(value = "findGrowthList", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findGrowthList(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findGrowthList user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.growthListSelect(args);
	}

	@RequestMapping(value = "findChl", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findChl(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findChl user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.ChlListSelect(args);
	}

	@RequestMapping(value = "findChlMod", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findChlMod(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findChlMod user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		List<Map<String, Object>> result = GrowtropinService.ChlListModSelect(args);

		Map<String, Object> mapResult;

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		for (int i = 0; i < result.size(); i++) {
			mapResult = result.get(i);

			mapResult.put("hp", aria.Decrypt(mapResult.get("hp").toString()));

			result.set(i, mapResult);
		}

		return result;
	}

	// 자녀정보 수정
	@RequestMapping(value = "chlModify", method = { RequestMethod.POST })
	@ResponseBody
	public void chlModify(HttpServletRequest request, HttpSession session, @RequestBody Map<String, Object> args)
			throws Exception {

		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("chlModify user_co : " + args.get("user_co"));
		} else {
			// 세션의 user 코드 변수 추가
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성.
		Aria aria = new Aria(Key);
		// 암호화 예제
		String userAddr = aria.Encrypt((String) args.get("userAddr"));
		args.put("userAddr", userAddr);

		// 자녀정보 수정
		GrowtropinService.chlUpdate(args);

	}

	// 로그아웃 전 플레그값 가져요기(자동 로그인/아이디 저장)
	@RequestMapping(value = "logoutFlag", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> logoutFlag(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("logoutFlag user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.logoutF(args);
	}

	// 로그아웃
	@RequestMapping(value = "logout", method = { RequestMethod.POST })
	@ResponseBody
	public int logout(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("logout user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		GrowtropinService.updateAutoFlagN(args);

		// 세션 초기화
		/* session.invalidate(); */

		return 0;

	}

	@RequestMapping(value = "chlDelete", method = { RequestMethod.POST })
	@ResponseBody
	public int chlDelete(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("chlDelete user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		GrowtropinService.chlDelete(args);

		return 0;
	}

	@RequestMapping(value = "pswdChk", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> pswdChk(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String PW = aria.Encrypt((String) args.get("PW"));
		args.put("PW", PW);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("pswdChk user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.passwordCheck(args);
	}

	@RequestMapping(value = "upload_image", method = { RequestMethod.POST })
	@ResponseBody
	public String upload_image(@RequestPart("file") MultipartFile multipartFile, HttpServletResponse response,
			@RequestParam("value1") String test, @RequestParam("value2") String param) throws Exception {
		String oooo = "";

		if (multipartFile != null && !multipartFile.isEmpty()) {

			String fileName = multipartFile.getOriginalFilename();

			try {
				File file = new File("https://dahc.azurewebsites.net/resources/img");

				if (file.exists() == false) {
					file.mkdirs();
				}

				file = new File("https://dahc.azurewebsites.net/resources/img/" + fileName);
				multipartFile.transferTo(file);
				oooo = "https://dahc.azurewebsites.net/resources/img/" + fileName;

			} catch (IOException e) {
				oooo = e.getMessage(); //
			}

		} else {

			oooo = "Failed uploaded file : MltipartFile is null";
		}

		return oooo + "/" + test + "/" + param;
	}

	@RequestMapping(value = "withdrawal", method = { RequestMethod.POST })
	@ResponseBody
	public int withdrawal(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("withdrawal user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
			args.put("user_id", session.getAttribute(Constants.SESSION_USERID));
		}

		GrowtropinService.withdrawal(args);
		GrowtropinService.withdrawalInsert(args);

		return 0;
	}

	@RequestMapping(value = "chGrowthInformation", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> chGrowthInformation(HttpServletRequest request,
			@RequestBody Map<String, Object> args) throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("chGrowthInformation user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.chGrowthInfo(args);
	}

	@RequestMapping(value = "chBasicInformation", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> chBasicInformation(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("chBasicInformation user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.chBasicInfo(args);
	}

	@RequestMapping(value = "growthListInformation", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> growthListInformation(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("growthListInformation user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.growthListInfo(args);
	}

	// 나이스 API 메인페이지 라우팅
	@RequestMapping(value = "TEST")
	public String getTEST(HttpServletRequest request, @RequestParam Map<String, Object> args) throws Exception {
		return "TEST";
	}

	// 나이스 API 메인페이지 라우팅
	@RequestMapping(value = "checkplus_main")
	public String getUrmain(HttpServletRequest request, @RequestParam Map<String, Object> args) throws Exception {
		return "checkplus_main";
	}

	// 나이스 API 성공페이지 라우팅
	@RequestMapping(value = "checkplus_success")
	public String getUrl_success(HttpServletRequest request, @RequestParam Map<String, Object> args) throws Exception {
		return "checkplus_success";
	}

	// 나이스 API 실페페이지 라우팅
	@RequestMapping(value = "checkplus_fail")
	public String getUrl_fail(HttpServletRequest request, @RequestParam Map<String, Object> args) throws Exception {
		return "checkplus_fail";
	}

	// 다음주소 API 라우팅
	@RequestMapping(value = "postdaumJuso", method = { RequestMethod.POST })
	@ResponseBody
	public void postdaumJuso(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		System.out.println("argspost:" + args);

		daum.add(args);

		System.out.println("daumpost:" + daum);

		logger.debug("argspost:" + args);

	}

	// 다음조소 API 검색 함수
	@RequestMapping(value = "getdaumJuso", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> getdaumJuso(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		args.put("zip", null);
		args.put("addr", null);

		// daum 배열이 NULL 아니면 난수키와 같은 내역 확인하여 리턴
		if (daum != null) {
			for (int i = 0; i < daum.size(); i++) {

				if (daum.get(i).get("key").toString().equals(args.get("key").toString())) {

					args.put("zip", (String) daum.get(i).get("zip"));
					args.put("addr", (String) daum.get(i).get("addr"));

					daum.remove(i);

					System.out.println("daumget:" + daum);
					System.out.println("argsget:" + args);

				}

			}
		}

		return args;

	}

	// 다음조소 API 선택 주소 변수 입력 함수
	@RequestMapping(value = "postnice", method = { RequestMethod.POST })
	@ResponseBody
	public void postnice(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		System.out.println("args:" + args);

		nice.add(args);

		System.out.println("nice:" + nice);

	}

	// 나이스 API 검색 함수
	@RequestMapping(value = "getnice", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> getnice(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		args.put("userPhone", null);
		args.put("userPhoneVal", null);
		args.put("userName", null);

		// nice 배열이 NULL 아니면 난수키와 같은 내역 확인하여 리턴
		if (nice != null) {
			for (int i = 0; i < nice.size(); i++) {
				if (nice.get(i).get("key").toString().equals(args.get("key").toString())) {

					args.put("userPhone", (String) nice.get(i).get("userPhone"));
					args.put("userPhoneVal", (String) nice.get(i).get("userPhoneVal"));
					args.put("userName", (String) nice.get(i).get("userName"));

					nice.remove(i);

					System.out.println("nice:" + nice);
					System.out.println("args:" + args);

				}

			}
		}

		return args;
	}

	@RequestMapping(value = "growYmdCheck", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> growYmdCheck(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("growYmdCheck user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.growYmdCheck(args);
	}

	@RequestMapping(value = "findGrowthInput", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findGrowthInput(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findGrowthInput user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.findGrowthInput(args);
	}

	@RequestMapping(value = "removeGrowthdata", method = { RequestMethod.POST })
	@ResponseBody
	public int removeGrowthdata(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("removeGrowthdata user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		GrowtropinService.removeGrowthdata(args);

		return 0;
	}

	// Admin home 간호사 환자 count 조회 함수
	@RequestMapping(value = "homeAdmin", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> homeAdmin(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		// 검색 구분이 N 이면 세션 유저정보 변수 추가
		if (args.get("selectcl").equals("N")) {
			if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
				logger.debug("homeAdmin user_co : " + args.get("user_co"));
			} else {
				HttpSession session = request.getSession(true);
				args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
			}
		}

		logger.debug("args:" + args);

		// 임시저장 회원 Count 조회
		Map<String, Object> result = GrowtropinService.Tmp_UserCount(args);

		// 앱승인대기 Count 조회
		result.putAll(GrowtropinService.Nur_UserCount(args));

		// 앱승인대기 Count 조회
		result.putAll(GrowtropinService.Hold_UserCount(args));

		logger.debug("result:" + result);

		// 결과값 리턴
		return result;
	}

	// 간호사 환자정보 조회
	@RequestMapping(value = "Nur_UserSelect", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> Nur_UserSelect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		// 검색 구분이 N 이면 세션 유저정보 변수 추가
		if (args.get("selectcl").equals("N")) {
			if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
				logger.debug("Nur_UserSelect user_co : " + args.get("user_co"));
			} else {
				HttpSession session = request.getSession(true);
				args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
			}
		}

		logger.debug("args:" + args);

		// 간호사 환자조회
		List<Map<String, Object>> result = GrowtropinService.Nur_UserSelect(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		if (result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {

				// 암호화 내용 복호화 하여 결과에 추가
				String hp = aria.Decrypt((String) result.get(i).get("enphone"));

				result.get(i).put("phone", hp);

			}
		}

		logger.debug("result:" + result);

		// 결과값 리턴
		return result;
	}

	// 간호사 승인대기/승인확인 환자 조회
	@RequestMapping(value = "Hold_UserSelect", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> Hold_UserSelect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		// 검색구분이 A결우 센션 유저 변수 추가
		if (args.get("selectcl").equals("A")) {
			if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
				logger.debug("Hold_UserSelect user_co : " + args.get("user_co"));
			} else {
				HttpSession session = request.getSession(true);
				args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
			}
		}

		logger.debug("Hold_UserSelect args:" + args);

		// 검색구분에 따른 환자정보 조회
		List<Map<String, Object>> result = GrowtropinService.Hold_UserSelect(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		if (result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {

				// 암호화 내용 복화화 하여 결과에 추가
				String hp = aria.Decrypt((String) result.get(i).get("enphone"));
				String zipaddr = aria.Decrypt((String) result.get(i).get("enzipaddr"));

				result.get(i).put("phone", hp);
				result.get(i).put("zipaddr", zipaddr);

			}
		}

		logger.debug("result:" + result);

		// 결과값 리턴
		return result;
	}

	// 임시저장 환자 조회
	@RequestMapping(value = "Tmp_UserSelect", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> Tmp_UserSelect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("Tmp_UserSelect user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		// 임시저장 환자 결과 값 리턴
		return GrowtropinService.Tmp_UserSelect(args);
	}

	// 각종정보 정보조회
	@RequestMapping(value = "SelectCode", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> SelectCode(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// 코드테이블 조회 값 리턴
		return GrowtropinService.SelectCode(args);
	}

	// 별원명 정보조회
	@RequestMapping(value = "masterhospi", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> masterhospi(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// 병원명 조회 값 리턴
		return GrowtropinService.masterhospi(args);
	}

	// 임시저장 유저별 유저정보 조회
	@RequestMapping(value = "Tmp_UserSelect_C", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> Tmp_UserSelect_C(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// 유저코드에 맞는 유저정보 조회
		Map<String, Object> result = GrowtropinService.Tmp_UserSelect_C(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		// 복호화
		result.put("hp", aria.Decrypt(result.get("hp").toString()));
		result.put("zipaddr", aria.Decrypt(result.get("zipaddr").toString()));

		logger.debug(result);

		// 결과값 리턴
		return result;
	}

	// 핸드폰 번호 유저정보 조회
	@RequestMapping(value = "Phone_UserSelect", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> Phone_UserSelect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String EnuserPhone = aria.Encrypt((String) args.get("userPhone"));
		args.put("EnuserPhone", EnuserPhone);

		// 핸드폰 번호로 유정정보 조회
		Map<String, Object> result = GrowtropinService.Phone_UserSelect(args);

		// 복회화
		result.put("hp", aria.Decrypt(result.get("hp").toString()));
		result.put("zipaddr", aria.Decrypt(result.get("zipaddr").toString()));

		logger.debug(result);

		// 결과값 리턴
		return result;
	}

	// ID중복 조회
	@RequestMapping(value = "Idcheck", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> Idcheck(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// ID로 user정보 조회
		return GrowtropinService.Idcheck(args);
	}

	// 유저 중복 정보 검색
	@RequestMapping(value = "userIdcheck", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> userIdcheck(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String userPhone = aria.Encrypt((String) args.get("userPhone"));
		args.put("userPhone", userPhone);

		// 결과값 리턴
		return GrowtropinService.userIdcheck(args);
	}

	@RequestMapping(value = "monthInformation", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> monthInformation(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("monthInformation user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.monthInfo(args);
	}

	@RequestMapping(value = "userANselect", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> userANselect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("userANselect user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.userAN(args);
	}

	// 세션정보 검색
	@RequestMapping(value = "Sessionselect", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> Sesionselect(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("userANselect user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("userCo", session.getAttribute(Constants.SESSION_USERCO));
			args.put("id", session.getAttribute(Constants.SESSION_USERID));
			args.put("userNa", session.getAttribute(Constants.SESSION_USERNA));
			args.put("userCl", session.getAttribute(Constants.SESSION_USERCL));
			args.put("userStat", session.getAttribute(Constants.SESSION_USERST));
		}

		// 세션정보 리턴
		return args;
	}

	@RequestMapping(value = "tokenInsert", method = { RequestMethod.POST })
	@ResponseBody
	public int tokenInsert(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("tokenInsert user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		GrowtropinService.tokenInsertValue(args);

		return 0;
	}

	@RequestMapping(value = "injectionAlarm", method = { RequestMethod.POST })
	@ResponseBody
	public void injectionAlarm(HttpServletRequest request, HttpSession session, @RequestBody Map<String, Object> args)
			throws Exception {

		// logger.debug(args);

		String UseYn = "N";
		String period = "";
		String time = "";
		String inj_vol = "";

		List<Map<String, Object>> items = (List<Map<String, Object>>) args.get("injectionInfo");

		List<Map<String, Object>> Alarm;

		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).get("isActive").toString() == "true") {
				period = items.get(i).get("iperiod").toString();
				time = items.get(i).get("itime").toString();
				UseYn = "Y";

				logger.debug(items.get(i).get("id"));

				if (items.get(i).get("id").equals("월")) {
					args.put("mon", "Y");
				} else if (items.get(i).get("id").equals("화")) {
					args.put("tue", "Y");
				} else if (items.get(i).get("id").equals("수")) {
					args.put("wed", "Y");
				} else if (items.get(i).get("id").equals("목")) {
					args.put("thu", "Y");
				} else if (items.get(i).get("id").equals("금")) {
					args.put("fri", "Y");
				} else if (items.get(i).get("id").equals("토")) {
					args.put("sat", "Y");
				} else {
					args.put("sun", "Y");
				}
			} else {
				if (items.get(i).get("id").equals("월")) {
					args.put("mon", "N");
				} else if (items.get(i).get("id").equals("화")) {
					args.put("tue", "N");
				} else if (items.get(i).get("id").equals("수")) {
					args.put("wed", "N");
				} else if (items.get(i).get("id").equals("목")) {
					args.put("thu", "N");
				} else if (items.get(i).get("id").equals("금")) {
					args.put("fri", "N");
				} else if (items.get(i).get("id").equals("토")) {
					args.put("sat", "N");
				} else {
					args.put("sun", "N");
				}
			}
		}

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("injectionAlarm user_co : " + args.get("user_co"));
		} else {
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		args.put("useyn", UseYn);
		args.put("period", period);
		args.put("time", time);
		args.put("inj_vol", args.get("inj_volume").toString());

		Alarm = GrowtropinService.injectionAlarmSelect(args);

		if (Alarm.size() > 0) {
			logger.debug("여기는 update");
			GrowtropinService.injectionAlarmUpdate(args);

		} else {
			logger.debug("여기는 Insert");
			GrowtropinService.injectionAlarmInsert(args);
		}

	}

	@RequestMapping(value = "findAlarm", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findAlarm(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findAlarm user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		// 간호사 소혹 회원 조회
		return GrowtropinService.injectionAlarmSelect(args);
	}

	@RequestMapping(value = "alarmOnOff", method = { RequestMethod.POST })
	@ResponseBody
	public void alarmOnOff(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("alarmOnOff user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		GrowtropinService.alarmOnOff(args);
	}

	// 대기홙자 확인 처리
	@RequestMapping(value = "Hold_UserComfrim", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> Hold_UserComfrim(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("Hold_UserComfrim user_co : " + args.get("user_co"));
		} else {
			// 세션사용
			HttpSession session = request.getSession(true);
			// 승인강호사 변수 추가
			args.put("APPR_NRS", session.getAttribute(Constants.SESSION_USERCO));
		}

		logger.debug(args);

		// 그루트로핀 환자 확인
		GrowtropinService.Hold_UserComfrim(args);

		// crm인터페이스정보 입력
		System.out.println("args : " + args);

		GrowtropinService.crm_interface(args);

		// 별수 초기화
		args.clear();

		args.put("selectcl", "N");

		logger.debug(args);

		// 간호사 소속 회원 조회
		return GrowtropinService.Hold_UserSelect(args);
	}

	@RequestMapping(value = "tokenPushed", method = { RequestMethod.POST })
	@ResponseBody
	public void tokenPushed(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		args.put("user_co", args.get("selectusercode"));

		Map<String, Object> tokenvalue = GrowtropinService.tokenValue(args);
		String token = (String) tokenvalue.get("tokenValue");

		URL url = new URL("https://fcm.googleapis.com/fcm/send"); // "https://fcm.googleapis.com/fcm/send"
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		/*
		 * conn.setRequestProperty("Authorization", "key=" +
		 * "AAAA8kAcD2g:APA91bHYAXEA5js72S5A7gisRPlZ5Bi4CrW-PsvaW_n1OWvBDaASfo2_GTEUeLcutgDUPXdMM8UDexGyCNepeWUf6gW1z7yhwluJzedIW8Hy0k2vEIcHtsdCJm9l-yJE7TPmEBAGU_AE"
		 * );
		 */
		conn.setRequestProperty("Authorization", "key="
				+ "AAAAAjNmC2Q:APA91bF5XrcweElKHEyLdu5Ajy4nraIyoFrrXO91TCP18STKy4s1ftgbcemVRHyzHvGZl6TLc0FJjBtZ9Do3rtmjDyKljdMHEabascNhvd0j0V8nRAbTpdRBNVpaoHzDw6KYzR6pMefN");
		conn.setDoOutput(true);

		String input = "{\"notification\" : {\"title\" : \"" + "자라다" + "\", \"body\" : \"" + "로그인하여 사용하실 수 있습니다."
				+ "\"}, \"to\":\"" + token + "\"}";
		OutputStream os = conn.getOutputStream();
		// 서버에서 날려서 한글 깨지는 사람은 아래처럼 UTF-8로 인코딩해서 날려주자
		os.write(input.getBytes("UTF-8"));
		os.flush();
		os.close();

		int responseCode = conn.getResponseCode();
		logger.debug("\nSending 'POST' request to URL : " + url);
		logger.debug("Post parameters : " + input);
		logger.debug("Response Code : " + responseCode);

	}

	@RequestMapping(value = "adminPushed", method = { RequestMethod.POST })
	@ResponseBody
	public void adminPushed(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		args.put("id", args.get("id"));

		Map<String, Object> name = GrowtropinService.userName(args);
		String username = (String) name.get("userNa");

		List<Map<String, Object>> result = GrowtropinService.adminToken(args);

		int num = result.size();

		int i;

		for (i = 0; i < num; i++) {

			/*
			 * String adminToken = result.get(i).get("tokenValue").toString();
			 */

			if (result.get(i).get("tokenValue") != null) {
				String adminToken = result.get(i).get("tokenValue").toString();

				URL url = new URL("https://fcm.googleapis.com/fcm/send"); // "https://fcm.googleapis.com/fcm/send"
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				/*
				 * conn.setRequestProperty("Authorization", "key=" +
				 * "AAAA8kAcD2g:APA91bHYAXEA5js72S5A7gisRPlZ5Bi4CrW-PsvaW_n1OWvBDaASfo2_GTEUeLcutgDUPXdMM8UDexGyCNepeWUf6gW1z7yhwluJzedIW8Hy0k2vEIcHtsdCJm9l-yJE7TPmEBAGU_AE"
				 * );
				 */
				conn.setRequestProperty("Authorization", "key="
						+ "AAAAAjNmC2Q:APA91bF5XrcweElKHEyLdu5Ajy4nraIyoFrrXO91TCP18STKy4s1ftgbcemVRHyzHvGZl6TLc0FJjBtZ9Do3rtmjDyKljdMHEabascNhvd0j0V8nRAbTpdRBNVpaoHzDw6KYzR6pMefN");
				conn.setDoOutput(true);

				String input = "{\"notification\" : {\"title\" : \"" + "자라다" + "\", \"body\" : \"" + username
						+ "님이 회원가입을 하였습니다. \n확인해주시기 바랍니다." + "\"}, \"to\":\"" + adminToken + "\"}";
				OutputStream os = conn.getOutputStream();
				// 서버에서 날려서 한글 깨지는 사람은 아래처럼 UTF-8로 인코딩해서 날려주자
				os.write(input.getBytes("UTF-8"));
				os.flush();
				os.close();

				int responseCode = conn.getResponseCode();
				logger.debug("\nSending 'POST' request to URL : " + url);
				logger.debug("Post parameters : " + input);
				logger.debug("Response Code : " + responseCode);
			} else {
				System.out.println("admin사용자의 token값이 없기때문에 보낼수가 없다.!");
			}

		}

	}

	@RequestMapping(value = "findInjection", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findInjection(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findInjection user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		if (args.get("flag").equals("Y")) {
			return GrowtropinService.findInjectionY(args);
		} else {
			return GrowtropinService.findInjectionM(args);
		}
	}

	@RequestMapping(value = "victory", method = { RequestMethod.POST })
	@ResponseBody
	public int victory(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("victory user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		GrowtropinService.victoryPhoto(args);

		return 0;
	}

	@RequestMapping(value = "zxcvb", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> zxcvb(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("zxcvb user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.zxcvbnm(args);
	}

	@RequestMapping(value = "findDailyInjection", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findDailyInjection(HttpServletRequest request,
			@RequestBody Map<String, Object> args) throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findDailyInjection user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.findDailyInjection(args);

	}

	@RequestMapping(value = "injInsert", method = { RequestMethod.POST })
	@ResponseBody
	public Map<String, Object> injInsert(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("injInsert user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		if (args.get("iuflag").equals("I")) {
			GrowtropinService.injInsert(args);
		} else {
			GrowtropinService.injUpdate(args);
		}

		return GrowtropinService.INJ_MSG_SELECT_YMD(args);
	}

	@RequestMapping(value = "findInjCo", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findInjCo(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findInjCo user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		return GrowtropinService.findInjCo(args);
	}

	@RequestMapping(value = "tokenInsertTwo", method = { RequestMethod.POST })
	@ResponseBody
	public int tokenInsertTwo(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {
		logger.debug(args);

		HttpSession session = request.getSession(true);

		GrowtropinService.tokenInsertValueTwo(args);

		return 0;
	}

	@RequestMapping(value = "addChildrenMyPage", method = { RequestMethod.POST })
	@ResponseBody
	public void addChildrenMyPage(HttpServletRequest request, @RequestBody Map<String, Object> args) throws Exception {

		System.out.println(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("addChildrenMyPage USER_CO : " + args.get("USER_CO"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("USER_CO", session.getAttribute(Constants.SESSION_USERCO));
		}

		Map<String, Object> result = null;

		result = GrowtropinService.MAX_CHL_CO(args);

		logger.debug("result : " + result);

		logger.debug("CHL_CO : " + result.get("chlCo".toString()));

		args.put("CHL_CO", result.get("chlCo".toString()));

		// 자년 정보 입력
		GrowtropinService.insertUser_ch(args);

	}

	@RequestMapping(value = "updatePhone", method = { RequestMethod.POST })
	@ResponseBody
	public void updatePhone(HttpServletRequest request, HttpSession session, @RequestBody Map<String, Object> args)
			throws Exception {

		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("updatePhone user_co : " + args.get("user_co"));
		} else {
			if (args.get("user_co") == null || args.get("user_co") == "") {
				args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
			}
		}

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String userPhone = aria.Encrypt((String) args.get("newphone"));
		args.put("newphone", userPhone);

		GrowtropinService.updatePhone(args);

	}

	@RequestMapping(value = "updatePwd", method = { RequestMethod.POST })
	@ResponseBody
	public void updatePwd(HttpServletRequest request, HttpSession session, @RequestBody Map<String, Object> args)
			throws Exception {

		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("updatePwd user_co : " + args.get("user_co"));
		} else {
			if (args.get("user_co") == null || args.get("user_co") == "") {
				args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
			}
		}

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);
		// 암호화 예제
		String userAddr = aria.Encrypt((String) args.get("newpwd"));
		args.put("newpwd", userAddr);

		GrowtropinService.updatePwd(args);

	}

	@RequestMapping(value = "findAlarmVol", method = { RequestMethod.POST })
	@ResponseBody
	public List<Map<String, Object>> findAlarmVol(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {
		logger.debug(args);

		if ("android".equals(args.get("deviceType")) || "ios".equals(args.get("deviceType"))) {
			logger.debug("findAlarmVol user_co : " + args.get("user_co"));
		} else {
			HttpSession session = request.getSession(true);
			args.put("user_co", session.getAttribute(Constants.SESSION_USERCO));
		}

		// 간호사 소혹 회원 조회
		return GrowtropinService.findAlarmVol(args);
	}

	// 테스트를 위한 암호 복호화
	@RequestMapping(value = "test", method = { RequestMethod.GET })
	@ResponseBody
	public List<Map<String, Object>> test(HttpServletRequest request, @RequestBody Map<String, Object> args)
			throws Exception {

		logger.debug(args);

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		// 단일 용자 조회
		result.add(GrowtropinService.UserSelect(args));

		// 전체 사용자 조회
		// result = GrowtropinService.UserSelect2(args);

		// key값 조회
		Map<String, Object> selectkey = GrowtropinService.selectencryptkey();
		// key값 대입
		String Key = selectkey.get("encryptkey").toString();

		// key 값으로 암복호화 모듈 생성
		Aria aria = new Aria(Key);

		int i = 0;
		for (Map<String, Object> arg : result) {
			i++;
			if (!"".equals(arg.get("pw").toString())) {
				System.out.println(
						i + " >>> " + arg.get("pw").toString() + " || " + aria.Decrypt(arg.get("pw").toString()));
			}

			if (!"".equals(arg.get("hp").toString())) {

				if (aria.Decrypt(arg.get("hp").toString()).length() >= 11) {
					System.out.println(
							" >>> " + arg.get("hp").toString() + " || " + aria.Decrypt(arg.get("hp").toString()));
				}
			}

			if (!"".equals(arg.get("zipaddr").toString())) {
				System.out.println(
						" >>> " + arg.get("zipaddr").toString() + " || " + aria.Decrypt(arg.get("zipaddr").toString()));
			}
		}

		logger.debug("result:" + result);

		return null;
	}

}
