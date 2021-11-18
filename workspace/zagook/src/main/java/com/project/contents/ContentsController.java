package com.project.contents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.Utility.Utility;
import com.project.feed.FeedDTO;
import com.project.member.Member;

@Controller
public class ContentsController {
	@Autowired
	@Qualifier("com.project.contents.ContentsServiceImpl")
	private ContentsService service;

	@GetMapping("/")
	public String home(HttpServletRequest request, HttpSession session) {
		if(session.getAttribute("id") != null) {
			Map map = new HashMap();
			String id = (String) session.getAttribute("id");
			
			map.put("id", id);
			
			List<ContentsDTO> list = service.list(map);
			List<String> tag_list = new ArrayList();
			
			request.setAttribute("list", list);
			
			int k = 0;
			while (k < list.size()) {
				int cnt = 0;
				int check = 0;
				ContentsDTO dto = list.get(k);
				map.put("contentsno", dto.getContentsno());
				if(service.updateLike(map) > 0) {
					cnt = service.likeCnt(map);
					dto.setLikecnt(cnt);
					check = service.likeCheck(map); 
					if(check > 0) {
						dto.setLike_clicked(check);
					}
				}
				tag_list = service.getTag(dto.getContentsno());
				dto.setTag_list(tag_list);
				k++;
			}
			
		}
		return "/home";
	}
	
	@GetMapping("/contents/create")
	public String create() {

		return "/contents/create";
	}

	@PostMapping("/contents/create")
	public String create(ContentsDTO dto, HttpServletRequest request) throws IOException {// exception 지우기
		String upDir = Contents.getUploadDir();
		String fname = Utility.saveFileSpring(dto.getFilenameMF(), upDir);
		
		int size = (int) dto.getFilenameMF().getSize();

		if (size > 0) {
			dto.setFilename(fname);
		} else {
			dto.setFilename("default.jpg");
		}
		int cnt = service.create(dto);
		int cnt2 = service.create2(dto);
		int cnt3 = service.create3(dto);
		
		if (cnt>0 & cnt2>0 & cnt3>0) {
//			response.setContentType("text/html; charset=UTF-8");
//			 
//			PrintWriter out = response.getWriter();
//			 
//			out.println("<script>alert('계정이 등록 되었습니다');</script>");
			return "redirect:/";
		} else {
			return "error";
		}
	}

	@GetMapping("/contents/update/{contentsno}")
	public String update(@PathVariable("contentsno") int contentsno, Model model) {
		ContentsDTO dto = service.detail(contentsno);
		model.addAttribute("dto", dto);
		return "/contents/update";
	}

	@PostMapping("/contents/update")
	public String update(ContentsDTO dto) {
		int cnt = service.update(dto);
		int cnt2 = service.update2(dto);
		if (cnt>0 & cnt2>0) {
			return "redirect:/";
		} else {
			return "error";
		}
	}

	@GetMapping("/contents/delete/{contentsno}")
	public String delete(@PathVariable("contentsno") int contentsno) {

		return "/contents/delete";
	}

	@PostMapping("/contents/delete")
	public String delete(HttpServletRequest request, int contentsno) {

		int cnt = service.delete(contentsno);
		int cnt2 = service.delete2(contentsno);
		int cnt3 = service.delete3(contentsno);
		if (cnt>0 & cnt2>0 & cnt3>0) {
			return "redirect:/list";
		} else {
			return "/error";
		}
	}

	@GetMapping("/contents/updateFile/{contentsno}/{oldfile}")
	public String updateFile(@PathVariable("contentsno") int contentsno, @PathVariable("oldfile") String oldfile,
			Model model) {
		model.addAttribute("contentsno", contentsno);
		model.addAttribute("oldfile", oldfile);
		return "/contents/updateFile";
	}

	@PostMapping("/contents/updateFile")
	public String updateFile(MultipartFile filenameMF, String oldfile, int contentsno, HttpServletRequest request)
			throws IOException {
		String basePath = Contents.getUploadDir();

		if (oldfile != null && !oldfile.equals("default.jpg")) { // 원본파일 삭제
			Utility.deleteFile(basePath, oldfile);
		}

		// images에 변경 파일 저장
		Map map = new HashMap();
		map.put("contentsno", contentsno);
		map.put("fname", Utility.saveFileSpring(filenameMF, basePath));

		// 디비에 파일명 변경
		int cnt = service.updateFile(map);

		if (cnt == 1) {
			return "redirect:/list";
		} else {
			return "./error";
		}
	}

	@GetMapping("/contents/detail/{contentsno}")
	public String detail(@PathVariable("contentsno") int contentsno, Model model) {
		model.addAttribute("dto", service.detail(contentsno));
		return "/contents/detail";
	}

	@RequestMapping("/contents/list")
	public String list(HttpServletRequest request) {
		// 검색관련------------------------
		String col = Utility.checkNull(request.getParameter("col"));
		String word = Utility.checkNull(request.getParameter("word"));

		if (col.equals("total")) {
			word = "";
		}

		// 페이지관련-----------------------
		int nowPage = 1;// 현재 보고있는 페이지
		if (request.getParameter("nowPage") != null) {
			nowPage = Integer.parseInt(request.getParameter("nowPage"));
		}
		int recordPerPage = 5;// 한페이지당 보여줄 레코드갯수

		// DB에서 가져올 순번-----------------
		int sno = ((nowPage - 1) * recordPerPage) + 1;
		int eno = nowPage * recordPerPage;

		Map map = new HashMap();
		map.put("col", col);
		map.put("word", word);
		map.put("sno", sno);
		map.put("eno", eno);

		int total = service.total(map);

		List<ContentsDTO> list = service.list(map);

		String paging = Utility.paging(total, nowPage, recordPerPage, col, word);

		// request에 Model사용 결과 담는다
		request.setAttribute("list", list);
		request.setAttribute("nowPage", nowPage);
		request.setAttribute("col", col);
		request.setAttribute("word", word);
		request.setAttribute("paging", paging);

		return "/contents/list";

	}

	@GetMapping("/search")
	public String search() {
		return "/search";
	}
	
	@GetMapping(value="/searchInput", produces = "application/json")
	@ResponseBody
	public List<Map> searchInput(HttpServletRequest request) throws IOException {
		String searchInput = Utility.checkNull(request.getParameter("searchInput"));
		List<Map> searchlist = service.searchInput(searchInput);
		System.out.println(searchInput);
		System.out.println(searchlist);
		return searchlist;
	}
	
	@GetMapping("/search/friend")
	public String search_friend() {
		return "/search/friend";
	}
	
	@GetMapping(value="/searchInput_friend", produces = "application/json")
	@ResponseBody
	public List<Map> searchInput_friend(HttpServletRequest request) throws IOException {
		String searchInput = Utility.checkNull(request.getParameter("searchInput_friend"));
		List<Map> searchFriendlist = service.searchInput_friend(searchInput);
		System.out.println(searchInput);
		System.out.println(searchFriendlist);
		return searchFriendlist;
	}
	
	@GetMapping(value="/like", produces = "application/json")
	@ResponseBody
	public int like(HttpServletRequest request, HttpSession session) throws IOException {
		String id = (String) session.getAttribute("id");
		int contentsno = Integer.parseInt(request.getParameter("contentsno"));
		Map map = new HashMap();
		int cnt = 0;
		map.put("contentsno", contentsno);
		map.put("id", id);
		if(service.like(map)>0) {
			service.updateLike(map);
			cnt = service.likeCnt(map);
		}
		return cnt;
	}
	
	@GetMapping(value="/unlike", produces = "application/json")
	@ResponseBody
	public int unlike(HttpServletRequest request, HttpSession session) throws IOException {
		String id = (String) session.getAttribute("id");
		int contentsno = Integer.parseInt(request.getParameter("contentsno"));
		Map map = new HashMap();
		int cnt = 0;
		map.put("contentsno", contentsno);
		map.put("id", id);
		if(service.unlike(map)>0) {
			service.updateLike(map);
			cnt = service.likeCnt(map);
		}
		return cnt;
	}
}
